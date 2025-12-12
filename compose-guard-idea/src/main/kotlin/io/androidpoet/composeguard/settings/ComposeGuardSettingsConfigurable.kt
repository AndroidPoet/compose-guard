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
import com.intellij.ui.CheckBoxList
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.RuleCategory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

/**
 * Settings UI for ComposeGuard plugin.
 * Allows users to enable/disable individual rules or entire categories.
 */
public class ComposeGuardSettingsConfigurable : Configurable {

  private var mainPanel: JPanel? = null
  private var masterSwitch: JBCheckBox? = null
  private var gutterIconsCheckbox: JBCheckBox? = null
  private var inlayHintsCheckbox: JBCheckBox? = null

  // Category checkboxes
  private var namingCategoryCheckbox: JBCheckBox? = null
  private var modifierCategoryCheckbox: JBCheckBox? = null
  private var stateCategoryCheckbox: JBCheckBox? = null
  private var parameterCategoryCheckbox: JBCheckBox? = null
  private var composableCategoryCheckbox: JBCheckBox? = null
  private var stricterCategoryCheckbox: JBCheckBox? = null
  private var experimentalCategoryCheckbox: JBCheckBox? = null

  // Rule checkboxes organized by category
  private val ruleCheckboxes = mutableMapOf<String, JBCheckBox>()

  // All rules organized by category
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
      RuleInfo("ModifierTopMost", "Modifier Top Most", "Modifier should be the first optional parameter"),
      RuleInfo("ModifierReuse", "Modifier Reuse", "Avoid reusing modifier instances"),
      RuleInfo("ModifierOrder", "Modifier Order", "Modifier chain order matters for behavior"),
      RuleInfo("AvoidComposed", "Avoid Composed", "Avoid using Modifier.composed()"),
    ),
    RuleCategory.STATE to listOf(
      RuleInfo("RememberState", "Remember State", "State should be wrapped in remember {}"),
      RuleInfo("TypeSpecificState", "Type Specific State", "Use type-specific state functions"),
      RuleInfo("MutableStateParameter", "Mutable State Parameter", "Avoid MutableState as parameter"),
      RuleInfo("HoistState", "Hoist State", "State should be hoisted when appropriate"),
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
      RuleInfo("LambdaParameterInEffect", "Lambda Parameter In Effect", "Lambda parameters in effects"),
      RuleInfo("MovableContent", "Movable Content", "Movable content usage"),
      RuleInfo("PreviewVisibility", "Preview Visibility", "Preview function visibility"),
    ),
    RuleCategory.STRICTER to listOf(
      RuleInfo("Material2Usage", "Material 2 Usage", "Don't use Material 2 (use Material 3 instead)"),
      RuleInfo("UnstableCollections", "Unstable Collections", "Avoid unstable collections"),
    ),
    RuleCategory.EXPERIMENTAL to listOf(
      // LazyList Optimization Rules
      RuleInfo("LazyListMissingKey", "LazyList Missing Key", "LazyList items() should have a key parameter"),
      RuleInfo("LazyListContentType", "LazyList ContentType", "Heterogeneous LazyLists should use contentType"),
      // Performance Rules
      RuleInfo("DerivedStateOfCandidate", "Remember with Keys", "Computed values should use remember with keys"),
      RuleInfo("FrequentRecomposition", "Lifecycle-Aware Collection", "Suggest collectAsStateWithLifecycle for flows"),
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

    // Master switch section
    val masterSection = createMasterSection(settings)
    contentPanel.add(masterSection)
    contentPanel.add(Box.createVerticalStrut(10))
    contentPanel.add(JSeparator())
    contentPanel.add(Box.createVerticalStrut(10))

    // Display options section
    val displaySection = createDisplayOptionsSection(settings)
    contentPanel.add(displaySection)
    contentPanel.add(Box.createVerticalStrut(10))
    contentPanel.add(JSeparator())
    contentPanel.add(Box.createVerticalStrut(10))

    // Rules section header
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

    // Categories with rules
    for ((category, rules) in rulesByCategory) {
      val categoryPanel = createCategoryPanel(category, rules, settings)
      contentPanel.add(categoryPanel)
      contentPanel.add(Box.createVerticalStrut(5))
    }

    // Reset button
    contentPanel.add(Box.createVerticalStrut(10))
    val resetPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      val resetButton = JButton("Reset All to Defaults").apply {
        addActionListener {
          resetToDefaults()
        }
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

    updateCategoryCheckboxStates()

    return mainPanel!!
  }

  private fun createMasterSection(settings: ComposeGuardSettingsState): JPanel {
    return JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
      masterSwitch = JBCheckBox("Enable ComposeGuard", settings.isComposeRulesEnabled).apply {
        font = font.deriveFont(Font.BOLD)
        addActionListener {
          updateAllRulesEnabled(isSelected)
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

      // Category header with checkbox
      val categoryCheckbox = JBCheckBox(category.displayName, isCategoryEnabled(category, settings)).apply {
        font = font.deriveFont(Font.BOLD, 12f)
        addActionListener {
          updateCategoryRules(category, isSelected)
        }
      }

      when (category) {
        RuleCategory.NAMING -> namingCategoryCheckbox = categoryCheckbox
        RuleCategory.MODIFIER -> modifierCategoryCheckbox = categoryCheckbox
        RuleCategory.STATE -> stateCategoryCheckbox = categoryCheckbox
        RuleCategory.PARAMETER -> parameterCategoryCheckbox = categoryCheckbox
        RuleCategory.COMPOSABLE -> composableCategoryCheckbox = categoryCheckbox
        RuleCategory.STRICTER -> stricterCategoryCheckbox = categoryCheckbox
        RuleCategory.EXPERIMENTAL -> experimentalCategoryCheckbox = categoryCheckbox
      }

      val categoryPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
        add(categoryCheckbox)
        alignmentX = Component.LEFT_ALIGNMENT
      }
      add(categoryPanel)
      add(Box.createVerticalStrut(5))

      // Rules in this category
      for (rule in rules) {
        val actualRule = ComposeRuleRegistry.getRuleById(rule.id)
        val defaultEnabled = actualRule?.enabledByDefault ?: true
        val ruleCheckbox = JBCheckBox(rule.displayName, settings.isRuleEnabled(rule.id, defaultEnabled)).apply {
          toolTipText = rule.description
          addActionListener {
            updateCategoryCheckboxState(category)
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

  private fun isCategoryEnabled(category: RuleCategory, settings: ComposeGuardSettingsState): Boolean {
    return when (category) {
      RuleCategory.NAMING -> settings.enableNamingRules
      RuleCategory.MODIFIER -> settings.enableModifierRules
      RuleCategory.STATE -> settings.enableStateRules
      RuleCategory.PARAMETER -> settings.enableParameterRules
      RuleCategory.COMPOSABLE -> settings.enableComposableRules
      RuleCategory.STRICTER -> settings.enableStricterRules
      RuleCategory.EXPERIMENTAL -> settings.enableExperimentalRules
    }
  }

  private fun updateCategoryRules(category: RuleCategory, enabled: Boolean) {
    val rules = rulesByCategory[category] ?: return
    for (rule in rules) {
      ruleCheckboxes[rule.id]?.isSelected = enabled
    }
  }

  private fun updateCategoryCheckboxState(category: RuleCategory) {
    val rules = rulesByCategory[category] ?: return
    val allEnabled = rules.all { ruleCheckboxes[it.id]?.isSelected == true }
    val categoryCheckbox = when (category) {
      RuleCategory.NAMING -> namingCategoryCheckbox
      RuleCategory.MODIFIER -> modifierCategoryCheckbox
      RuleCategory.STATE -> stateCategoryCheckbox
      RuleCategory.PARAMETER -> parameterCategoryCheckbox
      RuleCategory.COMPOSABLE -> composableCategoryCheckbox
      RuleCategory.STRICTER -> stricterCategoryCheckbox
      RuleCategory.EXPERIMENTAL -> experimentalCategoryCheckbox
    }
    categoryCheckbox?.isSelected = allEnabled
  }

  private fun updateCategoryCheckboxStates() {
    for (category in RuleCategory.entries) {
      updateCategoryCheckboxState(category)
    }
  }

  private fun updateAllRulesEnabled(enabled: Boolean) {
    // Update all category checkboxes
    namingCategoryCheckbox?.isSelected = enabled
    modifierCategoryCheckbox?.isSelected = enabled
    stateCategoryCheckbox?.isSelected = enabled
    parameterCategoryCheckbox?.isSelected = enabled
    composableCategoryCheckbox?.isSelected = enabled
    stricterCategoryCheckbox?.isSelected = enabled
    // Note: Experimental rules are not affected by master switch toggle
    // experimentalCategoryCheckbox?.isSelected = enabled

    // Update all rule checkboxes (except experimental)
    for ((ruleId, checkbox) in ruleCheckboxes) {
      // Don't auto-enable experimental rules when master switch is toggled
      val isExperimental = rulesByCategory[RuleCategory.EXPERIMENTAL]?.any { it.id == ruleId } == true
      if (!isExperimental) {
        checkbox.isSelected = enabled
      }
    }
  }

  private fun resetToDefaults() {
    masterSwitch?.isSelected = true
    gutterIconsCheckbox?.isSelected = true
    inlayHintsCheckbox?.isSelected = true

    // Reset all category checkboxes
    namingCategoryCheckbox?.isSelected = true
    modifierCategoryCheckbox?.isSelected = true
    stateCategoryCheckbox?.isSelected = true
    parameterCategoryCheckbox?.isSelected = true
    composableCategoryCheckbox?.isSelected = true
    stricterCategoryCheckbox?.isSelected = true
    experimentalCategoryCheckbox?.isSelected = false // Experimental disabled by default

    // Reset all rule checkboxes
    for ((ruleId, checkbox) in ruleCheckboxes) {
      // Experimental rules are disabled by default
      val isExperimental = rulesByCategory[RuleCategory.EXPERIMENTAL]?.any { it.id == ruleId } == true
      checkbox.isSelected = !isExperimental
    }
  }

  override fun isModified(): Boolean {
    val settings = ComposeGuardSettingsState.getInstance()

    if (masterSwitch?.isSelected != settings.isComposeRulesEnabled) return true
    if (gutterIconsCheckbox?.isSelected != settings.showRuleGutterIcons) return true
    if (inlayHintsCheckbox?.isSelected != settings.showRuleInlayHints) return true

    if (namingCategoryCheckbox?.isSelected != settings.enableNamingRules) return true
    if (modifierCategoryCheckbox?.isSelected != settings.enableModifierRules) return true
    if (stateCategoryCheckbox?.isSelected != settings.enableStateRules) return true
    if (parameterCategoryCheckbox?.isSelected != settings.enableParameterRules) return true
    if (composableCategoryCheckbox?.isSelected != settings.enableComposableRules) return true
    if (stricterCategoryCheckbox?.isSelected != settings.enableStricterRules) return true
    if (experimentalCategoryCheckbox?.isSelected != settings.enableExperimentalRules) return true

    for ((ruleId, checkbox) in ruleCheckboxes) {
      val rule = ComposeRuleRegistry.getRuleById(ruleId)
      val defaultEnabled = rule?.enabledByDefault ?: true
      if (checkbox.isSelected != settings.isRuleEnabled(ruleId, defaultEnabled)) return true
    }

    return false
  }

  override fun apply() {
    val settings = ComposeGuardSettingsState.getInstance()

    settings.isComposeRulesEnabled = masterSwitch?.isSelected ?: true
    settings.showRuleGutterIcons = gutterIconsCheckbox?.isSelected ?: true
    settings.showRuleInlayHints = inlayHintsCheckbox?.isSelected ?: true

    settings.enableNamingRules = namingCategoryCheckbox?.isSelected ?: true
    settings.enableModifierRules = modifierCategoryCheckbox?.isSelected ?: true
    settings.enableStateRules = stateCategoryCheckbox?.isSelected ?: true
    settings.enableParameterRules = parameterCategoryCheckbox?.isSelected ?: true
    settings.enableComposableRules = composableCategoryCheckbox?.isSelected ?: true
    settings.enableStricterRules = stricterCategoryCheckbox?.isSelected ?: true
    settings.enableExperimentalRules = experimentalCategoryCheckbox?.isSelected ?: false

    for ((ruleId, checkbox) in ruleCheckboxes) {
      settings.setRuleEnabled(ruleId, checkbox.isSelected)
    }

    // Restart code analysis to apply changes in real-time
    for (project in ProjectManager.getInstance().openProjects) {
      // Drop all PSI caches to ensure fresh analysis
      PsiManager.getInstance(project).dropPsiCaches()

      // Restart daemon to refresh all highlighting (inspections, annotators, line markers, inlay hints)
      DaemonCodeAnalyzer.getInstance(project).restart()
    }
  }

  override fun reset() {
    val settings = ComposeGuardSettingsState.getInstance()

    masterSwitch?.isSelected = settings.isComposeRulesEnabled
    gutterIconsCheckbox?.isSelected = settings.showRuleGutterIcons
    inlayHintsCheckbox?.isSelected = settings.showRuleInlayHints

    namingCategoryCheckbox?.isSelected = settings.enableNamingRules
    modifierCategoryCheckbox?.isSelected = settings.enableModifierRules
    stateCategoryCheckbox?.isSelected = settings.enableStateRules
    parameterCategoryCheckbox?.isSelected = settings.enableParameterRules
    composableCategoryCheckbox?.isSelected = settings.enableComposableRules
    stricterCategoryCheckbox?.isSelected = settings.enableStricterRules
    experimentalCategoryCheckbox?.isSelected = settings.enableExperimentalRules

    for ((ruleId, checkbox) in ruleCheckboxes) {
      val rule = ComposeRuleRegistry.getRuleById(ruleId)
      val defaultEnabled = rule?.enabledByDefault ?: true
      checkbox.isSelected = settings.isRuleEnabled(ruleId, defaultEnabled)
    }

    updateCategoryCheckboxStates()
  }

  override fun disposeUIResources() {
    mainPanel = null
    masterSwitch = null
    gutterIconsCheckbox = null
    inlayHintsCheckbox = null
    namingCategoryCheckbox = null
    modifierCategoryCheckbox = null
    stateCategoryCheckbox = null
    parameterCategoryCheckbox = null
    composableCategoryCheckbox = null
    stricterCategoryCheckbox = null
    experimentalCategoryCheckbox = null
    ruleCheckboxes.clear()
  }

  private data class RuleInfo(
    val id: String,
    val displayName: String,
    val description: String,
  )
}
