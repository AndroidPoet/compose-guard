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
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

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

  // Standard framework CompositionLocals. Reading these is idiomatic and not something to hoist
  // into a parameter — the canonical rule is about declaring your own CompositionLocals, not
  // reading the platform ones. Flagging these is the most-reported false positive for this rule.
  private val frameworkCompositionLocals = setOf(
    "LocalContext",
    "LocalConfiguration",
    "LocalDensity",
    "LocalLayoutDirection",
    "LocalLifecycleOwner",
    "LocalView",
    "LocalViewConfiguration",
    "LocalFocusManager",
    "LocalHapticFeedback",
    "LocalTextInputService",
    "LocalSoftwareKeyboardController",
    "LocalClipboardManager",
    "LocalClipboard",
    "LocalUriHandler",
    "LocalContentColor",
    "LocalContentAlpha",
    "LocalTextStyle",
    "LocalTextSelectionColors",
    "LocalIndication",
    "LocalInspectionMode",
    "LocalAccessibilityManager",
    "LocalAutofill",
    "LocalAutofillTree",
    "LocalFontFamilyResolver",
    "LocalFontLoader",
    "LocalWindowInfo",
    "LocalGraphicsContext",
    "LocalAbsoluteTonalElevation",
    "LocalMinimumInteractiveComponentEnforcement",
    "LocalMinimumInteractiveComponentSize",
    "LocalOverscrollConfiguration",
    "LocalRippleConfiguration",
    "LocalScrollbarStyle",
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
    }

    // A CompositionLocal read is `LocalFoo.current` — a property access, NOT a call. Scanning only
    // call expressions never matched it, so this half of the rule was dead. Detect it via the
    // dot-qualified `<Local…>.current` shape and skip the platform CompositionLocals.
    val dotExpressions = PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java)
    for (dotExpr in dotExpressions) {
      if (dotExpr.selectorExpression?.text != "current") continue
      val localName = dotExpr.receiverExpression.text
      if (!localName.startsWith("Local")) continue
      if (localName in frameworkCompositionLocals) continue

      val paramType = inferParameterType(localName)
      violations.add(
        createViolation(
          element = dotExpr,
          message = "Consider making '$localName' an explicit parameter",
          tooltip = """
            Accessing CompositionLocals in the body makes testing harder.

            Consider making it a parameter:

            ❌ Current:
            @Composable
            fun MyComposable() {
                val value = $localName.current
            }

            ✅ Better:
            @Composable
            fun MyComposable(
                value: $paramType = $localName.current
            ) {
            }
          """.trimIndent(),
          quickFixes = listOf(
            AddExplicitParameterFix(localName, paramType),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
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

  private fun extractViewModelType(call: KtCallExpression): String? {
    val typeArguments = call.typeArguments
    if (typeArguments.isNotEmpty()) {
      val typeArg = typeArguments.firstOrNull()
      return typeArg?.typeReference?.text
    }
    return null
  }
}
