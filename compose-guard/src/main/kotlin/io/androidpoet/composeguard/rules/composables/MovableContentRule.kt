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
import io.androidpoet.composeguard.quickfix.WrapInRememberFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

public class MovableContentRule : ComposableFunctionRule() {
  override val id: String = "MovableContent"
  override val name: String = "Movable Content Should Be Remembered"
  override val description: String = "movableContentOf should be wrapped in remember {}."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.ERROR
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#movable-content-should-be-remembered"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName != "movableContentOf" && calleeName != "movableContentWithReceiverOf") continue

      var parent = call.parent
      var insideRemember = false
      while (parent != null) {
        if (parent is KtCallExpression && parent.calleeExpression?.text == "remember") {
          insideRemember = true
          break
        }
        parent = parent.parent
      }

      if (!insideRemember) {
        violations.add(
          createViolation(
            element = call,
            message = "'$calleeName' should be wrapped in remember {}",
            tooltip = "Movable content needs to be remembered to persist across recompositions.",
            quickFixes = listOf(
              WrapInRememberFix(),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }
}
