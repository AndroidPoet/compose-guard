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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseLifecycleAwareCollectorFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

public class FrequentRecompositionRule : ComposableFunctionRule() {

  override val id: String = "FrequentRecomposition"

  override val name: String = "Potential Excessive Recomposition"

  override val description: String = """
    Detects patterns that may cause excessive recompositions, impacting
    performance of your Compose UI.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/performance/bestpractices"

  private val collectionPatterns = setOf(
    "collectAsState",
    "collectAsStateWithLifecycle",
    "observeAsState",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    checkFlowCollection(body, violations)

    return violations
  }

  private fun checkFlowCollection(
    body: com.intellij.psi.PsiElement,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    val dotExpressions = PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val text = dotExpr.text

      for (pattern in collectionPatterns) {
        if (text.contains(".$pattern")) {
          if (pattern == "collectAsState" && !text.contains("collectAsStateWithLifecycle")) {
            val callExpr = dotExpr.selectorExpression as? KtCallExpression ?: continue

            violations.add(
              createViolation(
                element = callExpr,
                message = "Consider using collectAsStateWithLifecycle for lifecycle awareness",
                tooltip = """
                  collectAsState() continues collecting even when the app is in the
                  background, which can waste resources.

                  Consider using collectAsStateWithLifecycle() from:
                  androidx.lifecycle.compose

                  This automatically stops collection when the lifecycle is below
                  a certain state (default: STARTED).

                  Example:
                  val state by flow.collectAsStateWithLifecycle()

                  Add dependency:
                  implementation("androidx.lifecycle:lifecycle-runtime-compose:x.x.x")
                """.trimIndent(),
                quickFixes = listOf(UseLifecycleAwareCollectorFix(), SuppressComposeRuleFix(id)),
              ),
            )
          }
        }
      }
    }
  }
}
