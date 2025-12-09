/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.composables

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.ReplaceEffectKeyFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Be mindful of effect keys.
 */
public class EffectKeysRule : ComposableFunctionRule() {
  override val id: String = "EffectKeys"
  override val name: String = "Effect Keys Correctness"
  override val description: String = "LaunchedEffect and DisposableEffect should have appropriate keys."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#be-mindful-of-effect-keys"

  private val effectComposables = setOf("LaunchedEffect", "DisposableEffect", "SideEffect")

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName !in effectComposables) continue

      val args = call.valueArguments
      if (calleeName == "LaunchedEffect" || calleeName == "DisposableEffect") {
        // Check if using Unit as key (anti-pattern for effects that should restart)
        val firstArg = args.firstOrNull()?.text
        if (firstArg == "Unit" || firstArg == "true") {
          violations.add(createViolation(
            element = call,
            message = "'$calleeName($firstArg)' - using constant key means effect never restarts",
            tooltip = "Consider if this effect should restart based on some state. Using a constant key like Unit or true means the effect runs once and never restarts.",
            quickFixes = listOf(
              ReplaceEffectKeyFix(),
              SuppressComposeRuleFix(id),
            ),
          ))
        }
      }
    }

    return violations
  }
}
