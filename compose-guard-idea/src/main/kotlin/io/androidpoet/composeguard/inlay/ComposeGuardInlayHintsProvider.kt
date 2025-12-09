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
package io.androidpoet.composeguard.inlay

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.JBUI
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Provides inline hints showing compose rule violations.
 * Shows small badges next to violations indicating the rule that was violated.
 */
@Suppress("UnstableApiUsage")
public class ComposeGuardInlayHintsProvider :
  InlayHintsProvider<ComposeGuardInlayHintsProvider.Settings> {

  override val key: SettingsKey<Settings> = SettingsKey("compose.rules.inlay.hints")

  override val name: String = "Compose Rules Hints"

  override val previewText: String = """
        @Composable
        fun userCard(  // Should be UserCard
            user: User,
            onClick: () -> Unit,
            items: MutableList<String>  // Mutable type
        ) {
            val state = mutableStateOf(0)  // Should use remember
        }
  """.trimIndent()

  override fun createSettings(): Settings = Settings()

  override fun createConfigurable(settings: Settings): ImmediateConfigurable {
    return object : ImmediateConfigurable {
      override fun createComponent(listener: ChangeListener): JComponent {
        return JPanel()
      }
    }
  }

  override fun getCollectorFor(
    file: PsiFile,
    editor: Editor,
    settings: Settings,
    sink: InlayHintsSink,
  ): InlayHintsCollector {
    return object : FactoryInlayHintsCollector(editor) {
      private val globalSettings = ComposeGuardSettingsState.getInstance()

      override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        if (!globalSettings.isComposeRulesEnabled || !globalSettings.showRuleInlayHints) {
          return true
        }

        if (element !is KtNamedFunction) return true

        if (!element.isComposable()) {
          return true
        }

        val ktFile = element.containingFile as? KtFile ?: return true
        val context = AnalysisContext(ktFile, isOnTheFly = true)
        val violations = collectViolations(element, context)

        if (violations.isEmpty()) {
          return true
        }

        // Add hint after function name
        element.nameIdentifier?.let { nameElement ->
          val presentation = createViolationHint(factory, violations)
          sink.addInlineElement(
            offset = nameElement.textRange.endOffset,
            relatesToPrecedingText = true,
            presentation = presentation,
            placeAtTheEndOfLine = false,
          )
        }

        return true
      }

      private fun collectViolations(
        function: KtNamedFunction,
        context: AnalysisContext,
      ): List<ComposeRuleViolation> {
        val enabledRules = ComposeRuleRegistry.getEnabledRules()
        val allViolations = mutableListOf<ComposeRuleViolation>()

        for (rule in enabledRules) {
          try {
            val violations = rule.analyzeFunction(function, context)
            allViolations.addAll(violations)
          } catch (e: Exception) {
            // Skip rules that fail
          }
        }

        return allViolations
      }

      private fun createViolationHint(
        factory: PresentationFactory,
        violations: List<ComposeRuleViolation>,
      ): InlayPresentation {
        val maxSeverity = violations.maxOfOrNull { it.rule.severity } ?: RuleSeverity.INFO
        val color = getColorForSeverity(maxSeverity)

        val icon = ColorIcon(JBUI.scale(8), color)
        val count = violations.size
        val text = if (count == 1) {
          violations.first().rule.id
        } else {
          "$count issues"
        }

        var presentation = factory.roundWithBackground(
          factory.seq(
            factory.icon(icon),
            factory.smallText(" $text"),
          ),
        )

        // Add tooltip
        val tooltipText = buildTooltip(violations)
        presentation = factory.withTooltip(tooltipText, presentation)

        return presentation
      }

      private fun getColorForSeverity(severity: RuleSeverity): Color {
        return when (severity) {
          RuleSeverity.ERROR -> ERROR_COLOR
          RuleSeverity.WARNING -> WARNING_COLOR
          RuleSeverity.WEAK_WARNING -> WEAK_WARNING_COLOR
          RuleSeverity.INFO -> INFO_COLOR
        }
      }

      private fun buildTooltip(violations: List<ComposeRuleViolation>): String {
        return buildString {
          append("<html><body>")
          append("<b>Compose Rules Violations (${violations.size})</b><br/><br/>")

          for (violation in violations.take(10)) {
            val icon = when (violation.rule.severity) {
              RuleSeverity.ERROR -> "❌"
              RuleSeverity.WARNING -> "⚠️"
              RuleSeverity.WEAK_WARNING -> "💡"
              RuleSeverity.INFO -> "ℹ️"
            }
            append("$icon <b>${violation.rule.id}</b>: ${violation.message}<br/>")
          }

          if (violations.size > 10) {
            append("<br/>... and ${violations.size - 10} more violations")
          }

          append("</body></html>")
        }
      }
    }
  }

  public data class Settings(
    var showAllViolations: Boolean = true,
    var showOnlyHighSeverity: Boolean = false,
  )

  private companion object {
    private val ERROR_COLOR = JBColor(
      Color(232, 104, 74),
      Color(232, 104, 74),
    )
    private val WARNING_COLOR = JBColor(
      Color(240, 198, 116),
      Color(240, 198, 116),
    )
    private val WEAK_WARNING_COLOR = JBColor(
      Color(174, 174, 174),
      Color(174, 174, 174),
    )
    private val INFO_COLOR = JBColor(
      Color(95, 184, 101),
      Color(95, 184, 101),
    )
  }
}
