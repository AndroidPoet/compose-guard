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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.quickfix.ReorderParametersFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.hasDefaultValue
import io.androidpoet.composeguard.rules.isComposableLambda
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Rule: Order composable parameters properly.
 *
 * Parameter order should be:
 * 1. Required parameters (no defaults)
 * 2. Modifier parameter (with default)
 * 3. Optional parameters (with defaults)
 * 4. Content lambda (trailing, with default)
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#ordering-composable-parameters-properly">Ordering Parameters Properly</a>
 */
public class ParameterOrderingRule : ComposableFunctionRule() {

  override val id: String = "ParameterOrdering"

  override val name: String = "Parameter Ordering"

  override val description: String = """
    Composable parameters should be ordered: required → modifier → optional → content.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.PARAMETER

  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#ordering-composable-parameters-properly"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val params = function.valueParameters
    if (params.size <= 1) return emptyList()

    val violations = mutableListOf<ComposeRuleViolation>()

    // Check if required parameters come before optional ones
    var foundOptional = false
    for (param in params) {
      val hasDefault = param.hasDefaultValue()
      val isModifier = isModifierParam(param)
      val isContentLambda = param.isComposableLambda()

      if (!hasDefault && foundOptional && !isModifier && !isContentLambda) {
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Required parameter '${param.name}' should come before optional parameters",
            tooltip = """
              Composable parameters should be ordered:
              1. Required parameters (no defaults)
              2. Modifier parameter (with default)
              3. Optional parameters (with defaults)
              4. Content lambda (trailing, with default)

              Parameter '${param.name}' has no default value but appears after optional parameters.
            """.trimIndent(),
            quickFixes = listOf(
              ReorderParametersFix(),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }

      if (hasDefault && !isModifier && !isContentLambda) {
        foundOptional = true
      }
    }

    // Check if modifier is reasonably early (after required params)
    val modifierIndex = params.indexOfFirst { isModifierParam(it) }
    if (modifierIndex > 0) {
      val paramsBeforeModifier = params.take(modifierIndex)
      val optionalBeforeModifier = paramsBeforeModifier.count { it.hasDefaultValue() && !it.isComposableLambda() }

      if (optionalBeforeModifier > 0) {
        violations.add(
          createViolation(
            element = params[modifierIndex].nameIdentifier ?: params[modifierIndex],
            message = "Modifier parameter should come before other optional parameters",
            tooltip = """
              The modifier parameter should appear after required parameters
              but before other optional parameters.
            """.trimIndent(),
            quickFixes = listOf(
              ReorderParametersFix(),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }

  private fun isModifierParam(param: KtParameter): Boolean {
    val typeName = param.typeReference?.text ?: return false
    return typeName == "Modifier" || typeName.endsWith(".Modifier")
  }
}
