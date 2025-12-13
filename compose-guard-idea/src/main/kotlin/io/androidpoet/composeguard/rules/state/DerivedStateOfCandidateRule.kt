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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.WrapInDerivedStateOfFix
import io.androidpoet.composeguard.quickfix.WrapInRememberFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Rule: Detect candidates for derivedStateOf or remember with keys.
 *
 * KEY INSIGHT: Use derivedStateOf when input state changes MORE frequently
 * than the derived output changes.
 *
 * Classic example:
 * ```kotlin
 * // BAD: Recomposes on every scroll frame
 * val showButton = scrollState.firstVisibleItemIndex > 0
 *
 * // GOOD: Only recomposes when showButton changes (true ↔ false)
 * val showButton by remember {
 *     derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
 * }
 * ```
 *
 * When to use derivedStateOf:
 * - Threshold checks on scroll state (position > 0, offset > threshold)
 * - Boolean derivations from frequently changing values
 * - Expensive computations on state that produce cached results
 *
 * When to use remember(keys):
 * - Computations based on function parameters
 * - Values that should update when specific inputs change
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/side-effects#derivedstateof">derivedStateOf</a>
 */
public class DerivedStateOfCandidateRule : ComposableFunctionRule() {

  override val id: String = "DerivedStateOfCandidate"

  override val name: String = "Consider Using remember with keys"

  override val description: String = """
    Use derivedStateOf when input changes more frequently than output.
    Use remember(keys) when computation depends on function parameters.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/side-effects#derivedstateof"

  // State holders that change frequently (good candidates for derivedStateOf)
  private val frequentlyChangingState = setOf(
    "scrollState",
    "lazyListState",
    "lazyGridState",
    "pagerState",
    "drawerState",
    "sheetState",
    "swipeableState",
    "dismissState",
    "animatedFloat",
    "animatedValue",
    "Animatable",
  )

  // Properties that indicate frequently changing values
  private val frequentlyChangingProperties = setOf(
    "firstVisibleItemIndex",
    "firstVisibleItemScrollOffset",
    "value",
    "offset",
    "progress",
    "currentValue",
    "targetValue",
    "scrollOffset",
    "layoutInfo",
  )

  // Collection transformation operations that are candidates for remember
  private val expensiveOperations = setOf(
    "filter", "filterNot", "filterIsInstance", "filterNotNull",
    "map", "mapNotNull", "mapIndexed", "flatMap", "flatten",
    "sorted", "sortedBy", "sortedByDescending", "sortedWith",
    "reversed", "distinct", "distinctBy",
    "groupBy", "groupingBy", "partition",
    "chunked", "windowed", "zip", "zipWithNext",
    "associate", "associateBy", "associateWith",
    "toList", "toSet", "toMap", "toMutableList", "toMutableSet", "toSortedSet",
    "reduce", "reduceOrNull", "fold", "foldRight",
    "sumOf", "sumBy", "count", "maxOf", "maxOfOrNull", "minOf", "minOfOrNull",
    "joinToString",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val properties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)

    for (property in properties) {
      if (isAlreadyOptimized(property)) continue

      val initializer = property.initializer ?: continue
      val propertyName = property.name ?: "value"

      // Check for scroll state threshold patterns (PERFECT for derivedStateOf)
      if (isScrollStateThresholdPattern(initializer)) {
        violations.add(
          createViolation(
            element = initializer,
            message = "Use derivedStateOf for '$propertyName' - scroll state threshold check",
            tooltip = buildScrollStateTooltip(propertyName),
            quickFixes = listOf(WrapInDerivedStateOfFix(), SuppressComposeRuleFix(id)),
          ),
        )
        continue
      }

      // Check for comparisons on frequently changing state
      if (isFrequentStateComparison(initializer)) {
        violations.add(
          createViolation(
            element = initializer,
            message = "Use derivedStateOf for '$propertyName' - reduces recomposition frequency",
            tooltip = buildDerivedStateTooltip(propertyName),
            quickFixes = listOf(WrapInDerivedStateOfFix(), SuppressComposeRuleFix(id)),
          ),
        )
        continue
      }

      // Check for expensive operations (candidates for remember with keys)
      if (containsExpensiveOperation(initializer)) {
        val operationName = findExpensiveOperationName(initializer)
        violations.add(
          createViolation(
            element = initializer,
            message = "Consider using remember for computed value '$propertyName'",
            tooltip = buildRememberTooltip(propertyName, operationName),
            quickFixes = listOf(WrapInRememberFix(), SuppressComposeRuleFix(id)),
          ),
        )
      }
    }

    return violations
  }

  /**
   * Detects patterns like: scrollState.firstVisibleItemIndex > 0
   * These are PERFECT candidates for derivedStateOf because:
   * - Input (scroll offset) changes on every frame
   * - Output (boolean) only changes when crossing the threshold
   */
  private fun isScrollStateThresholdPattern(element: com.intellij.psi.PsiElement): Boolean {
    // Look for binary comparison expressions
    val binaryExprs = PsiTreeUtil.findChildrenOfType(element, KtBinaryExpression::class.java)

    for (binaryExpr in binaryExprs) {
      val operationToken = binaryExpr.operationToken.toString()

      // Check for comparison operators
      if (operationToken !in listOf("GT", "LT", "GTEQ", "LTEQ", "EQEQ", "EXCLEQ")) {
        continue
      }

      val left = binaryExpr.left?.text ?: ""
      val right = binaryExpr.right?.text ?: ""

      // Check if either side references scroll state properties
      val leftIsScrollState = frequentlyChangingState.any { left.contains(it, ignoreCase = true) } &&
        frequentlyChangingProperties.any { left.contains(it) }

      val rightIsScrollState = frequentlyChangingState.any { right.contains(it, ignoreCase = true) } &&
        frequentlyChangingProperties.any { right.contains(it) }

      if (leftIsScrollState || rightIsScrollState) {
        return true
      }
    }

    return false
  }

  /**
   * Detects comparisons on frequently changing state that produce booleans.
   */
  private fun isFrequentStateComparison(element: com.intellij.psi.PsiElement): Boolean {
    val dotExpressions = PsiTreeUtil.findChildrenOfType(element, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val receiverText = dotExpr.receiverExpression.text
      val selectorText = dotExpr.selectorExpression?.text ?: ""

      // Check for frequently changing state access
      val isFrequentState = frequentlyChangingState.any { receiverText.contains(it, ignoreCase = true) }
      val isFrequentProperty = frequentlyChangingProperties.any { selectorText.contains(it) }

      if (isFrequentState && isFrequentProperty) {
        // Check if this is part of a comparison (produces boolean output)
        val parent = dotExpr.parent
        if (parent is KtBinaryExpression) {
          val op = parent.operationToken.toString()
          if (op in listOf("GT", "LT", "GTEQ", "LTEQ", "EQEQ", "EXCLEQ")) {
            return true
          }
        }
      }
    }

    return false
  }

  private fun isAlreadyOptimized(property: KtProperty): Boolean {
    val initializer = property.initializer ?: property.delegateExpression ?: return false

    if (initializer is KtCallExpression) {
      val calleeName = initializer.calleeExpression?.text
      if (calleeName == "remember" || calleeName == "rememberSaveable") {
        return true
      }
    }

    val text = initializer.text
    if (text.contains("derivedStateOf") || text.contains("remember")) {
      return true
    }

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
    val dotExpressions = PsiTreeUtil.findChildrenOfType(element, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val callExpr = dotExpr.selectorExpression as? KtCallExpression ?: continue
      val methodName = callExpr.calleeExpression?.text ?: continue

      if (methodName in expensiveOperations) {
        return true
      }
    }

    val callExpressions = PsiTreeUtil.findChildrenOfType(element, KtCallExpression::class.java)
    for (callExpr in callExpressions) {
      val methodName = callExpr.calleeExpression?.text ?: continue
      if (methodName in expensiveOperations) {
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

      if (methodName in expensiveOperations) {
        return methodName
      }
    }

    val callExpressions = PsiTreeUtil.findChildrenOfType(element, KtCallExpression::class.java)
    for (callExpr in callExpressions) {
      val methodName = callExpr.calleeExpression?.text ?: continue
      if (methodName in expensiveOperations) {
        return methodName
      }
    }

    return "computation"
  }

  private fun buildScrollStateTooltip(propertyName: String): String = """
    This is a scroll state threshold check - a PERFECT candidate for derivedStateOf!

    The KEY insight: Input (scroll position) changes on every frame, but
    output (boolean) only changes when crossing the threshold.

    ❌ Without derivedStateOf:
    val $propertyName = scrollState.firstVisibleItemIndex > 0
    // Triggers recomposition on EVERY scroll frame!

    ✅ With derivedStateOf:
    val $propertyName by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }
    // Only triggers recomposition when $propertyName changes (true ↔ false)

    This dramatically reduces unnecessary recompositions during scrolling.
  """.trimIndent()

  private fun buildDerivedStateTooltip(propertyName: String): String = """
    This value is derived from frequently changing state.

    Use derivedStateOf when: input changes MORE frequently than output.

    val $propertyName by remember {
        derivedStateOf { /* your computation */ }
    }

    derivedStateOf:
    - Caches the computed result
    - Only triggers recomposition when the DERIVED value changes
    - Perfect for threshold checks, boolean derivations, etc.
  """.trimIndent()

  private fun buildRememberTooltip(propertyName: String, operationName: String): String = """
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
    - derivedStateOf - recalculates when read state changes, recomposes only when result changes
  """.trimIndent()
}
