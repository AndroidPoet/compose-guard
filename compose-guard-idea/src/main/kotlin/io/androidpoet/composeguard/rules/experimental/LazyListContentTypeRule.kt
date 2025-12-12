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
package io.androidpoet.composeguard.rules.experimental

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.AddContentTypeFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: LazyList with multiple item types should use contentType.
 *
 * When a LazyColumn/LazyRow contains different types of items (heterogeneous list),
 * using contentType helps Compose reuse compositions more efficiently.
 *
 * Example violation:
 * ```kotlin
 * LazyColumn {
 *     item { Header() }           // Type 1
 *     items(users) { UserItem() } // Type 2
 *     item { Footer() }           // Type 3
 *     // Missing contentType!
 * }
 * ```
 *
 * Correct usage:
 * ```kotlin
 * LazyColumn {
 *     item(contentType = "header") { Header() }
 *     items(users, contentType = { "user" }) { UserItem() }
 *     item(contentType = "footer") { Footer() }
 * }
 * ```
 */
public class LazyListContentTypeRule : ComposableFunctionRule() {

  override val id: String = "LazyListContentType"

  override val name: String = "LazyList Missing ContentType"

  override val description: String = """
    LazyColumn/LazyRow with heterogeneous items should use contentType parameter
    for efficient composition reuse.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.EXPERIMENTAL

  override val severity: RuleSeverity = RuleSeverity.INFO

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/lists#content-type"

  private val lazyListFunctions = setOf(
    "LazyColumn",
    "LazyRow",
    "LazyVerticalGrid",
    "LazyHorizontalGrid",
    "LazyVerticalStaggeredGrid",
    "LazyHorizontalStaggeredGrid",
  )

  private val lazyItemFunctions = setOf(
    "item",
    "items",
    "itemsIndexed",
    "stickyHeader",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Find all LazyList calls
    val callExpressions = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (callExpression in callExpressions) {
      val calleeName = callExpression.calleeExpression?.text ?: continue

      if (calleeName in lazyListFunctions) {
        // Find the lambda body of the LazyList
        val lambdaArgument = callExpression.lambdaArguments.firstOrNull()
          ?: callExpression.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
          ?: continue

        val lambdaBody = when (lambdaArgument) {
          is KtLambdaExpression -> lambdaArgument.bodyExpression
          else -> (lambdaArgument as? com.intellij.psi.PsiElement)
            ?.let { PsiTreeUtil.findChildOfType(it, KtLambdaExpression::class.java) }
            ?.bodyExpression
        } ?: continue

        // Find all item/items calls in the LazyList scope
        val itemCalls = PsiTreeUtil.findChildrenOfType(lambdaBody, KtCallExpression::class.java)
          .filter { it.calleeExpression?.text in lazyItemFunctions }

        // Check if this is a heterogeneous list (multiple different item types)
        if (itemCalls.size >= 2) {
          val hasMultipleTypes = hasMultipleItemTypes(itemCalls)

          if (hasMultipleTypes) {
            // Check which item calls are missing contentType
            val missingContentType = itemCalls.filter { !hasContentTypeParameter(it) }

            if (missingContentType.isNotEmpty()) {
              // Report violation on the LazyList itself
              violations.add(
                createViolation(
                  element = callExpression.calleeExpression ?: callExpression,
                  message = "$calleeName has heterogeneous items without contentType",
                  tooltip = """
                    This $calleeName contains ${itemCalls.size} different item definitions
                    but ${missingContentType.size} of them are missing the contentType parameter.

                    Using contentType helps Compose:
                    - Reuse compositions more efficiently
                    - Avoid recomposing items of different types
                    - Improve scrolling performance

                    Add contentType to each item type:

                    $calleeName {
                        item(contentType = "header") { Header() }
                        items(data, contentType = { "item" }) { Item(it) }
                        item(contentType = "footer") { Footer() }
                    }

                    Each distinct visual item type should have a unique contentType.
                  """.trimIndent(),
                  quickFixes = listOf(AddContentTypeFix(), SuppressComposeRuleFix(id)),
                ),
              )
            }
          }
        }
      }
    }

    return violations
  }

  private fun hasContentTypeParameter(callExpression: KtCallExpression): Boolean {
    return callExpression.valueArguments.any { arg ->
      arg.getArgumentName()?.asName?.asString() == "contentType"
    }
  }

  private fun hasMultipleItemTypes(itemCalls: List<KtCallExpression>): Boolean {
    // Simple heuristic: if there are both item() and items() calls,
    // or multiple item() calls, it's likely heterogeneous
    val itemCount = itemCalls.count { it.calleeExpression?.text == "item" }
    val itemsCount = itemCalls.count { it.calleeExpression?.text == "items" }
    val stickyHeaderCount = itemCalls.count { it.calleeExpression?.text == "stickyHeader" }

    // Heterogeneous if:
    // - Has both item() and items()
    // - Has multiple item() calls
    // - Has stickyHeader
    return (itemCount > 0 && itemsCount > 0) ||
      itemCount > 1 ||
      stickyHeaderCount > 0
  }
}
