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

public class ViewModelForwardingRule : ComposableFunctionRule() {
  override val id: String = "ViewModelForwarding"
  override val name: String = "Don't Forward ViewModels"
  override val description: String = "ViewModels should not be passed through composable functions."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#viewmodels-should-not-be-forwarded"

  // Effects take the ViewModel as a restart key, not as a forwarded argument, so passing it
  // positionally to these is legitimate and must not be flagged as forwarding.
  private val effectComposables = setOf("LaunchedEffect", "DisposableEffect")

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    val viewModelParams = function.valueParameters.filter { param ->
      val typeName = param.typeReference?.text ?: return@filter false
      isViewModelType(typeName)
    }

    if (viewModelParams.isEmpty()) return emptyList()

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (param in viewModelParams) {
      val paramName = param.name ?: continue

      for (call in calls) {
        // Forwarding only applies when the ViewModel is handed to ANOTHER composable. Composable
        // callees are PascalCase; passing the VM to an ordinary helper, builder or effect
        // (e.g. LaunchedEffect, remember, a lowercase function) is not forwarding.
        val calleeName = call.calleeExpression?.text ?: continue
        if (calleeName.firstOrNull()?.isUpperCase() != true) continue

        val args = call.valueArguments
        for (arg in args) {
          val argText = arg.getArgumentExpression()?.text ?: continue
          if (argText == paramName) {
            // The VM is handed verbatim to a composable callee — that is forwarding regardless of
            // whether the argument is positional or named, and regardless of what the child calls
            // its parameter. Effects take the VM as a restart key, not as a forward, so exempt them.
            if (calleeName !in effectComposables) {
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
}
