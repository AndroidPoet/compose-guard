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
package io.androidpoet.composeguard.rules.modifiers

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.MoveModifierToRootFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.getModifierParameter
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule: Modifiers should be used at the top-most layout of a composable.
 *
 * The modifier parameter passed to a composable should be applied to the
 * root layout node, not nested children. This ensures the composable
 * behaves correctly when embedded in different parent layouts.
 *
 * Example violation:
 * ```
 * @Composable
 * fun MyComposable(modifier: Modifier = Modifier) {
 *     Box {  // modifier should be here
 *         Column(modifier = modifier) { // Wrong - not at root
 *             Text("Nested")
 *         }
 *     }
 * }
 * ```
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-be-used-at-the-top-most-layout-of-the-component">Modifiers at Top-Most Layout</a>
 */
public class ModifierTopMostRule : ComposableFunctionRule() {

  override val id: String = "ModifierTopMost"

  override val name: String = "Modifier at Top-Most Layout"

  override val description: String = """
    The modifier parameter should be applied to the root layout
    of the composable, not to nested children.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#modifiers-should-be-used-at-the-top-most-layout-of-the-component"

  // Common content-emitting composables (layouts that can have children)
  private val contentEmitters = setOf(
    "Box", "Column", "Row", "Surface", "Card", "Scaffold",
    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
    "ConstraintLayout", "FlowRow", "FlowColumn", "BoxWithConstraints",
    "AlertDialog", "Dialog", "ModalBottomSheet", "BottomSheet",
    "NavigationRail", "NavigationBar", "TopAppBar", "BottomAppBar",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val modifierParam = function.getModifierParameter() ?: return emptyList()
    val modifierName = modifierParam.name ?: return emptyList()

    val body = function.bodyBlockExpression ?: return emptyList()

    // Find all call expressions that use the modifier parameter
    val allCalls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in allCalls) {
      // Check if this call uses the modifier parameter
      val modifierArgument = findModifierArgument(call, modifierName) ?: continue

      // Check if this is a composable call (starts with uppercase)
      val callName = call.calleeExpression?.text ?: continue
      if (!callName.first().isUpperCase()) continue

      // Check if there's a parent content-emitting composable
      val hasContentEmitterParent = hasParentContentEmitter(call, body)

      if (hasContentEmitterParent) {
        violations.add(
          createViolation(
            element = modifierArgument,
            message = "Modifier should be applied to the root composable, not nested inside another layout",
            tooltip = """
              The main modifier parameter should be applied to the root-most layout
              in your composable, not to a nested child.

              Currently, '$modifierName' is used on '$callName' which is nested inside
              another content-emitting composable.

              Why this matters:
              - Parent composables can't control size/position of this composable
              - Breaks composable reusability in different layouts
              - May cause unexpected layout behavior

              Fix: Move the modifier to the root layout:
              ❌ Box {
                   Column(modifier = $modifierName) { ... }
                 }

              ✅ Box(modifier = $modifierName) {
                   Column { ... }
                 }
            """.trimIndent(),
            quickFixes = listOf(
              MoveModifierToRootFix(modifierName),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }

  /**
   * Finds the modifier argument in a call expression if it uses the given modifier name.
   */
  private fun findModifierArgument(call: KtCallExpression, modifierName: String): KtValueArgument? {
    val valueArguments = call.valueArgumentList?.arguments ?: return null
    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argExpr = arg.getArgumentExpression()?.text ?: continue

      // Check if this argument is the modifier parameter
      // It could be: modifier = modifier, modifier = modifier.something(), or just positional
      if ((argName == "modifier" || argName == null) &&
        (argExpr == modifierName || argExpr.startsWith("$modifierName."))
      ) {
        return arg
      }
    }
    return null
  }

  /**
   * Checks if there's a parent composable that emits content (i.e., this call is nested).
   */
  private fun hasParentContentEmitter(call: KtCallExpression, functionBody: KtBlockExpression): Boolean {
    // Walk up the tree to find parent composable calls
    for (parent in call.parents) {
      // Stop when we reach the function body
      if (parent == functionBody) break

      // Check if we're inside a lambda argument of a composable call
      if (parent is KtLambdaArgument) {
        val parentCall = parent.parent as? KtCallExpression ?: continue
        val parentCallName = parentCall.calleeExpression?.text ?: continue

        // Check if the parent is a content-emitting composable
        if (parentCallName.first().isUpperCase() && parentCallName in contentEmitters) {
          return true
        }

        // Even if not in our list, if it starts with uppercase and has a trailing lambda,
        // it's likely a content-emitting composable
        if (parentCallName.first().isUpperCase() && parentCall.lambdaArguments.isNotEmpty()) {
          return true
        }
      }
    }
    return false
  }
}
