/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.androidpoet.composeguard.rules.composables

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isSuppressed
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

public class EffectKeysRule : ComposableFunctionRule() {
  override val id: String = "EffectKeys"
  override val name: String = "Effect Keys Correctness"
  override val description: String = "LaunchedEffect and DisposableEffect should have appropriate keys."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#be-mindful-of-effect-keys"

  private val effectComposables = setOf("LaunchedEffect", "DisposableEffect")
  private val constantKeys = setOf("Unit", "true", "false")

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (isSuppressed(function, id)) {
      return emptyList()
    }

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName !in effectComposables) continue

      if (isSuppressed(call, id)) continue

      val args = call.valueArguments
      val firstArg = args.firstOrNull()?.text
      if (firstArg in constantKeys) {
        violations.add(
          createViolation(
            element = call,
            message = "'$calleeName($firstArg)' - using constant key means effect never restarts",
            tooltip = """
              Using a constant key like Unit, true, or false means the effect runs once
              and never restarts during recomposition.

              This may be intentional if the effect should only run once, but often
              indicates a bug where the effect should restart when some state changes.

              Consider if the effect should restart based on some parameter:
              $calleeName(someId) { ... }

              If running once is intentional, you can suppress this warning.
            """.trimIndent(),
            quickFixes = listOf(
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }
}
