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

import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Hoist state to parent when appropriate.
 *
 * State hoisting is a pattern where state is moved up to make a composable
 * stateless. This makes the composable easier to test, reuse, and reason about.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#hoist-all-the-things">Hoist All The Things</a>
 */
public class HoistStateRule : ComposableFunctionRule() {

  override val id: String = "HoistState"

  override val name: String = "Consider Hoisting State"

  override val description: String = """
    State management should often be hoisted to parent composables
    to make components stateless and reusable.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.INFO

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#hoist-all-the-things"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    // This rule requires complex analysis of state usage patterns
    // Full implementation would analyze:
    // - Whether state is only used internally
    // - Whether the composable could benefit from being stateless
    // - Whether there are related event callbacks
    //
    // For now, this is a placeholder - full implementation in future PR
    return emptyList()
  }
}
