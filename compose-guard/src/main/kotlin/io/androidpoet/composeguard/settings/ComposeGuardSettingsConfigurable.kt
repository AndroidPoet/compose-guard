/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.androidpoet.composeguard.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.RuleCategory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSeparator

/**
 * Settings UI for ComposeGuard plugin.
 * Allows users to enable/disable individual rules or entire categories.
 *
 * Simple design:
 * - Master switch: toggles ALL rules on/off (convenience)
 * - Category checkbox: toggles all rules in that category (convenience)
 * - Individual rule checkbox: toggles that specific rule
 * - All state is stored per-rule in ruleEnabledStates map
 */
public class ComposeGuardSettingsConfigurable : Configurable {

  private var mainPanel: JPanel? = null
  private var masterSwitch: JBCheckBox? = null
  private var gutterIconsCheckbox: JBCheckBox? = null
  private var inlayHintsCheckbox: JBCheckBox? = null
  private var suppressBuiltInCheckbox: JBCheckBox? = null

  private val categoryCheckboxes = mutableMapOf<RuleCategory, JBCheckBox>()

  private val ruleCheckboxes = mutableMapOf<String, JBCheckBox>()

  private val rulesByCategory = mapOf(
    RuleCategory.NAMING to listOf(
      RuleInfo("ComposableNaming", "Composable Naming", "Composables returning Unit should use PascalCase"),
      RuleInfo("ComposableAnnotationNaming", "Composable Annotation Naming", "Custom composable annotations should end with 'Composable'"),
      RuleInfo("CompositionLocalNaming", "CompositionLocal Naming", "CompositionLocal should start with 'Local'"),
      RuleInfo("EventParameterNaming", "Event Parameter Naming", "Event parameters should start with 'on'"),
      RuleInfo("MultipreviewNaming", "Multipreview Naming", "Multipreview annotations should have a proper name"),
      RuleInfo("PreviewNaming", "Preview Naming", "Preview functions should end with 'Preview'"),
    ),
    RuleCategory.MODIFIER to listOf(
      RuleInfo("ModifierRequired", "Modifier Required", "Public composables should have a modifier parameter"),
      RuleInfo("ModifierDefaultValue", "Modifier Default Value", "Modifier parameters should have default value"),
      RuleInfo("ModifierNaming", "Modifier Naming", "Modifier parameter should be named 'modifier'"),
      RuleInfo("ModifierTopMost", "Modifier Top Most", "Modifier should be applied to the root composable"),
      RuleInfo("ModifierReuse", "Modifier Reuse", "Avoid reusing modifier instances"),
      RuleInfo("ModifierOrder", "Modifier Order", "Modifier chain order matters for behavior"),
      RuleInfo("AvoidComposed", "Avoid Composed", "Avoid using Modifier.composed()"),
    ),
    RuleCategory.STATE to listOf(
      RuleInfo("RememberState", "Remember State", "State should be wrapped in remember {}"),
      RuleInfo("TypeSpecificState", "Type Specific State", "Use type-specific state functions"),
      RuleInfo("MutableStateParameter", "Mutable State Parameter", "Avoid MutableState as parameter"),
      RuleInfo("HoistState", "Hoist State", "State should be hoisted when appropriate"),
      RuleInfo("DerivedStateOfCandidate", "Remember with Keys", "Computed values should use remember with keys"),
      RuleInfo("FrequentRecomposition", "Lifecycle-Aware Collection", "Suggest collectAsStateWithLifecycle for flows"),
      RuleInfo("LambdaParameterInEffect", "Lambda Parameter In Effect", "Lambda parameters in effects should be keyed"),
    ),
    RuleCategory.PARAMETER to listOf(
      RuleInfo("ParameterOrdering", "Parameter Ordering", "Parameters should be ordered correctly"),
      RuleInfo("TrailingLambda", "Trailing Lambda", "Lambda parameters should be trailing"),
      RuleInfo("MutableParameter", "Mutable Parameter", "Avoid mutable types as parameters"),
      RuleInfo("ExplicitDependencies", "Explicit Dependencies", "Explicit dependencies in effects"),
      RuleInfo("ViewModelForwarding", "ViewModel Forwarding", "Don't forward ViewModels as parameters"),
    ),
    RuleCategory.COMPOSABLE to listOf(
      RuleInfo("ContentEmission", "Content Emission", "Content emission patterns"),
      RuleInfo("MultipleContentEmitters", "Multiple Content Emitters", "Multiple content slots handling"),
      RuleInfo("ContentSlotReused", "Content Slot Reused", "Content slot reuse patterns"),
      RuleInfo("EffectKeys", "Effect Keys", "Effect dependencies and keys"),
      RuleInfo("MovableContent", "Movable Content", "Movable content usage"),
      RuleInfo("PreviewVisibility", "Preview Visibility", "Preview function visibility"),
      RuleInfo("LazyListContentType", "LazyList ContentType", "Heterogeneous LazyLists should use contentType"),
      RuleInfo("LazyListMissingKey", "LazyList Missing Key", "LazyList items() should have a key parameter"),
    ),
    RuleCategory.STRICTER to listOf(
      RuleInfo("Material2Usage", "Material 2 Usage", "Don't use Material 2 (use Material 3 instead)"),
      RuleInfo("UnstableCollections", "Unstable Collections", "Avoid unstable collections"),
    ),
  )

  override fun getDisplayName(): String = "ComposeGuard"

  override fun createComponent(): JComponent {
    val settings = ComposeGuardSettingsState.getInstance()

    mainPanel = JPanel(BorderLayout()).apply {
      border = JBUI.Borders.empty(10)
    }

    val contentPanel = JPanel().apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    contentPanel.add(createMasterSection())
    contentPanel.add(Box.createVerticalStrut(10))
    contentPanel.add(JSeparator())
    contentPanel.add(Box.createVerticalStrut(10))

    contentPanel.add(createDisplayOptionsSection(settings))
    contentPanel.add(Box.createVerticalStrut(10))
    contentPanel.add(JSeparator())
    contentPanel.add(Box.createVerticalStrut(10))

    val rulesHeader = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      val label = JBLabel("Rule Configuration").apply {
        font = font.deriveFont(Font.BOLD, 14f)
      }
      add(label)
      alignmentX = Component.LEFT_ALIGNMENT
    }
    contentPanel.add(rulesHeader)
    contentPanel.add(Box.createVerticalStrut(5))

    val infoLabel = JBLabel("Enable or disable individual rules. Disabled rules will not show warnings.").apply {
      foreground = JBColor.GRAY
      font = font.deriveFont(11f)
    }
    val infoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      add(infoLabel)
      alignmentX = Component.LEFT_ALIGNMENT
    }
    contentPanel.add(infoPanel)
    contentPanel.add(Box.createVerticalStrut(10))

    for ((category, rules) in rulesByCategory) {
      contentPanel.add(createCategoryPanel(category, rules, settings))
      contentPanel.add(Box.createVerticalStrut(5))
    }

    contentPanel.add(Box.createVerticalStrut(10))
    val resetPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      val resetButton = JButton("Reset All to Defaults").apply {
        addActionListener { resetToDefaults() }
      }
      add(resetButton)
      alignmentX = Component.LEFT_ALIGNMENT
    }
    contentPanel.add(resetPanel)

    val scrollPane = JBScrollPane(contentPanel).apply {
      border = null
      verticalScrollBar.unitIncrement = 16
    }

    mainPanel?.add(scrollPane, BorderLayout.CENTER)

    loadCheckboxStates(settings)

    return mainPanel!!
  }

  private fun createMasterSection(): JPanel {
    return JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      masterSwitch = JBCheckBox("Enable All Rules", true).apply {
        font = font.deriveFont(Font.BOLD)
        addActionListener {
          setAllRulesEnabled(isSelected)
        }
      }
      add(masterSwitch)
      alignmentX = Component.LEFT_ALIGNMENT
    }
  }

  private fun createDisplayOptionsSection(settings: ComposeGuardSettingsState): JPanel {
    return JPanel().apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS)
      alignmentX = Component.LEFT_ALIGNMENT

      val headerLabel = JBLabel("Display Options").apply {
        font = font.deriveFont(Font.BOLD, 13f)
      }
      val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
        add(headerLabel)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(headerPanel)
      add(Box.createVerticalStrut(5))

      gutterIconsCheckbox = JBCheckBox("Show gutter icons", settings.showRuleGutterIcons)
      val gutterPanel = JPanel(FlowLayout(FlowLayout.LEFT, 15, 0)).apply {
        add(gutterIconsCheckbox)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(gutterPanel)

      inlayHintsCheckbox = JBCheckBox("Show inlay hints", settings.showRuleInlayHints)
      val inlayPanel = JPanel(FlowLayout(FlowLayout.LEFT, 15, 0)).apply {
        add(inlayHintsCheckbox)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(inlayPanel)

      suppressBuiltInCheckbox = JBCheckBox(
        "Suppress built-in Compose inspections (ModifierParameter, etc.)",
        settings.suppressBuiltInInspections,
      ).apply {
        toolTipText = "Hide Android Studio's built-in Compose lint warnings when ComposeGuard handles them"
      }
      val suppressPanel = JPanel(FlowLayout(FlowLayout.LEFT, 15, 0)).apply {
        add(suppressBuiltInCheckbox)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(suppressPanel)
    }
  }

  private fun createCategoryPanel(
    category: RuleCategory,
    rules: List<RuleInfo>,
    settings: ComposeGuardSettingsState,
  ): JPanel {
    return JPanel().apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS)
      alignmentX = Component.LEFT_ALIGNMENT
      border = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(JBColor.border(), 1),
        JBUI.Borders.empty(8),
      )

      val categoryCheckbox = JBCheckBox(category.displayName, true).apply {
        font = font.deriveFont(Font.BOLD, 12f)
        addActionListener {
          setCategoryRulesEnabled(category, isSelected)
          updateMasterSwitchState()
        }
      }
      categoryCheckboxes[category] = categoryCheckbox

      val categoryPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
        add(categoryCheckbox)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(categoryPanel)
      add(Box.createVerticalStrut(5))

      for (rule in rules) {
        val actualRule = ComposeRuleRegistry.getRuleById(rule.id)
        val defaultEnabled = actualRule?.enabledByDefault ?: true
        val ruleEnabled = settings.isRuleEnabled(rule.id, defaultEnabled)

        val ruleCheckbox = JBCheckBox(rule.displayName, ruleEnabled).apply {
          toolTipText = rule.description
          addActionListener {
            updateCategoryCheckboxState(category)
            updateMasterSwitchState()
          }
        }
        ruleCheckboxes[rule.id] = ruleCheckbox

        val rulePanel = JPanel(FlowLayout(FlowLayout.LEFT, 20, 0)).apply {
          add(ruleCheckbox)
          alignmentX = Component.LEFT_ALIGNMENT
        }
        add(rulePanel)
      }
    }
  }

  /**
   * Load checkbox states from settings.
   */
  private fun loadCheckboxStates(settings: ComposeGuardSettingsState) {
    for ((_, rules) in rulesByCategory) {
      for (rule in rules) {
        val actualRule = ComposeRuleRegistry.getRuleById(rule.id)
        val defaultEnabled = actualRule?.enabledByDefault ?: true
        ruleCheckboxes[rule.id]?.isSelected = settings.isRuleEnabled(rule.id, defaultEnabled)
      }
    }

    for (category in RuleCategory.entries) {
      updateCategoryCheckboxState(category)
    }

    updateMasterSwitchState()
  }

  /**
   * Set all rules to enabled/disabled.
   */
  private fun setAllRulesEnabled(enabled: Boolean) {
    for ((_, checkbox) in categoryCheckboxes) {
      checkbox.isSelected = enabled
    }

    for ((_, checkbox) in ruleCheckboxes) {
      checkbox.isSelected = enabled
    }
  }

  /**
   * Set all rules in a category to enabled/disabled.
   */
  private fun setCategoryRulesEnabled(category: RuleCategory, enabled: Boolean) {
    val rules = rulesByCategory[category] ?: return
    for (rule in rules) {
      ruleCheckboxes[rule.id]?.isSelected = enabled
    }
  }

  /**
   * Update category checkbox state based on its rules.
   * Category is checked if ALL rules in it are checked.
   */
  private fun updateCategoryCheckboxState(category: RuleCategory) {
    val rules = rulesByCategory[category] ?: return
    val allEnabled = rules.all { ruleCheckboxes[it.id]?.isSelected == true }
    categoryCheckboxes[category]?.isSelected = allEnabled
  }

  /**
   * Update master switch state based on all rules.
   * Master switch is checked if ALL rules are checked.
   */
  private fun updateMasterSwitchState() {
    val allEnabled = ruleCheckboxes.values.all { it.isSelected }
    masterSwitch?.isSelected = allEnabled
  }

  /**
   * Reset all settings to defaults.
   */
  private fun resetToDefaults() {
    gutterIconsCheckbox?.isSelected = true
    inlayHintsCheckbox?.isSelected = true
    suppressBuiltInCheckbox?.isSelected = true

    for ((_, rules) in rulesByCategory) {
      for (rule in rules) {
        val actualRule = ComposeRuleRegistry.getRuleById(rule.id)
        val defaultEnabled = actualRule?.enabledByDefault ?: true
        ruleCheckboxes[rule.id]?.isSelected = defaultEnabled
      }
    }

    for (category in RuleCategory.entries) {
      updateCategoryCheckboxState(category)
    }
    updateMasterSwitchState()
  }

  override fun isModified(): Boolean {
    val settings = ComposeGuardSettingsState.getInstance()

    if (gutterIconsCheckbox?.isSelected != settings.showRuleGutterIcons) return true
    if (inlayHintsCheckbox?.isSelected != settings.showRuleInlayHints) return true
    if (suppressBuiltInCheckbox?.isSelected != settings.suppressBuiltInInspections) return true

    for ((_, rules) in rulesByCategory) {
      for (rule in rules) {
        val checkbox = ruleCheckboxes[rule.id] ?: continue
        val actualRule = ComposeRuleRegistry.getRuleById(rule.id)
        val defaultEnabled = actualRule?.enabledByDefault ?: true
        val savedState = settings.isRuleEnabled(rule.id, defaultEnabled)
        if (checkbox.isSelected != savedState) return true
      }
    }

    return false
  }

  override fun apply() {
    val settings = ComposeGuardSettingsState.getInstance()

    settings.showRuleGutterIcons = gutterIconsCheckbox?.isSelected ?: true
    settings.showRuleInlayHints = inlayHintsCheckbox?.isSelected ?: true
    settings.suppressBuiltInInspections = suppressBuiltInCheckbox?.isSelected ?: true

    for ((ruleId, checkbox) in ruleCheckboxes) {
      settings.setRuleEnabled(ruleId, checkbox.isSelected)
    }

    for (project in ProjectManager.getInstance().openProjects) {
      PsiManager.getInstance(project).dropPsiCaches()
      DaemonCodeAnalyzer.getInstance(project).restart()
    }
  }

  override fun reset() {
    val settings = ComposeGuardSettingsState.getInstance()

    gutterIconsCheckbox?.isSelected = settings.showRuleGutterIcons
    inlayHintsCheckbox?.isSelected = settings.showRuleInlayHints
    suppressBuiltInCheckbox?.isSelected = settings.suppressBuiltInInspections

    loadCheckboxStates(settings)
  }

  override fun disposeUIResources() {
    mainPanel = null
    masterSwitch = null
    gutterIconsCheckbox = null
    inlayHintsCheckbox = null
    suppressBuiltInCheckbox = null
    categoryCheckboxes.clear()
    ruleCheckboxes.clear()
  }

  private data class RuleInfo(
    val id: String,
    val displayName: String,
    val description: String,
  )
}
