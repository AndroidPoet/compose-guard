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
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.quickfix.RenameComposableFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isPreview
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Preview functions should follow naming conventions.
 */
public class PreviewNamingRule : ComposableFunctionRule() {
  override val id: String = "PreviewNaming"
  override val name: String = "Preview Naming Convention"
  override val description: String = "Preview functions should include 'Preview' in their name."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-previews-properly"

  override fun shouldAnalyze(function: KtNamedFunction): Boolean = function.isPreview()

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val name = function.name ?: return emptyList()
    if (!name.contains("Preview", ignoreCase = true)) {
      val suggestedName = "${name}Preview"
      return listOf(
        createViolation(
          element = function.nameIdentifier ?: function,
          message = "Preview function '$name' should include 'Preview' in name",
          tooltip = "Add 'Preview' suffix to clearly identify preview functions (e.g., '$suggestedName').",
          quickFixes = listOf(
            RenameComposableFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }
    return emptyList()
  }
}
