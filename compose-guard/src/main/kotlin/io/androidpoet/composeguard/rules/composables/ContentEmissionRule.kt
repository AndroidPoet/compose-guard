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

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.returnsUnit
import org.jetbrains.kotlin.psi.KtNamedFunction

public class ContentEmissionRule : ComposableFunctionRule() {
  override val id: String = "ContentEmission"
  override val name: String = "Don't Emit Content and Return"
  override val description: String = "Composables should either emit content OR return a value, not both."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-emit-content-and-return-a-result"

  private val layoutComposables = setOf(
    "Box", "Column", "Row", "LazyColumn", "LazyRow", "Card", "Surface", "Scaffold",
    "ConstraintLayout", "FlowRow", "FlowColumn", "LazyVerticalGrid", "LazyHorizontalGrid",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (function.returnsUnit()) return emptyList()

    val bodyText = function.bodyExpression?.text ?: function.bodyBlockExpression?.text ?: return emptyList()
    val emitsContent = layoutComposables.any { bodyText.contains(it) }

    if (emitsContent) {
      val returnType = function.typeReference?.text ?: "non-Unit"
      return listOf(
        createViolation(
          element = function.typeReference ?: function.nameIdentifier ?: function,
          message = "Composable emits content but also returns '$returnType'",
          tooltip = """
          Composable functions should either:
          - Emit UI content to the composition tree (return Unit)
          - Return a value to the caller (no UI emission)

          But not both! This function emits UI content AND returns a value.

          Why this matters:
          - Mixing responsibilities creates confusing APIs
          - Callers may not expect side effects from a function that returns a value
          - Makes testing and reuse more difficult

          Consider splitting into two functions:
          1. A composable that emits UI (returns Unit)
          2. A regular function that computes and returns the value

          Example:
          ❌ @Composable fun BadExample(): String {
               Column { Text("Hello") }
               return "result"
             }

          ✅ @Composable fun GoodEmitter() {
               Column { Text("Hello") }
             }

             fun computeValue(): String = "result"
          """.trimIndent(),
          quickFixes = listOf(SuppressComposeRuleFix(id)),
        ),
      )
    }
    return emptyList()
  }
}
