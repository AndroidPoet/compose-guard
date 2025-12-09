/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Custom composable annotations should end with "Composable" suffix.
 */
public class ComposableAnnotationNamingRule : ComposableFunctionRule() {
  override val id: String = "ComposableAnnotationNaming"
  override val name: String = "Composable Annotation Naming"
  override val description: String = "Custom composable annotations should end with 'Composable' suffix."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-composable-annotations-properly"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // This rule applies to annotation classes, not functions
    // Leaving as stub - would need different visitor pattern
    return emptyList()
  }
}
