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
package io.androidpoet.composeguard.rules.performance

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseLambdaModifierFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

public class DeferStateReadsRule : ComposableFunctionRule() {

  override val id: String = "DeferStateReads"

  override val name: String = "Defer State Reads"

  override val description: String = """
    Use lambda-based modifiers for frequently changing state to reduce recompositions.
    Deferring state reads postpones evaluation until the layout or draw phase.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/performance#defer-reads"

  private val modifiersWithLambdaAlternatives = mapOf(
    "offset" to LambdaAlternative(
      lambdaVersion = "offset { IntOffset(x, y) }",
      paramsToCheck = setOf("x", "y"),
      suggestion = "Use Modifier.offset { IntOffset(x, y) } to defer the read",
    ),
    "padding" to LambdaAlternative(
      lambdaVersion = "padding { PaddingValues(...) }",
      paramsToCheck = setOf("all", "horizontal", "vertical", "start", "top", "end", "bottom"),
      suggestion = "Consider using graphicsLayer or layout modifier for animated padding",
    ),
    "alpha" to LambdaAlternative(
      lambdaVersion = "graphicsLayer { alpha = value }",
      paramsToCheck = setOf("alpha"),
      suggestion = "Use Modifier.graphicsLayer { alpha = value } to defer the read",
    ),
    "rotate" to LambdaAlternative(
      lambdaVersion = "graphicsLayer { rotationZ = degrees }",
      paramsToCheck = setOf("degrees"),
      suggestion = "Use Modifier.graphicsLayer { rotationZ = degrees } to defer the read",
    ),
    "scale" to LambdaAlternative(
      lambdaVersion = "graphicsLayer { scaleX = scale; scaleY = scale }",
      paramsToCheck = setOf("scale", "scaleX", "scaleY"),
      suggestion = "Use Modifier.graphicsLayer { scaleX/scaleY = value } to defer the read",
    ),
    "size" to LambdaAlternative(
      lambdaVersion = "layout { ... }",
      paramsToCheck = setOf("size", "width", "height"),
      suggestion = "Use Modifier.layout { ... } for animated size changes",
    ),
  )

  private val frequentlyChangingStatePatterns = setOf(
    "animateFloatAsState",
    "animateDpAsState",
    "animateIntAsState",
    "animateOffsetAsState",
    "animateSizeAsState",
    "animateColorAsState",
    "animateValueAsState",
    "Animatable",
    "scrollState",
    "lazyListState",
    "pagerState",
    "derivedStateOf",
  )

  private val animatedPropertyPatterns = setOf(
    "offset", "scroll", "position", "alpha", "scale", "rotation", "angle",
    "progress", "fraction", "animated", "transition", "x", "y", "dx", "dy",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val frequentlyChangingState = findFrequentlyChangingState(body)

    val dotExpressions = PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      checkModifierChain(dotExpr, frequentlyChangingState, violations)
    }

    return violations
  }

  private fun findFrequentlyChangingState(body: com.intellij.psi.PsiElement): Set<String> {
    val stateNames = mutableSetOf<String>()

    val properties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)
    for (property in properties) {
      val name = property.name ?: continue
      val initializerText = property.initializer?.text ?: ""

      val isAnimatedState = frequentlyChangingStatePatterns.any { pattern ->
        initializerText.contains(pattern, ignoreCase = true)
      }

      val hasAnimatedName = animatedPropertyPatterns.any { pattern ->
        name.contains(pattern, ignoreCase = true)
      }

      if (isAnimatedState || hasAnimatedName) {
        stateNames.add(name)
      }
    }

    return stateNames
  }

  private fun checkModifierChain(
    dotExpr: KtDotQualifiedExpression,
    frequentlyChangingState: Set<String>,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    val text = dotExpr.text

    if (!text.contains("Modifier")) return

    for ((modifierName, alternative) in modifiersWithLambdaAlternatives) {
      val nonLambdaPattern = Regex("""\.$modifierName\s*\((?!\s*\{)([^)]+)\)""")
      val matches = nonLambdaPattern.findAll(text)

      for (match in matches) {
        val args = match.groupValues.getOrNull(1) ?: continue

        val usesFrequentState = frequentlyChangingState.any { stateName ->
          args.contains(stateName)
        }

        val usesDpOrSp = args.contains(".dp") || args.contains(".sp")

        if (usesFrequentState || (usesDpOrSp && hasStateReference(args, frequentlyChangingState))) {
          val callExpr = findModifierCall(dotExpr, modifierName)
          if (callExpr != null) {
            violations.add(
              createViolation(
                element = callExpr,
                message = "Consider deferring state read in .$modifierName() for better performance",
                tooltip = buildTooltip(modifierName, alternative),
                quickFixes = listOf(
                  UseLambdaModifierFix(modifierName, alternative.lambdaVersion),
                  SuppressComposeRuleFix(id),
                ),
              ),
            )
          }
        }
      }
    }
  }

  private fun hasStateReference(args: String, frequentlyChangingState: Set<String>): Boolean {
    return frequentlyChangingState.any { stateName ->
      args.contains(stateName)
    }
  }

  private fun findModifierCall(dotExpr: KtDotQualifiedExpression, modifierName: String): KtCallExpression? {
    val calls = PsiTreeUtil.findChildrenOfType(dotExpr, KtCallExpression::class.java)
    return calls.find { it.calleeExpression?.text == modifierName }
  }

  private fun buildTooltip(modifierName: String, alternative: LambdaAlternative): String {
    return """
      ${alternative.suggestion}

      Reading state during composition causes recomposition whenever
      the state changes. For frequently changing values (animations,
      scroll offsets), this can cause performance issues.

      Lambda-based modifiers defer the state read to the layout or
      draw phase, skipping unnecessary recompositions.

      ❌ Before (reads during composition):
      Modifier.$modifierName(value.dp)

      ✅ After (defers read to layout phase):
      ${alternative.lambdaVersion}

      Learn more: https://developer.android.com/develop/ui/compose/performance#defer-reads
    """.trimIndent()
  }

  private data class LambdaAlternative(
    val lambdaVersion: String,
    val paramsToCheck: Set<String>,
    val suggestion: String,
  )
}
