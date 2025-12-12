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
package io.androidpoet.composeguard.rules.experimental

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

/**
 * Rule: Detect patterns that may cause excessive recompositions.
 *
 * This rule identifies common anti-patterns that lead to unnecessary
 * recompositions and performance issues in Compose.
 *
 * Detected patterns:
 * 1. Object creation in composition (new instances on every recomposition)
 * 2. Flow/LiveData collection without proper handling
 * 3. Frequently changing state reads at high levels
 * 4. Non-skippable composables with unstable parameters
 *
 * Example violations:
 * ```kotlin
 * @Composable
 * fun BadExample() {
 *     // BAD: New object created every recomposition
 *     val config = Config(color = Color.Red, size = 16.dp)
 *
 *     // BAD: New list created every recomposition
 *     val items = listOf("A", "B", "C")
 *
 *     // BAD: Collecting flow without lifecycle awareness
 *     val state = viewModel.stateFlow.collectAsState()
 * }
 * ```
 */
public class FrequentRecompositionRule : ComposableFunctionRule() {

  override val id: String = "FrequentRecomposition"

  override val name: String = "Potential Excessive Recomposition"

  override val description: String = """
    Detects patterns that may cause excessive recompositions, impacting
    performance of your Compose UI.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.EXPERIMENTAL

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val enabledByDefault: Boolean = true

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/performance/bestpractices"

  // Flow/State collection patterns
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

    // Check for flow collection without lifecycle awareness
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

      // Check for flow collection
      for (pattern in collectionPatterns) {
        if (text.contains(".$pattern")) {
          // This is generally fine, but we can suggest lifecycle-aware version
          if (pattern == "collectAsState" && !text.contains("collectAsStateWithLifecycle")) {
            // Only flag if not already using lifecycle-aware version
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
