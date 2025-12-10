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
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.AnyFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Avoid composed {} modifier factory.
 *
 * The `composed` API has performance issues and is considered deprecated.
 * Use Modifier.Node instead for better performance and semantics.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#avoid-modifier-extension-factory-functions">Avoid Modifier Extension Factory Functions</a>
 */
public class AvoidComposedRule : AnyFunctionRule() {
  override val id: String = "AvoidComposed"
  override val name: String = "Avoid composed {} Modifier"
  override val description: String = "Use Modifier.Node instead of deprecated composed {} API."
  override val category: RuleCategory = RuleCategory.MODIFIER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#avoid-modifier-extension-factory-functions"

  // This rule does NOT require @Composable - it checks Modifier extension functions
  override val requiresComposable: Boolean = false

  override fun shouldAnalyze(function: KtNamedFunction): Boolean {
    // Check if this is a Modifier extension function
    val receiverType = function.receiverTypeReference?.text ?: return false
    return receiverType == "Modifier" || receiverType.endsWith(".Modifier")
  }

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val violations = mutableListOf<ComposeRuleViolation>()

    // Collect all call expressions to check
    val callsToCheck = mutableListOf<KtCallExpression>()

    // Check if the body itself is a composed call (expression body: = composed { })
    if (body is KtCallExpression) {
      callsToCheck.add(body)
    }

    // Also check for composed calls within the body
    callsToCheck.addAll(PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java))

    for (call in callsToCheck) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName == "composed") {
        violations.add(createViolation(
          element = call,
          message = "Avoid using 'composed {}' - use Modifier.Node instead",
          tooltip = """
            The 'composed' API has performance issues and is considered deprecated.

            Problems with composed:
            - Creates a new composition for each modifier instance
            - Causes performance overhead during recomposition
            - Less efficient than Modifier.Node

            Solution: Migrate to Modifier.Node for better performance:
            - Create a ModifierNodeElement subclass
            - Create a Modifier.Node subclass
            - Use Modifier.then() instead of composed {}

            See: https://developer.android.com/develop/ui/compose/modifiers
          """.trimIndent(),
          quickFixes = listOf(
            SuppressComposeRuleFix(id),
          ),
        ))
      }
    }

    return violations
  }
}
