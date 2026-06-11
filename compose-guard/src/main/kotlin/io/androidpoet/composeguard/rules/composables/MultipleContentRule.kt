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
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

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

  private val transparentWrappers = setOf(
    "CompositionLocalProvider",
    "key",
  )

  // Effects emit no UI, so they must not be counted toward the content-emitter total even though
  // their callees are PascalCase and sit in statement position.
  private val sideEffects = setOf(
    "LaunchedEffect",
    "DisposableEffect",
    "SideEffect",
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

    val topLevelEmissions = countTopLevelEmissions(body)

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

  private fun countTopLevelEmissions(body: KtBlockExpression): Int {
    var count = 0

    val directCalls = PsiTreeUtil.getChildrenOfType(body, KtCallExpression::class.java) ?: return 0

    for (call in directCalls) {
      val callName = call.calleeExpression?.text ?: continue

      if (callName in transparentWrappers || callName in sideEffects) {
        continue
      }

      if (callName in contentEmitters) {
        count++
      } else if (callName.firstOrNull()?.isUpperCase() == true && call.isContentEmittingStatement()) {
        count++
      }
    }

    return count
  }
}
