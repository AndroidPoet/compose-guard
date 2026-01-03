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
package io.androidpoet.composeguard.rules.parameters

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.AddExplicitParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Make dependencies explicit (ViewModels, CompositionLocals).
 *
 * ViewModels and CompositionLocals acquired inside a composable function body
 * make the composable harder to test and preview. Consider making them explicit
 * parameters with default values.
 *
 * This rule skips:
 * - Override functions (constrained by parent)
 * - Navigation composable blocks (viewModel() is idiomatic there)
 * - Calls with arguments (may need specific configuration)
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#make-dependencies-explicit-viewmodels">Make Dependencies Explicit</a>
 */
public class ExplicitDependenciesRule : ComposableFunctionRule() {
  override val id: String = "ExplicitDependencies"
  override val name: String = "Make Dependencies Explicit"
  override val description: String = "ViewModels and CompositionLocals acquired in body should be parameters."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#make-dependencies-explicit-viewmodels"

  private val viewModelFactories = setOf(
    "viewModel",
    "hiltViewModel",
    "koinViewModel",
    "koinNavViewModel",
    "mavericksViewModel",
    "tangleViewModel",
    "weaverViewModel",
    "injectedViewModel",
  )

  private val navigationContexts = setOf(
    "composable",
    "navigation",
    "NavHost",
    "NavGraphBuilder",
    "dialog",
    "bottomSheet",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (function.hasModifier(KtTokens.OVERRIDE_KEYWORD)) {
      return emptyList()
    }

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    if (isInNavigationContext(function)) {
      return emptyList()
    }

    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue

      if (calleeName in viewModelFactories) {
        if (call.valueArguments.isNotEmpty()) continue

        val viewModelType = extractViewModelType(call)
        val quickFixes = if (viewModelType != null) {
          listOf(
            AddExplicitParameterFix(viewModelType, viewModelType),
            SuppressComposeRuleFix(id),
          )
        } else {
          listOf(SuppressComposeRuleFix(id))
        }

        violations.add(
          createViolation(
            element = call,
            message = "Consider making ViewModel an explicit parameter",
            tooltip = """
              Implicit ViewModel injection makes this composable harder to test and preview.

              Consider making it an explicit parameter with a default value:

              ❌ Current:
              @Composable
              fun MyScreen() {
                  val viewModel = $calleeName<MyViewModel>()
              }

              ✅ Better:
              @Composable
              fun MyScreen(
                  viewModel: MyViewModel = $calleeName()
              ) {
              }

              Benefits:
              - Easier to test with fake/mock ViewModels
              - Works better in @Preview functions
              - More explicit dependencies
            """.trimIndent(),
            quickFixes = quickFixes,
          ),
        )
      }

      if (calleeName.startsWith("Local") && calleeName != "LocalContext") {
        val dotExpr = call.parent
        if (dotExpr?.text?.endsWith(".current") == true) {
          val paramType = inferParameterType(calleeName)
          violations.add(
            createViolation(
              element = call,
              message = "Consider making '$calleeName' an explicit parameter",
              tooltip = """
                Accessing CompositionLocals in the body makes testing harder.

                Consider making it a parameter:

                ❌ Current:
                @Composable
                fun MyComposable() {
                    val value = $calleeName.current
                }

                ✅ Better:
                @Composable
                fun MyComposable(
                    value: $paramType = $calleeName.current
                ) {
                }
              """.trimIndent(),
              quickFixes = listOf(
                AddExplicitParameterFix(calleeName, paramType),
                SuppressComposeRuleFix(id),
              ),
            ),
          )
        }
      }
    }

    return violations
  }

  private fun isInNavigationContext(function: KtNamedFunction): Boolean {
    var parent = function.parent
    while (parent != null) {
      if (parent is KtCallExpression) {
        val callName = parent.calleeExpression?.text
        if (callName in navigationContexts) {
          return true
        }
      }
      parent = parent.parent
    }
    return false
  }

  private fun inferParameterType(calleeName: String): String {
    return when (calleeName) {
      "LocalContext" -> "Context"
      "LocalConfiguration" -> "Configuration"
      "LocalDensity" -> "Density"
      "LocalLayoutDirection" -> "LayoutDirection"
      "LocalLifecycleOwner" -> "LifecycleOwner"
      "LocalView" -> "View"
      "LocalFocusManager" -> "FocusManager"
      "LocalHapticFeedback" -> "HapticFeedback"
      "LocalTextInputService" -> "TextInputService"
      "LocalClipboardManager" -> "ClipboardManager"
      else -> calleeName.removePrefix("Local")
    }
  }

  /**
   * Extracts the ViewModel type from a call expression like viewModel<SampleViewModel>().
   * Returns null if the type cannot be determined.
   */
  private fun extractViewModelType(call: KtCallExpression): String? {
    val typeArguments = call.typeArguments
    if (typeArguments.isNotEmpty()) {
      val typeArg = typeArguments.firstOrNull()
      return typeArg?.typeReference?.text
    }
    return null
  }
}
