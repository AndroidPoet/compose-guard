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
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Rule: Don't forward ViewModels through composable functions.
 *
 * Passing ViewModels as parameters through multiple composable layers
 * creates tight coupling and makes the composition harder to test.
 * Instead, pass only the state and callbacks that child composables need.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#viewmodels-should-not-be-forwarded">ViewModels Should Not Be Forwarded</a>
 */
public class ViewModelForwardingRule : ComposableFunctionRule() {
  override val id: String = "ViewModelForwarding"
  override val name: String = "Don't Forward ViewModels"
  override val description: String = "ViewModels should not be passed through composable functions."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#viewmodels-should-not-be-forwarded"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    // Check if any parameter is a ViewModel type
    val viewModelParams = function.valueParameters.filter { param ->
      val typeName = param.typeReference?.text ?: return@filter false
      isViewModelType(typeName)
    }

    if (viewModelParams.isEmpty()) return emptyList()

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Find all composable calls in the body
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (param in viewModelParams) {
      val paramName = param.name ?: continue

      // Check if this ViewModel is being passed to another composable
      for (call in calls) {
        val args = call.valueArguments
        for (arg in args) {
          val argText = arg.getArgumentExpression()?.text ?: continue
          if (argText == paramName) {
            // Check if the argument is named as a ViewModel parameter
            val argName = arg.getArgumentName()?.asName?.asString()
            if (argName != null && isViewModelParamName(argName)) {
              violations.add(
                createViolation(
                  element = arg,
                  message = "ViewModel '$paramName' is being forwarded to another composable",
                  tooltip = """
                    Forwarding ViewModels through composable functions creates tight coupling
                    and makes testing difficult.

                    Instead of passing the ViewModel, pass only the state and callbacks needed:

                    ❌ Bad:
                    @Composable
                    fun ParentScreen(viewModel: MyViewModel) {
                        ChildScreen(viewModel = viewModel)  // Forwarding ViewModel
                    }

                    ✅ Good:
                    @Composable
                    fun ParentScreen(viewModel: MyViewModel) {
                        val state by viewModel.state.collectAsState()
                        ChildScreen(
                            state = state,
                            onAction = viewModel::handleAction
                        )
                    }

                    Benefits:
                    - Child composables become pure UI functions
                    - Easier to test and preview
                    - Better separation of concerns
                    - More reusable components
                  """.trimIndent(),
                  quickFixes = listOf(SuppressComposeRuleFix(id)),
                ),
              )
            }
          }
        }
      }
    }

    return violations
  }

  private fun isViewModelType(typeName: String): Boolean {
    return typeName.endsWith("ViewModel") ||
      typeName.contains("ViewModel<") ||
      typeName == "ViewModel" ||
      typeName.endsWith("VM")
  }

  private fun isViewModelParamName(name: String): Boolean {
    return name == "viewModel" ||
      name == "vm" ||
      name.endsWith("ViewModel") ||
      name.endsWith("VM")
  }
}
