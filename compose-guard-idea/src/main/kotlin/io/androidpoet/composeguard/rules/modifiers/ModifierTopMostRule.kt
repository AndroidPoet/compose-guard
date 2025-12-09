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

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.getModifierParameter
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Modifiers should be used at the top-most layout of a composable.
 *
 * The modifier parameter passed to a composable should be applied to the
 * root layout node, not nested children. This ensures the composable
 * behaves correctly when embedded in different parent layouts.
 *
 * Note: This rule performs a basic check. Full analysis requires
 * tracking modifier usage through the call graph.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-be-used-at-the-top-most-layout-of-the-component">Modifiers at Top-Most Layout</a>
 */
public class ModifierTopMostRule : ComposableFunctionRule() {

  override val id: String = "ModifierTopMost"

  override val name: String = "Modifier at Top-Most Layout"

  override val description: String = """
    The modifier parameter should be applied to the root layout
    of the composable, not to nested children.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-be-used-at-the-top-most-layout-of-the-component"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    // This rule requires deep analysis of modifier usage
    // For now, we check basic patterns - full implementation would
    // require tracking modifier variable usage through the AST

    val modifierParam = function.getModifierParameter() ?: return emptyList()
    val modifierName = modifierParam.name ?: return emptyList()

    val bodyText = function.bodyExpression?.text
      ?: function.bodyBlockExpression?.text
      ?: return emptyList()

    // Count usages of the modifier parameter
    val usageCount = Regex("""\b$modifierName\b""").findAll(bodyText).count()

    if (usageCount == 0) {
      return listOf(
        createViolation(
          element = modifierParam.nameIdentifier ?: modifierParam,
          message = "Modifier parameter '$modifierName' is not used in the composable body",
          tooltip = """
            The modifier parameter should be applied to the root layout.
            Currently, it appears the modifier is not being used at all.

            Make sure to pass the modifier to your root composable:
            Column(modifier = $modifierName) { ... }
          """.trimIndent(),
          quickFixes = listOf(
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    return emptyList()
  }
}
