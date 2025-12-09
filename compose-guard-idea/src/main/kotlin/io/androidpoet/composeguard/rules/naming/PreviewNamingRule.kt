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
import io.androidpoet.composeguard.rules.isPreview
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Preview functions should follow naming conventions.
 */
public class PreviewNamingRule : ComposableFunctionRule() {
  override val id: String = "PreviewNaming"
  override val name: String = "Preview Naming Convention"
  override val description: String = "Preview functions should include 'Preview' in their name."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-previews-properly"

  override fun shouldAnalyze(function: KtNamedFunction): Boolean = function.isPreview()

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val name = function.name ?: return emptyList()
    if (!name.contains("Preview", ignoreCase = true)) {
      val suggestedName = "${name}Preview"
      return listOf(createViolation(
        element = function.nameIdentifier ?: function,
        message = "Preview function '$name' should include 'Preview' in name",
        tooltip = "Add 'Preview' suffix to clearly identify preview functions (e.g., '$suggestedName').",
        quickFixes = listOf(
          RenameComposableFix(suggestedName),
          SuppressComposeRuleFix(id),
        ),
      ))
    }
    return emptyList()
  }
}
