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
package io.androidpoet.composeguard.rules.state

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
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Rule: State should be remembered in composables.
 *
 * Using `mutableStateOf` or similar state builders without `remember`
 * will create a new state instance on every recomposition, losing
 * the previous value.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#state-should-be-remembered-in-composables">State Should Be Remembered</a>
 */
public class RememberStateRule : ComposableFunctionRule() {

  override val id: String = "RememberState"

  override val name: String = "State Should Be Remembered"

  override val description: String = """
    mutableStateOf and similar state builders should be wrapped in remember {}
    to persist the state across recompositions.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.ERROR

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#state-should-be-remembered-in-composables"

  private val stateBuilders = setOf(
    "mutableStateOf",
    "mutableIntStateOf",
    "mutableLongStateOf",
    "mutableFloatStateOf",
    "mutableDoubleStateOf",
    "mutableStateListOf",
    "mutableStateMapOf",
    "derivedStateOf",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val properties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)

    for (property in properties) {
      val expression = property.initializer
        ?: property.delegateExpression
        ?: continue

      val callExpression = expression as? KtCallExpression
        ?: PsiTreeUtil.findChildOfType(expression, KtCallExpression::class.java)
        ?: continue

      val calleeName = callExpression.calleeExpression?.text ?: continue

      if (calleeName in stateBuilders) {
        if (!isInsideRemember(callExpression)) {
          val isDelegate = property.delegateExpression != null
          val changeFrom = if (isDelegate) {
            "var ${property.name} by $calleeName(...)"
          } else {
            "val ${property.name} = $calleeName(...)"
          }
          val changeTo = if (isDelegate) {
            "var ${property.name} by remember { $calleeName(...) }"
          } else {
            "val ${property.name} = remember { $calleeName(...) }"
          }

          violations.add(
            createViolation(
              element = callExpression,
              message = "'$calleeName' should be wrapped in remember {}",
              tooltip = """
                State created with $calleeName will be recreated on every recomposition
                without remember {}, losing the previous value.

                Change:
                  $changeFrom

                To:
                  $changeTo
              """.trimIndent(),
              quickFixes = listOf(
                WrapInRememberFix(),
                SuppressComposeRuleFix(id),
              ),
            ),
          )
        }
      }
    }

    return violations
  }

  private fun isInsideRemember(element: KtCallExpression): Boolean {
    var parent = element.parent
    while (parent != null) {
      if (parent is KtCallExpression) {
        val calleeName = parent.calleeExpression?.text
        if (calleeName == "remember" || calleeName == "rememberSaveable") {
          return true
        }
      }
      parent = parent.parent
    }
    return false
  }
}
