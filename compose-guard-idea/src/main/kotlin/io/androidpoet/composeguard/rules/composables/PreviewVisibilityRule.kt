/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.quickfix.MakePreviewPrivateFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isPreview
import io.androidpoet.composeguard.rules.isPublic
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Preview composables should not be public.
 */
public class PreviewVisibilityRule : ComposableFunctionRule() {
  override val id: String = "PreviewVisibility"
  override val name: String = "Preview Should Be Private"
  override val description: String = "Preview functions should be private to prevent accidental use in production."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#preview-composables-should-not-be-public"

  override fun shouldAnalyze(function: KtNamedFunction): Boolean = function.isPreview()

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (function.isPublic()) {
      return listOf(createViolation(
        element = function.nameIdentifier ?: function,
        message = "Preview function '${function.name}' should be private",
        tooltip = "Preview functions should be private to prevent accidental use in production code.",
        quickFixes = listOf(
          MakePreviewPrivateFix(),
          SuppressComposeRuleFix(id),
        ),
      ))
    }
    return emptyList()
  }
}
