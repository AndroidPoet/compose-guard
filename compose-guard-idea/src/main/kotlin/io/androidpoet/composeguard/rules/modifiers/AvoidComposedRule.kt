/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.modifiers

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.ConvertToModifierNodeFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Avoid composed {} modifier factory.
 */
public class AvoidComposedRule : ComposableFunctionRule() {
  override val id: String = "AvoidComposed"
  override val name: String = "Avoid composed {} Modifier"
  override val description: String = "Use Modifier.Node instead of deprecated composed {} API."
  override val category: RuleCategory = RuleCategory.MODIFIER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#avoid-modifier-extension-factory-functions"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName == "composed") {
        violations.add(createViolation(
          element = call,
          message = "Avoid using 'composed {}' - use Modifier.Node instead",
          tooltip = "The 'composed' API has performance issues. Migrate to Modifier.Node for better performance.",
          quickFixes = listOf(
            ConvertToModifierNodeFix(),
            SuppressComposeRuleFix(id),
          ),
        ))
      }
    }

    return violations
  }
}
