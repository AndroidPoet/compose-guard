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
 *     items(users, contentType = { "listItem" }) { UserItem() }
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

  override val category: RuleCategory = RuleCategory.COMPOSABLE

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

    val callExpressions = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (callExpression in callExpressions) {
      val calleeName = callExpression.calleeExpression?.text ?: continue

      if (calleeName in lazyListFunctions) {
        val lambdaExpression = callExpression.lambdaArguments.firstOrNull()?.getLambdaExpression()
          ?: callExpression.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
          ?: continue

        val lambdaBody = lambdaExpression.bodyExpression ?: continue

        val itemCalls = PsiTreeUtil.findChildrenOfType(lambdaBody, KtCallExpression::class.java)
          .filter { it.calleeExpression?.text in lazyItemFunctions }

        if (itemCalls.size >= 2) {
          val hasMultipleTypes = hasMultipleItemTypes(itemCalls)

          if (hasMultipleTypes) {
            val missingContentType = itemCalls.filter { !hasContentTypeParameter(it) }

            if (missingContentType.isNotEmpty()) {
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
                        items(data, contentType = { "listItem" }) { Item(it) }
                        item(contentType = "footer") { Footer() }
                    }

                    Each distinct visual item type should have a unique contentType.
                  """.trimIndent(),
                  quickFixes = listOf(AddContentTypeFix(), SuppressComposeRuleFix(id)),
                ),
              )

              for (itemCall in missingContentType) {
                val itemCalleeName = itemCall.calleeExpression?.text ?: "item"
                violations.add(
                  createViolation(
                    element = itemCall.calleeExpression ?: itemCall,
                    message = "$itemCalleeName() missing contentType parameter",
                    tooltip = """
                      This $itemCalleeName() is part of a heterogeneous $calleeName but is missing
                      the contentType parameter.

                      Add contentType parameter:

                      ${when (itemCalleeName) {
                      "stickyHeader" -> "$itemCalleeName(contentType = \"stickyHeader\") { ... }"
                      "item" -> "$itemCalleeName(contentType = \"header\") { ... }"
                      "items" -> "$itemCalleeName(data, contentType = { \"listItem\" }) { ... }"
                      "itemsIndexed" -> "$itemCalleeName(data, contentType = { _, _ -> \"listItem\" }) { ... }"
                      else -> "$itemCalleeName(contentType = \"item\") { ... }"
                    }}
                    """.trimIndent(),
                    quickFixes = listOf(AddContentTypeFix(), SuppressComposeRuleFix(id)),
                    severity = RuleSeverity.WEAK_WARNING,
                  ),
                )
              }
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
    val itemCount = itemCalls.count { it.calleeExpression?.text == "item" }
    val itemsCount = itemCalls.count { it.calleeExpression?.text == "items" }
    val stickyHeaderCount = itemCalls.count { it.calleeExpression?.text == "stickyHeader" }

    return (itemCount > 0 && itemsCount > 0) ||
      itemCount > 1 ||
      stickyHeaderCount > 0
  }
}
