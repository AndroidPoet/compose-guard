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

import io.androidpoet.composeguard.quickfix.RenameParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

public class ModifierNamingRule : ComposableFunctionRule() {

  override val id: String = "ModifierNaming"

  override val name: String = "Modifier Naming Convention"

  override val description: String = """
    Modifier parameters should be named 'modifier' for the main modifier
    or 'xModifier' for sub-component modifiers.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-modifiers-properly"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    val modifierParams = function.valueParameters.filter { param ->
      val typeName = param.typeReference?.text ?: return@filter false
      typeName == "Modifier" || typeName.endsWith(".Modifier")
    }

    if (modifierParams.isEmpty()) return emptyList()

    val firstModifier = modifierParams.first()
    val firstName = firstModifier.name ?: ""

    if (firstName != "modifier") {
      if (!firstName.endsWith("Modifier")) {
        violations.add(
          createViolation(
            element = firstModifier.nameIdentifier ?: firstModifier,
            message = "Modifier parameter '$firstName' should be named 'modifier'",
            tooltip = """
              The main modifier parameter should be named 'modifier'.
              If this is a sub-component modifier, use the pattern 'xModifier'
              (e.g., 'contentModifier', 'iconModifier').

              Current: $firstName: Modifier
              Suggested: modifier: Modifier
            """.trimIndent(),
            quickFixes = listOf(
              RenameParameterFix("modifier"),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    for (param in modifierParams.drop(1)) {
      val paramName = param.name ?: continue

      if (paramName == "modifier") {
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Additional modifier '$paramName' should be named 'xModifier'",
            tooltip = """
              When multiple modifier parameters exist, only the first one should
              be named 'modifier'. Additional modifiers should follow the pattern
              'xModifier' (e.g., 'contentModifier', 'iconModifier') to indicate
              which sub-component they modify.
            """.trimIndent(),
            quickFixes = listOf(
              RenameParameterFix("contentModifier"),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      } else if (!paramName.endsWith("Modifier")) {
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Modifier parameter '$paramName' should follow 'xModifier' naming",
            tooltip = """
              Sub-component modifier parameters should follow the pattern 'xModifier'
              (e.g., 'contentModifier', 'iconModifier') to clearly indicate which
              component they modify.

              Current: $paramName: Modifier
              Suggested: ${paramName}Modifier: Modifier
            """.trimIndent(),
            quickFixes = listOf(
              RenameParameterFix("${paramName}Modifier"),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }
}
