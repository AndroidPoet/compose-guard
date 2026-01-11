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

import io.androidpoet.composeguard.quickfix.RenameParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtNamedFunction

public class EventParameterNamingRule : ComposableFunctionRule() {
  override val id: String = "EventParameterNaming"
  override val name: String = "Event Parameter Naming"
  override val description: String = "Event callbacks should follow 'on' + present-tense verb pattern (onClick, not onClicked)."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-parameters-properly"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    for (param in function.valueParameters) {
      val name = param.name ?: continue
      val typeText = param.typeReference?.text ?: continue

      if (!typeText.contains("->")) continue

      if (name.startsWith("on") && name.endsWith("ed")) {
        val suggestedName = convertPastTenseToPresent(name)
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Event '$name' should use present-tense verb",
            tooltip = "Use present-tense: '$suggestedName' instead of '$name'.",
            quickFixes = listOf(
              RenameParameterFix(suggestedName),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }

  private fun convertPastTenseToPresent(name: String): String {
    if (!name.endsWith("ed")) return name

    val withoutEd = name.dropLast(2)

    if (withoutEd.length >= 2) {
      val lastChar = withoutEd.last()
      val secondLastChar = withoutEd[withoutEd.length - 2]

      if (lastChar == secondLastChar && lastChar.isConsonant()) {
        return withoutEd.dropLast(1)
      }
    }

    val withoutD = name.dropLast(1)
    if (withoutD.endsWith("e") && withoutD.length >= 4) {
      val ending = withoutD.takeLast(3).lowercase()
      val nonSilentEEndings = listOf(
        "cke",
        "ske",
        "cte",
        "ste",
        "rte",
        "nte",
        "xte",
        "fte",
        "pte",
        "rke",
        "lke",
        "nke",
        "ade",
        "rde",
        "nde",
      )

      if (nonSilentEEndings.any { ending.endsWith(it) }) {
        return withoutEd
      }

      val beforeE = withoutD[withoutD.length - 2]
      val beforeBeforeE = withoutD[withoutD.length - 3]

      if (beforeE.isConsonant()) {
        if (beforeBeforeE.isVowel()) {
          return withoutD
        }
        if (beforeBeforeE.isConsonant() && withoutD.length >= 5) {
          val threeBack = withoutD[withoutD.length - 4]
          if (threeBack.isVowel()) {
            return withoutD
          }
        }
      }
    }

    return withoutEd
  }

  private fun Char.isConsonant(): Boolean {
    return this.lowercaseChar() !in "aeiou" && this.isLetter()
  }

  private fun Char.isVowel(): Boolean {
    return this.lowercaseChar() in "aeiou"
  }
}
