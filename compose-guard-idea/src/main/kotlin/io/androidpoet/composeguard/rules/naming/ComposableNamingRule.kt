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
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.quickfix.RenameComposableFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.returnsUnit
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Composable functions should follow naming conventions.
 *
 * - Unit-returning composables should use PascalCase (e.g., `UserCard`, `HomeScreen`)
 * - Value-returning composables should use camelCase (e.g., `rememberScrollState`)
 *
 * This convention helps distinguish between composables that emit UI
 * (treated like UI components/entities) and those that return values
 * (treated like functions).
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#naming-composable-functions-properly">Naming Composable Functions Properly</a>
 */
public class ComposableNamingRule : ComposableFunctionRule() {

  override val id: String = "ComposableNaming"

  override val name: String = "Composable Naming Convention"

  override val description: String = """
    Unit-returning composables should use PascalCase (e.g., UserCard).
    Value-returning composables should use camelCase (e.g., rememberScrollState).
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.NAMING

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-composable-functions-properly"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val name = function.name ?: return emptyList()
    if (name.isEmpty()) return emptyList()

    val firstChar = name.first()
    val isUpperCase = firstChar.isUpperCase()
    val returnsUnit = function.returnsUnit()

    val violations = mutableListOf<ComposeRuleViolation>()

    if (returnsUnit && !isUpperCase) {
      // Unit-returning composable should start with uppercase
      val suggestedName = RenameComposableFix.toPascalCase(name)
      violations.add(
        createViolation(
          element = function.nameIdentifier ?: function,
          message = "Composable '$name' should start with uppercase (PascalCase)",
          tooltip = """
            Unit-returning @Composable functions should be named using PascalCase
            because they represent UI components/entities.

            Example: Change '$name' to '$suggestedName'
          """.trimIndent(),
          quickFixes = listOf(
            RenameComposableFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    } else if (!returnsUnit && isUpperCase) {
      // Value-returning composable should start with lowercase
      val suggestedName = RenameComposableFix.toCamelCase(name)
      violations.add(
        createViolation(
          element = function.nameIdentifier ?: function,
          message = "Value-returning composable '$name' should start with lowercase",
          tooltip = """
            Composables that return values should use camelCase naming
            because they represent computations/factories, not UI components.

            Example: Change '$name' to '$suggestedName'
          """.trimIndent(),
          quickFixes = listOf(
            RenameComposableFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    return violations
  }
}
