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
package io.androidpoet.composeguard.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.JBUI
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.awt.Color
import javax.swing.Icon

/**
 * Provides gutter icons showing compose rule violations for @Composable functions.
 * Shows a colored icon indicating whether there are rule violations.
 */
public class ComposeGuardLineMarkerProvider : LineMarkerProvider {

  private val settings: ComposeGuardSettingsState
    get() = ComposeGuardSettingsState.getInstance()

  public override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
    // Check if rules and gutter icons are enabled
    if (!settings.isComposeRulesEnabled || !settings.showRuleGutterIcons) {
      return null
    }

    val function = element.parent as? KtNamedFunction ?: return null

    // Only process the name identifier
    if (element != function.nameIdentifier) return null

    // Only process composable functions
    if (!function.isComposable()) {
      return null
    }

    // Analyze for violations
    val violations = analyzeFunction(function)

    if (violations.isEmpty()) {
      return null
    }

    val icon = getIcon(violations)
    val tooltip = buildTooltip(violations)

    return LineMarkerInfo(
      element,
      element.textRange,
      icon,
      { tooltip },
      null,
      GutterIconRenderer.Alignment.RIGHT,
    )
  }

  private fun analyzeFunction(function: KtNamedFunction): List<ComposeRuleViolation> {
    val file = function.containingKtFile
    val context = AnalysisContext(file, isOnTheFly = true)
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

  /**
   * Gets the appropriate icon based on violation severity.
   *
   * Colors:
   * - Red: Has ERROR severity violations
   * - Orange: Has WARNING severity violations
   * - Yellow: Has WEAK_WARNING severity violations
   * - Blue: Has INFO severity violations
   */
  private fun getIcon(violations: List<ComposeRuleViolation>): Icon {
    val hasError = violations.any { it.rule.severity == RuleSeverity.ERROR }
    val hasWarning = violations.any { it.rule.severity == RuleSeverity.WARNING }
    val hasWeakWarning = violations.any { it.rule.severity == RuleSeverity.WEAK_WARNING }

    val color = when {
      hasError -> ERROR_COLOR
      hasWarning -> WARNING_COLOR
      hasWeakWarning -> WEAK_WARNING_COLOR
      else -> INFO_COLOR
    }

    return ColorIcon(JBUI.scale(10), color)
  }

  /**
   * Builds a tooltip string for the gutter icon.
   */
  private fun buildTooltip(violations: List<ComposeRuleViolation>): String {
    return buildString {
      append("<html><body>")
      append("<b>Compose Rules: ${violations.size} violation(s)</b><br/><br/>")

      // Group by severity
      val byCategory = violations.groupBy { it.rule.category }

      for ((category, categoryViolations) in byCategory) {
        append("<b>${category.displayName}:</b><br/>")
        for (violation in categoryViolations.take(5)) {
          val icon = when (violation.rule.severity) {
            RuleSeverity.ERROR -> "❌"
            RuleSeverity.WARNING -> "⚠️"
            RuleSeverity.WEAK_WARNING -> "💡"
            RuleSeverity.INFO -> "ℹ️"
          }
          append("$icon ${violation.rule.name}<br/>")
        }
        if (categoryViolations.size > 5) {
          append("... and ${categoryViolations.size - 5} more<br/>")
        }
        append("<br/>")
      }

      append("<i>Click to view details</i>")
      append("</body></html>")
    }
  }

  private companion object {
    private val ERROR_COLOR = JBColor(
      Color(232, 104, 74),  // Light theme: red/orange
      Color(232, 104, 74),  // Dark theme: red/orange
    )
    private val WARNING_COLOR = JBColor(
      Color(240, 198, 116), // Light theme: orange/yellow
      Color(240, 198, 116), // Dark theme: orange/yellow
    )
    private val WEAK_WARNING_COLOR = JBColor(
      Color(174, 174, 174), // Light theme: gray
      Color(174, 174, 174), // Dark theme: gray
    )
    private val INFO_COLOR = JBColor(
      Color(95, 184, 101),  // Light theme: green
      Color(95, 184, 101),  // Dark theme: green
    )
  }
}
