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

    val allCalls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in allCalls) {
      val modifierArgument = findModifierArgument(call, modifierName) ?: continue

      val callName = call.calleeExpression?.text ?: continue
      if (!callName.first().isUpperCase()) continue

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

  private fun findModifierArgument(call: KtCallExpression, modifierName: String): KtValueArgument? {
    val valueArguments = call.valueArgumentList?.arguments ?: return null
    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argExpr = arg.getArgumentExpression()?.text ?: continue

      if ((argName == "modifier" || argName == null) &&
        (argExpr == modifierName || argExpr.startsWith("$modifierName."))
      ) {
        return arg
      }
    }
    return null
  }

  private fun hasParentContentEmitter(call: KtCallExpression, functionBody: KtBlockExpression): Boolean {
    for (parent in call.parents) {
      if (parent == functionBody) break

      if (parent is KtLambdaArgument) {
        val parentCall = parent.parent as? KtCallExpression ?: continue
        val parentCallName = parentCall.calleeExpression?.text ?: continue

        if (parentCallName.first().isUpperCase() && parentCallName in contentEmitters) {
          return true
        }

        if (parentCallName.first().isUpperCase() && parentCall.lambdaArguments.isNotEmpty()) {
          return true
        }
      }
    }
    return false
  }
}
