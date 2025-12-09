/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Don't emit multiple pieces of content without a container.
 */
public class MultipleContentRule : ComposableFunctionRule() {
  override val id: String = "MultipleContent"
  override val name: String = "Don't Emit Multiple Content Pieces"
  override val description: String = "Composables should wrap multiple content pieces in a layout container."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.INFO
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-emit-multiple-pieces-of-content"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // This rule requires complex analysis of emitted content
    // Full implementation would analyze the composable call tree
    // Leaving as stub for future implementation
    return emptyList()
  }
}
