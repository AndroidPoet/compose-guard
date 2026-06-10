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
import io.androidpoet.composeguard.rules.isSuppressed
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

public class EffectKeysRule : ComposableFunctionRule() {
  override val id: String = "EffectKeys"
  override val name: String = "Effect Keys Correctness"
  override val description: String = "LaunchedEffect and DisposableEffect should have appropriate keys."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#be-mindful-of-effect-keys"

  private val effectComposables = setOf("LaunchedEffect", "DisposableEffect")
  private val constantKeys = setOf("Unit", "true", "false")

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    if (isSuppressed(function, id)) {
      return emptyList()
    }

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName !in effectComposables) continue

      if (isSuppressed(call, id)) continue

      val args = call.valueArguments
      val firstArg = args.firstOrNull()?.text
      if (firstArg !in constantKeys) continue

      // A constant key (Unit/true/false) is the idiomatic "run once" pattern and is only a problem
      // when the effect captures parameters that can change between recompositions — those should
      // be keys. If nothing changing is captured, leave it alone instead of nagging on every
      // run-once effect.
      val captured = capturedChangingParameters(call, function)
      if (captured.isEmpty()) continue

      violations.add(
        createViolation(
          element = call,
          message = "'$calleeName($firstArg)' won't restart when ${captured.joinToString(", ")} change(s)",
          tooltip = """
            This effect uses the constant key '$firstArg', so it never restarts during
            recomposition — but it captures parameter(s) that can change: ${captured.joinToString(", ")}.

            Pass the changing values as keys so the effect restarts when they change:
            $calleeName(${captured.joinToString(", ")}) { ... }

            If a captured value is stable and should NOT restart the effect, wrap it with
            rememberUpdatedState instead, or suppress this warning if running once is intentional.
          """.trimIndent(),
          quickFixes = listOf(
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    return violations
  }

  private fun capturedChangingParameters(call: KtCallExpression, function: KtNamedFunction): List<String> {
    val paramNames = function.valueParameters.mapNotNull { it.name }.toSet()
    if (paramNames.isEmpty()) return emptyList()

    val keyTexts = call.valueArguments
      .mapNotNull { it.getArgumentExpression() }
      .filter { it !is KtLambdaExpression }
      .map { it.text }
      .toSet()

    val lambda = call.lambdaArguments.firstOrNull()?.getLambdaExpression()
      ?: call.valueArguments
        .mapNotNull { it.getArgumentExpression() as? KtLambdaExpression }
        .lastOrNull()
    val body = lambda?.bodyExpression ?: return emptyList()

    return PsiTreeUtil.findChildrenOfType(body, KtNameReferenceExpression::class.java)
      .map { it.getReferencedName() }
      .filter { it in paramNames && it !in keyTexts }
      .distinct()
  }
}
