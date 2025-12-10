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

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Don't use MutableState as a parameter.
 *
 * Passing MutableState as a parameter splits state ownership between
 * the composable and its caller, making it harder to reason about
 * when changes occur.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-use-mutablestate-as-a-parameter">Don't Use MutableState as Parameter</a>
 */
public class MutableStateParameterRule : ComposableFunctionRule() {

  override val id: String = "MutableStateParameter"

  override val name: String = "Don't Use MutableState as Parameter"

  override val description: String = """
    MutableState should not be passed as a parameter. Instead, use
    the value and a callback to update it (state hoisting pattern).
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-use-mutablestate-as-a-parameter"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    for (param in function.valueParameters) {
      val typeText = param.typeReference?.text ?: continue

      if (typeText.contains("MutableState<") || typeText == "MutableState") {
        violations.add(
          createViolation(
            element = param.typeReference ?: param,
            message = "Don't use MutableState<T> as a parameter",
            tooltip = """
              Passing MutableState as a parameter splits state ownership between
              the composable and its caller. Use state hoisting instead.

              Change:
                @Composable
                fun MyComposable(state: MutableState<String>) { ... }

              To:
                @Composable
                fun MyComposable(
                  value: String,
                  onValueChange: (String) -> Unit
                ) { ... }
            """.trimIndent(),
            quickFixes = listOf(SuppressComposeRuleFix(id)),
          ),
        )
      }
    }

    return violations
  }
}
