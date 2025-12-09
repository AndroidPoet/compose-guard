/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.modifiers

import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Modifier order matters.
 */
public class ModifierOrderRule : ComposableFunctionRule() {
  override val id: String = "ModifierOrder"
  override val name: String = "Modifier Order Matters"
  override val description: String = "Modifiers are applied in order - clickable before clip can cause issues."
  override val category: RuleCategory = RuleCategory.MODIFIER
  override val severity: RuleSeverity = RuleSeverity.INFO
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#modifier-order-matters"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // This rule requires analyzing modifier chains in function calls
    // Full implementation would parse Modifier.x().y().z() chains
    // Leaving as stub - complex implementation needed
    return emptyList()
  }
}
