/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.quickfix.ChangeReturnTypeToUnitFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.returnsUnit
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Don't emit content and return a result.
 */
public class ContentEmissionRule : ComposableFunctionRule() {
  override val id: String = "ContentEmission"
  override val name: String = "Don't Emit Content and Return"
  override val description: String = "Composables should either emit content OR return a value, not both."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-emit-content-and-return-a-result"

  private val layoutComposables = setOf(
    "Box", "Column", "Row", "LazyColumn", "LazyRow", "Card", "Surface", "Scaffold"
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (function.returnsUnit()) return emptyList()

    val bodyText = function.bodyExpression?.text ?: function.bodyBlockExpression?.text ?: return emptyList()
    val emitsContent = layoutComposables.any { bodyText.contains(it) }

    if (emitsContent) {
      return listOf(createViolation(
        element = function.typeReference ?: function.nameIdentifier ?: function,
        message = "Composable emits content but also returns a value",
        tooltip = "Composables should either emit UI content OR return a value, not both. Consider splitting into two functions.",
        quickFixes = listOf(
          ChangeReturnTypeToUnitFix(),
          SuppressComposeRuleFix(id),
        ),
      ))
    }
    return emptyList()
  }
}
