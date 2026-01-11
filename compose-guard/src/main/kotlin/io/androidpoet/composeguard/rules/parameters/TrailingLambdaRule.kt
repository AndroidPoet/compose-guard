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

import io.androidpoet.composeguard.quickfix.MoveToTrailingLambdaFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposableLambda
import org.jetbrains.kotlin.psi.KtNamedFunction

public class TrailingLambdaRule : ComposableFunctionRule() {
  override val id: String = "TrailingLambda"
  override val name: String = "Trailing Lambda Rules"
  override val description: String = "Content slots should be trailing lambdas; event handlers (onClick) should not."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#slots-for-main-content-should-be-the-trailing-lambda"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val params = function.valueParameters
    if (params.isEmpty()) return emptyList()
    val violations = mutableListOf<ComposeRuleViolation>()
    val lastParam = params.last()

    val eventHandlerNames = setOf("onClick", "onValueChange", "onChange", "onDismiss", "onConfirm")
    if (lastParam.name in eventHandlerNames && params.any { it.isComposableLambda() }) {
      violations.add(
        createViolation(
          element = lastParam.nameIdentifier ?: lastParam,
          message = "Event handler '${lastParam.name}' should not be trailing lambda",
          tooltip = "Content slots should be trailing lambdas, not event handlers.",
          quickFixes = listOf(
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    val contentLambdas = params.filter { it.isComposableLambda() }
    if (contentLambdas.isNotEmpty()) {
      val mainContent = contentLambdas.find { it.name == "content" || it.name == null }
      if (mainContent != null && mainContent != lastParam) {
        violations.add(
          createViolation(
            element = mainContent.nameIdentifier ?: mainContent,
            message = "Content slot should be the trailing lambda",
            tooltip = "Move content slot to the last position for better call-site syntax.",
            quickFixes = listOf(
              MoveToTrailingLambdaFix(),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }
}
