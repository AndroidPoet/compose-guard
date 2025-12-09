/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.quickfix.RenameParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Event parameters should follow "on" + verb naming pattern.
 */
public class EventParameterNamingRule : ComposableFunctionRule() {
  override val id: String = "EventParameterNaming"
  override val name: String = "Event Parameter Naming"
  override val description: String = "Event callbacks should follow 'on' + present-tense verb pattern (onClick, not onClicked)."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-parameters-properly"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    for (param in function.valueParameters) {
      val name = param.name ?: continue
      val typeText = param.typeReference?.text ?: continue

      // Check if this is a callback (function type)
      if (!typeText.contains("->")) continue

      // Check for past-tense event names (onClicked, onChanged, etc.)
      if (name.startsWith("on") && name.endsWith("ed")) {
        val suggestedName = name.dropLast(2) // Remove "ed"
        violations.add(createViolation(
          element = param.nameIdentifier ?: param,
          message = "Event '$name' should use present-tense verb",
          tooltip = "Use present-tense: '$suggestedName' instead of '$name'.",
          quickFixes = listOf(
            RenameParameterFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ))
      }
    }

    return violations
  }
}
