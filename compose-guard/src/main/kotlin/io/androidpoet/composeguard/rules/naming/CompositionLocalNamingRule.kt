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

import io.androidpoet.composeguard.quickfix.AddLocalPrefixFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.PropertyRule
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isCompositionLocal
import org.jetbrains.kotlin.psi.KtProperty

public class CompositionLocalNamingRule : PropertyRule() {
  override val id: String = "CompositionLocalNaming"
  override val name: String = "CompositionLocal Naming"
  override val description: String = "CompositionLocal instances should start with 'Local' prefix."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-compositionlocals-properly"

  override fun shouldAnalyze(property: KtProperty): Boolean = property.isCompositionLocal()

  override fun doAnalyze(property: KtProperty, context: AnalysisContext): List<ComposeRuleViolation> {
    val name = property.name ?: return emptyList()
    if (!name.startsWith("Local")) {
      val suggestedName = "Local$name"
      return listOf(
        createViolation(
          element = property.nameIdentifier ?: property,
          message = "CompositionLocal '$name' should start with 'Local' prefix",
          tooltip = """
          CompositionLocal instances should follow naming convention with 'Local' prefix.

          Change: $name
          To: $suggestedName

          Example:
          ❌ val CurrentUser = compositionLocalOf { ... }
          ✅ val LocalCurrentUser = compositionLocalOf { ... }
          """.trimIndent(),
          quickFixes = listOf(
            AddLocalPrefixFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }
    return emptyList()
  }
}
