/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.quickfix.RenameComposableFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Multipreview annotations should be named with "Previews" prefix.
 */
public class MultipreviewNamingRule : ComposableFunctionRule() {
  override val id: String = "MultipreviewNaming"
  override val name: String = "Multipreview Naming"
  override val description: String = "Multipreview annotations should start with 'Previews' prefix."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-multipreview-annotations-properly"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // Check if function has multiple @Preview annotations (multipreview pattern)
    val previewCount = function.annotationEntries.count { it.shortName?.asString() == "Preview" }
    if (previewCount <= 1) return emptyList()

    val name = function.name ?: return emptyList()
    if (!name.startsWith("Previews")) {
      val suggestedName = "Previews$name"
      return listOf(createViolation(
        element = function.nameIdentifier ?: function,
        message = "Multipreview function '$name' should start with 'Previews'",
        tooltip = "Functions with multiple @Preview annotations should follow naming pattern 'PreviewsXxx'.",
        quickFixes = listOf(
          RenameComposableFix(suggestedName),
          SuppressComposeRuleFix(id),
        ),
      ))
    }
    return emptyList()
  }
}
