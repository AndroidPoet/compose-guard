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

  private fun isScrollStateThresholdPattern(element: com.intellij.psi.PsiElement): Boolean {
    val binaryExprs = PsiTreeUtil.findChildrenOfType(element, KtBinaryExpression::class.java)

    for (binaryExpr in binaryExprs) {
      val operationToken = binaryExpr.operationToken.toString()

      if (operationToken !in listOf("GT", "LT", "GTEQ", "LTEQ", "EQEQ", "EXCLEQ")) {
        continue
      }

      val left = binaryExpr.left?.text ?: ""
      val right = binaryExpr.right?.text ?: ""

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

  private fun isFrequentStateComparison(element: com.intellij.psi.PsiElement): Boolean {
    val dotExpressions = PsiTreeUtil.findChildrenOfType(element, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val receiverText = dotExpr.receiverExpression.text
      val selectorText = dotExpr.selectorExpression?.text ?: ""

      val isFrequentState = frequentlyChangingState.any { receiverText.contains(it, ignoreCase = true) }
      val isFrequentProperty = frequentlyChangingProperties.any { selectorText.contains(it) }

      if (isFrequentState && isFrequentProperty) {
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

    ✅ With derivedStateOf:
    val $propertyName by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }

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
