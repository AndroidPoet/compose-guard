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

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Do not emit multiple pieces of content.
 *
 * A composable function should emit zero or one layout nodes at the top level.
 * Each composable should be cohesive and not depend on its call site.
 *
 * Emitting multiple content nodes makes the composable dependent on its parent
 * layout (Column, Row, Box) which breaks encapsulation.
 *
 * Exception: Extension functions on layout scopes (ColumnScope, RowScope) are allowed.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/rules/#do-not-emit-multiple-pieces-of-content">Do not emit multiple pieces of content</a>
 */
public class MultipleContentRule : ComposableFunctionRule() {
  override val id: String = "MultipleContentEmitters"
  override val name: String = "Multiple Content Emitters"
  override val description: String = "Composable functions should emit only one piece of content at the top level."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/rules/#do-not-emit-multiple-pieces-of-content"

  // Known content-emitting composables
  private val contentEmitters = setOf(
    // Foundation layouts
    "Box", "Column", "Row", "Surface", "Card", "Scaffold",
    // Lazy layouts
    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
    // Basic content
    "Text", "Icon", "Image", "Button", "IconButton", "TextButton", "OutlinedButton",
    "TextField", "OutlinedTextField", "Checkbox", "RadioButton", "Switch", "Slider",
    // Other layouts
    "ConstraintLayout", "FlowRow", "FlowColumn", "BoxWithConstraints",
    "Divider", "Spacer", "Canvas",
    // Dialogs
    "AlertDialog", "Dialog",
    // Navigation
    "NavigationRail", "NavigationBar", "TopAppBar", "BottomAppBar",
  )

  // Layout scope receiver types that allow multiple emissions
  private val allowedReceiverTypes = setOf(
    "ColumnScope", "RowScope", "BoxScope", "LazyListScope", "LazyItemScope",
    "LazyGridScope", "LazyGridItemScope", "FlowRowScope", "FlowColumnScope",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // Skip if function has a receiver type that allows multiple emissions
    val receiverType = function.receiverTypeReference?.text
    if (receiverType != null && allowedReceiverTypes.any { receiverType.contains(it) }) {
      return emptyList()
    }

    // Need a block body to analyze
    val body = function.bodyBlockExpression ?: return emptyList()

    // Count direct content emissions at the top level of the function body
    val topLevelEmissions = countTopLevelEmissions(body)

    if (topLevelEmissions > 1) {
      return listOf(
        createViolation(
          element = function.nameIdentifier ?: function,
          message = "Composable emits multiple pieces of content ($topLevelEmissions emitters found)",
          tooltip = """
            Composable functions should emit only one piece of content at the top level.

            Emitting multiple content nodes makes the composable dependent on its
            parent layout (Column, Row, Box) which breaks encapsulation.

            ❌ Bad - emits multiple content nodes:
            @Composable
            fun InnerContent() {
                Text("Hello")
                Image(...)
                Button(...)
            }

            ✅ Good - wrap in a single container:
            @Composable
            fun InnerContent() {
                Column {
                    Text("Hello")
                    Image(...)
                    Button(...)
                }
            }

            Exception: Extension functions on layout scopes are allowed:
            @Composable
            fun ColumnScope.InnerContent() {
                Text("Hello")
                Image(...)
            }
          """.trimIndent(),
          quickFixes = listOf(SuppressComposeRuleFix(id)),
        ),
      )
    }

    return emptyList()
  }

  /**
   * Counts direct content-emitting composable calls at the top level of a block.
   * Does not count emissions inside lambdas (those are nested, not top-level).
   */
  private fun countTopLevelEmissions(body: KtBlockExpression): Int {
    var count = 0

    // Get direct children call expressions (not nested in lambdas)
    val directCalls = PsiTreeUtil.getChildrenOfType(body, KtCallExpression::class.java) ?: return 0

    for (call in directCalls) {
      val callName = call.calleeExpression?.text ?: continue

      // Check if it's a known content emitter
      if (callName in contentEmitters) {
        count++
      } else if (callName.first().isUpperCase()) {
        // Uppercase function names are likely composables that emit content
        // But only if they don't have a trailing lambda (which would make them containers)
        if (call.lambdaArguments.isEmpty()) {
          count++
        }
      }
    }

    return count
  }
}
