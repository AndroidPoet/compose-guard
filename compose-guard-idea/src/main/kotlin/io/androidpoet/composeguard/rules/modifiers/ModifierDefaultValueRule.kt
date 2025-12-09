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

import io.androidpoet.composeguard.quickfix.AddModifierDefaultValueFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.getModifierParameter
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Modifier parameters should have a default value of Modifier.
 *
 * This allows callers to optionally provide a modifier without requiring
 * one in all call sites.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-have-default-parameters">Modifiers Should Have Default Parameters</a>
 */
public class ModifierDefaultValueRule : ComposableFunctionRule() {

  override val id: String = "ModifierDefaultValue"

  override val name: String = "Modifier Should Have Default Value"

  override val description: String = """
    Modifier parameters should have a default value of Modifier
    to allow callers to optionally provide one.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-have-default-parameters"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val modifierParam = function.getModifierParameter() ?: return emptyList()

    // Check if modifier has a default value
    val defaultValue = modifierParam.defaultValue
    if (defaultValue != null) {
      return emptyList()
    }

    return listOf(
      createViolation(
        element = modifierParam.nameIdentifier ?: modifierParam,
        message = "Modifier parameter should have default value '= Modifier'",
        tooltip = """
          The modifier parameter should have a default value to make it optional.
          This allows callers to omit the modifier in common cases.

          Change: modifier: Modifier
          To: modifier: Modifier = Modifier
        """.trimIndent(),
        quickFixes = listOf(
          AddModifierDefaultValueFix(),
          SuppressComposeRuleFix(id),
        ),
      ),
    )
  }
}
