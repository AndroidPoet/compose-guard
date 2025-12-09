/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseImmutableCollectionFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isMutableType
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Don't use inherently mutable types as parameters.
 */
public class MutableParameterRule : ComposableFunctionRule() {
  override val id: String = "MutableParameter"
  override val name: String = "Don't Use Mutable Types as Parameters"
  override val description: String = "Mutable types (MutableList, ArrayList, etc.) should not be composable parameters."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-use-inherently-mutable-types-as-parameters"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    return function.valueParameters.mapNotNull { param ->
      val typeText = param.typeReference?.text ?: return@mapNotNull null
      if (typeText.isMutableType()) {
        val baseType = typeText.substringBefore("<").trim()
        val quickFix = when {
          baseType.contains("MutableList") || baseType == "ArrayList" ->
            UseImmutableCollectionFix("MutableList", "ImmutableList")
          baseType.contains("MutableSet") || baseType == "HashSet" || baseType == "LinkedHashSet" ->
            UseImmutableCollectionFix("MutableSet", "ImmutableSet")
          baseType.contains("MutableMap") || baseType == "HashMap" || baseType == "LinkedHashMap" ->
            UseImmutableCollectionFix("MutableMap", "ImmutableMap")
          else -> null
        }
        createViolation(
          element = param.typeReference ?: param,
          message = "Don't use mutable type '$typeText' as parameter",
          tooltip = "Mutable types don't trigger recomposition. Use immutable alternatives (ImmutableList, etc.).",
          quickFixes = listOfNotNull(quickFix, SuppressComposeRuleFix(id)),
        )
      } else null
    }
  }
}
