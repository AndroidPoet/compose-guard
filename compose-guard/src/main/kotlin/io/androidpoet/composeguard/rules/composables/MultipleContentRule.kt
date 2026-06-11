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
import io.androidpoet.composeguard.rules.isContentEmittingStatement
import io.androidpoet.composeguard.rules.nonEmittingEffectComposables
import io.androidpoet.composeguard.rules.transparentComposableWrappers
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtWhenExpression

public class MultipleContentRule : ComposableFunctionRule() {
  override val id: String = "MultipleContentEmitters"
  override val name: String = "Multiple Content Emitters"
  override val description: String = "Composable functions should emit only one piece of content at the top level."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/rules/#do-not-emit-multiple-pieces-of-content"

  private val contentEmitters = setOf(
    "Box", "Column", "Row", "Surface", "Card", "Scaffold",
    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
    "Text", "Icon", "Image", "Button", "IconButton", "TextButton", "OutlinedButton",
    "TextField", "OutlinedTextField", "Checkbox", "RadioButton", "Switch", "Slider",
    "ConstraintLayout", "FlowRow", "FlowColumn", "BoxWithConstraints",
    "Divider", "Spacer", "Canvas",
    "AlertDialog", "Dialog",
    "NavigationRail", "NavigationBar", "TopAppBar", "BottomAppBar",
  )

  private val allowedReceiverTypes = setOf(
    "ColumnScope", "RowScope", "BoxScope", "LazyListScope", "LazyItemScope",
    "LazyGridScope", "LazyGridItemScope", "FlowRowScope", "FlowColumnScope",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val receiverType = function.receiverTypeReference?.text
    if (receiverType != null && allowedReceiverTypes.any { receiverType.contains(it) }) {
      return emptyList()
    }

    val body = function.bodyBlockExpression ?: return emptyList()

    val topLevelEmissions = emissionsInBlock(body)

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

  private fun emissionsInBlock(body: KtBlockExpression): Int {
    val statements = PsiTreeUtil.getChildrenOfType(body, KtExpression::class.java) ?: return 0
    return statements.sumOf { emissionsOf(it) }
  }

  private fun emissionsOf(element: KtExpression): Int = when (element) {
    is KtCallExpression -> emissionsOfCall(element)
    // Only one branch of a conditional runs, so its contribution is the heaviest single branch, not
    // the sum — `if (c) Text() else Text()` is one emission, but an emitter alongside a conditional
    // emitter, or two emitters in one branch, are genuinely multiple top-level emissions.
    is KtIfExpression -> maxOf(branchEmissions(element.then), branchEmissions(element.`else`))
    is KtWhenExpression -> element.entries.maxOfOrNull { branchEmissions(it.expression) } ?: 0
    else -> 0
  }

  private fun branchEmissions(branch: KtExpression?): Int = when (branch) {
    null -> 0
    is KtBlockExpression -> emissionsInBlock(branch)
    else -> emissionsOf(branch)
  }

  private fun emissionsOfCall(call: KtCallExpression): Int {
    val callName = call.calleeExpression?.text ?: return 0

    if (callName in nonEmittingEffectComposables) return 0

    if (callName in transparentComposableWrappers) {
      // Transparent wrappers (CompositionLocalProvider, key) introduce no layout node, so content
      // directly inside them is still top-level emission — see through them rather than ignoring.
      val wrapperBody = call.lambdaArguments.lastOrNull()?.getLambdaExpression()?.bodyExpression
      return if (wrapperBody != null) emissionsInBlock(wrapperBody) else 0
    }

    if (callName in contentEmitters) return 1
    if (callName.firstOrNull()?.isUpperCase() == true && call.isContentEmittingStatement()) return 1

    return 0
  }
}
