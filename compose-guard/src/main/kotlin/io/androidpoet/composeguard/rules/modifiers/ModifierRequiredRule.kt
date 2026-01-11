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
package io.androidpoet.composeguard.rules.modifiers

import io.androidpoet.composeguard.quickfix.AddModifierParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.hasModifierParameter
import io.androidpoet.composeguard.rules.isPreview
import io.androidpoet.composeguard.rules.isPublic
import io.androidpoet.composeguard.rules.returnsUnit
import org.jetbrains.kotlin.psi.KtNamedFunction

public class ModifierRequiredRule : ComposableFunctionRule() {

  override val id: String = "ModifierRequired"

  override val name: String = "Modifier Parameter Required"

  override val description: String = """
    Public composables that emit UI should accept a Modifier parameter
    to allow callers to customize appearance and behavior.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#when-should-i-expose-modifier-parameters"

  override fun shouldAnalyze(function: KtNamedFunction): Boolean {
    if (function.isPreview()) return false

    if (!function.isPublic()) return false

    if (!function.returnsUnit()) return false

    return true
  }

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    if (function.hasModifierParameter()) {
      return emptyList()
    }

    return listOf(
      createViolation(
        element = function.nameIdentifier ?: function,
        message = "Public composable '${function.name}' should have a modifier parameter",
        tooltip = """
          Public @Composable functions that emit UI should accept a Modifier parameter.
          This enables callers to customize the composable's appearance and behavior
          using the composition-over-inheritance pattern.

          Add: modifier: Modifier = Modifier
        """.trimIndent(),
        quickFixes = listOf(
          AddModifierParameterFix(),
          SuppressComposeRuleFix(id),
        ),
      ),
    )
  }
}
