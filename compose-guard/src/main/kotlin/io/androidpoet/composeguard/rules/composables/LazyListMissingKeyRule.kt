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
import io.androidpoet.composeguard.quickfix.AddKeyParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: LazyList items() should have a key parameter.
 *
 * Without a key parameter, LazyColumn/LazyRow cannot efficiently track
 * item identity across recompositions, leading to unnecessary recompositions
 * and potential issues with state preservation.
 *
 * Example violation:
 * ```kotlin
 * LazyColumn {
 *     items(users) { user ->  // Missing key!
 *         UserItem(user)
 *     }
 * }
 * ```
 *
 * Correct usage:
 * ```kotlin
 * LazyColumn {
 *     items(users, key = { it.id }) { user ->
 *         UserItem(user)
 *     }
 * }
 * ```
 */
public class LazyListMissingKeyRule : ComposableFunctionRule() {

  override val id: String = "LazyListMissingKey"

  override val name: String = "LazyList Missing Key Parameter"

  override val description: String = """
    LazyColumn/LazyRow items() calls should include a key parameter for efficient
    recomposition and proper state preservation.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.COMPOSABLE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/lists#item-keys"

  private val lazyListFunctions = setOf(
    "LazyColumn",
    "LazyRow",
    "LazyVerticalGrid",
    "LazyHorizontalGrid",
    "LazyVerticalStaggeredGrid",
    "LazyHorizontalStaggeredGrid",
  )

  private val itemsFunctions = setOf(
    "items",
    "itemsIndexed",
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
        val lambdaArgument = callExpression.lambdaArguments.firstOrNull() ?: continue
        val lambdaBody = lambdaArgument.getLambdaExpression()?.bodyExpression ?: continue

        val itemsCalls = PsiTreeUtil.findChildrenOfType(lambdaBody, KtCallExpression::class.java)
          .filter { it.calleeExpression?.text in itemsFunctions }

        for (itemsCall in itemsCalls) {
          if (!hasKeyParameter(itemsCall)) {
            val itemsFunctionName = itemsCall.calleeExpression?.text ?: "items"

            violations.add(
              createViolation(
                element = itemsCall.calleeExpression ?: itemsCall,
                message = "'$itemsFunctionName' is missing a key parameter",
                tooltip = """
                  Without a key parameter, $calleeName cannot efficiently track item
                  identity across recompositions. This can cause:

                  - Unnecessary recompositions when the list changes
                  - Loss of item state (scroll position, text input, etc.)
                  - Animation issues when items are added/removed/reordered

                  Add a key parameter that uniquely identifies each item:

                  $itemsFunctionName(items, key = { it.id }) { item ->
                  }

                  The key should be:
                  - Unique for each item
                  - Stable (same item always produces same key)
                  - Cheap to compute
                """.trimIndent(),
                quickFixes = listOf(AddKeyParameterFix(), SuppressComposeRuleFix(id)),
              ),
            )
          }
        }
      }
    }

    for (callExpression in callExpressions) {
      val calleeName = callExpression.calleeExpression?.text ?: continue

      if (calleeName in itemsFunctions && !hasKeyParameter(callExpression)) {
        if (isInsideLazyListScope(callExpression)) {
          violations.add(
            createViolation(
              element = callExpression.calleeExpression ?: callExpression,
              message = "'$calleeName' is missing a key parameter",
              tooltip = """
                Without a key parameter, the LazyList cannot efficiently track item
                identity across recompositions.

                Add a key parameter:
                $calleeName(items, key = { it.id }) { item ->
                }
              """.trimIndent(),
              quickFixes = listOf(AddKeyParameterFix(), SuppressComposeRuleFix(id)),
            ),
          )
        }
      }
    }

    return violations.distinctBy { it.element }
  }

  private fun hasKeyParameter(callExpression: KtCallExpression): Boolean {
    val valueArguments = callExpression.valueArguments

    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      if (argName == "key") {
        return true
      }
    }

    if (valueArguments.size >= 2) {
      val secondArg = valueArguments[1]
      val secondArgName = secondArg.getArgumentName()?.asName?.asString()
      if (secondArgName == null || secondArgName == "key") {
        return true
      }
    }

    return false
  }

  private fun isInsideLazyListScope(element: KtCallExpression): Boolean {
    var parent = element.parent
    while (parent != null) {
      if (parent is KtCallExpression) {
        val calleeName = parent.calleeExpression?.text
        if (calleeName in lazyListFunctions) {
          return true
        }
      }
      parent = parent.parent
    }
    return false
  }
}
