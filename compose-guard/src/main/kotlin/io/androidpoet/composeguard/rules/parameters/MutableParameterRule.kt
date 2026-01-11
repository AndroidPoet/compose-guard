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

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isMutableType
import org.jetbrains.kotlin.psi.KtNamedFunction

public class MutableParameterRule : ComposableFunctionRule() {
  override val id: String = "MutableParameter"
  override val name: String = "Don't Use Mutable Types as Parameters"
  override val description: String = "Mutable types (MutableList, ArrayList, etc.) should not be composable parameters."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#do-not-use-inherently-mutable-types-as-parameters"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    return function.valueParameters.mapNotNull { param ->
      val typeText = param.typeReference?.text ?: return@mapNotNull null
      if (typeText.isMutableType()) {
        createViolation(
          element = param.typeReference ?: param,
          message = "Mutable type '$typeText' is not stable for Compose",
          tooltip = """
            Mutable types like $typeText are not stable and won't trigger
            recomposition when their contents change.

            Consider these alternatives:

            1. Use @Immutable annotation for custom classes:
               @Immutable
               data class MyData(val items: List<String>)

            2. Use kotlinx.collections.immutable library:
               - ImmutableList, ImmutableSet, ImmutableMap
               - PersistentList, PersistentSet, PersistentMap

               Add dependency:
               implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

               See: https://github.com/Kotlin/kotlinx.collections.immutable

            3. Use @Stable annotation if the type is mutable but Compose
               should treat it as stable (use with caution).
          """.trimIndent(),
          quickFixes = listOf(SuppressComposeRuleFix(id)),
        )
      } else {
        null
      }
    }
  }
}
