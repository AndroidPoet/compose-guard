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
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Rule: Computed values derived from state should use derivedStateOf.
 *
 * When you have a value that is computed from state, it recalculates on every
 * recomposition even if the underlying state hasn't changed. Using derivedStateOf
 * caches the computed value and only recalculates when the state it reads changes.
 *
 * Example violation:
 * ```kotlin
 * @Composable
 * fun FilteredList(items: List<Item>, searchQuery: String) {
 *     // This filters on EVERY recomposition, even if items/searchQuery unchanged!
 *     val filteredItems = items.filter { it.name.contains(searchQuery) }
 *     LazyColumn {
 *         items(filteredItems) { ... }
 *     }
 * }
 * ```
 *
 * Correct usage:
 * ```kotlin
 * @Composable
 * fun FilteredList(items: List<Item>, searchQuery: String) {
 *     val filteredItems = remember(items, searchQuery) {
 *         items.filter { it.name.contains(searchQuery) }
 *     }
 * }
 * ```
 */
public class DerivedStateOfCandidateRule : ComposableFunctionRule() {

  override val id: String = "DerivedStateOfCandidate"

  override val name: String = "Consider Using remember with keys"

  override val description: String = """
    Computed values derived from parameters should use remember with keys
    to avoid unnecessary recalculations on every recomposition.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.EXPERIMENTAL

  override val severity: RuleSeverity = RuleSeverity.INFO

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/side-effects#derivedstateof"

  // Collection transformation operations that are candidates for remember
  private val expensiveOperations = setOf(
    "filter",
    "filterNot",
    "filterIsInstance",
    "filterNotNull",
    "map",
    "mapNotNull",
    "mapIndexed",
    "flatMap",
    "flatten",
    "sorted",
    "sortedBy",
    "sortedByDescending",
    "sortedWith",
    "reversed",
    "distinct",
    "distinctBy",
    "groupBy",
    "groupingBy",
    "partition",
    "chunked",
    "windowed",
    "zip",
    "zipWithNext",
    "associate",
    "associateBy",
    "associateWith",
    "toList",
    "toSet",
    "toMap",
    "toMutableList",
    "toMutableSet",
    "toSortedSet",
    "reduce",
    "reduceOrNull",
    "fold",
    "foldRight",
    "sumOf",
    "sumBy",
    "count",
    "maxOf",
    "maxOfOrNull",
    "minOf",
    "minOfOrNull",
    "joinToString",
  )

  // String operations that might be expensive
  private val stringOperations = setOf(
    "format",
    "replace",
    "replaceFirst",
    "split",
    "trim",
    "uppercase",
    "lowercase",
    "capitalize",
    "decapitalize",
    "padStart",
    "padEnd",
    "substring",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Find local variable declarations
    val properties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)

    for (property in properties) {
      // Skip properties that are already using remember or derivedStateOf
      if (isAlreadyOptimized(property)) {
        continue
      }

      val initializer = property.initializer ?: continue

      // Check if the initializer contains expensive operations
      if (containsExpensiveOperation(initializer)) {
        val operationName = findExpensiveOperationName(initializer)
        val propertyName = property.name ?: "value"

        violations.add(
          createViolation(
            element = initializer,
            message = "Consider using remember with keys for computed value '$propertyName'",
            tooltip = """
              This value is computed using '$operationName' which runs on every
              recomposition, even if the input data hasn't changed.

              For computations based on parameters, use remember with keys:

              val $propertyName = remember(param1, param2) {
                  // Your computation here
              }

              For computations reading state (.value), use derivedStateOf:

              val $propertyName by remember {
                  derivedStateOf { stateA.value + stateB.value }
              }

              Key differences:
              - remember(keys) - recalculates when keys change
              - derivedStateOf - recalculates when read state changes
            """.trimIndent(),
            quickFixes = listOf(SuppressComposeRuleFix(id)),
          ),
        )
      }
    }

    return violations
  }

  private fun isAlreadyOptimized(property: KtProperty): Boolean {
    val initializer = property.initializer ?: property.delegateExpression ?: return false

    // Check if wrapped in remember
    if (initializer is KtCallExpression) {
      val calleeName = initializer.calleeExpression?.text
      if (calleeName == "remember" || calleeName == "rememberSaveable") {
        return true
      }
    }

    // Check if using derivedStateOf
    val text = initializer.text
    if (text.contains("derivedStateOf") || text.contains("remember")) {
      return true
    }

    // Check delegate expression (by remember { })
    val delegate = property.delegateExpression
    if (delegate != null) {
      val delegateText = delegate.text
      if (delegateText.contains("remember") || delegateText.contains("derivedStateOf")) {
        return true
      }
    }

    return false
  }

  private fun containsExpensiveOperation(element: com.intellij.psi.PsiElement): Boolean {
    // Check for chained operations like list.filter { }.map { }
    val dotExpressions = PsiTreeUtil.findChildrenOfType(element, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val callExpr = dotExpr.selectorExpression as? KtCallExpression ?: continue
      val methodName = callExpr.calleeExpression?.text ?: continue

      if (methodName in expensiveOperations || methodName in stringOperations) {
        return true
      }
    }

    // Check for direct call expressions
    val callExpressions = PsiTreeUtil.findChildrenOfType(element, KtCallExpression::class.java)
    for (callExpr in callExpressions) {
      val methodName = callExpr.calleeExpression?.text ?: continue
      if (methodName in expensiveOperations || methodName in stringOperations) {
        return true
      }
    }

    return false
  }

  private fun findExpensiveOperationName(element: com.intellij.psi.PsiElement): String {
    val dotExpressions = PsiTreeUtil.findChildrenOfType(element, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val callExpr = dotExpr.selectorExpression as? KtCallExpression ?: continue
      val methodName = callExpr.calleeExpression?.text ?: continue

      if (methodName in expensiveOperations || methodName in stringOperations) {
        return methodName
      }
    }

    val callExpressions = PsiTreeUtil.findChildrenOfType(element, KtCallExpression::class.java)
    for (callExpr in callExpressions) {
      val methodName = callExpr.calleeExpression?.text ?: continue
      if (methodName in expensiveOperations || methodName in stringOperations) {
        return methodName
      }
    }

    return "computation"
  }
}
