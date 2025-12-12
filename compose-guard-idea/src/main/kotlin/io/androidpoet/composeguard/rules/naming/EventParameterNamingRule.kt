/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
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

/**
 * Rule: Event parameters should follow "on" + verb naming pattern.
 */
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

      // Check if this is a callback (function type)
      if (!typeText.contains("->")) continue

      // Check for past-tense event names (onClicked, onChanged, etc.)
      if (name.startsWith("on") && name.endsWith("ed")) {
        val suggestedName = convertPastTenseToPresent(name)
        violations.add(createViolation(
          element = param.nameIdentifier ?: param,
          message = "Event '$name' should use present-tense verb",
          tooltip = "Use present-tense: '$suggestedName' instead of '$name'.",
          quickFixes = listOf(
            RenameParameterFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ))
      }
    }

    return violations
  }

  /**
   * Converts a past-tense event name to present tense.
   *
   * Handles various patterns:
   * - onClicked -> onClick (simple -ed removal)
   * - onSubmitted -> onSubmit (doubled consonant before -ed)
   * - onChanged -> onChange (verbs ending in 'e' only add 'd')
   */
  private fun convertPastTenseToPresent(name: String): String {
    if (!name.endsWith("ed")) return name

    val withoutEd = name.dropLast(2)

    // Pattern 1: Words ending in doubled consonant + "ed" (e.g., onSubmitted -> onSubmit)
    // Check if the last two chars before "ed" are the same consonant
    if (withoutEd.length >= 2) {
      val lastChar = withoutEd.last()
      val secondLastChar = withoutEd[withoutEd.length - 2]

      if (lastChar == secondLastChar && lastChar.isConsonant()) {
        return withoutEd.dropLast(1)
      }
    }

    // Pattern 2: Words where base ends in 'e' and only 'd' was added (e.g., onChanged -> onChange)
    // For verbs ending in silent 'e', English adds only 'd' (not 'ed'):
    // "change" + "d" = "changed", "close" + "d" = "closed"
    //
    // We check if removing just 'd' gives us a word ending in 'e' that follows
    // common verb patterns, excluding known non-silent-e endings
    val withoutD = name.dropLast(1) // Try removing just 'd'
    if (withoutD.endsWith("e") && withoutD.length >= 4) {
      // Check for consonant clusters that DON'T typically precede silent 'e'
      // These are common English endings where 'e' wouldn't be silent
      val ending = withoutD.takeLast(3).lowercase()
      val nonSilentEEndings = listOf(
        "cke", // click, tick, pick -> NOT clicke
        "ske", // ask -> NOT aske
        "cte", // select, connect -> NOT selecte
        "ste", // test -> NOT teste (but "taste" is different, handled by vowel pattern)
        "rte", // start, sort -> NOT starte
        "nte", // want, print -> NOT wante
        "xte", // text -> NOT texte
        "fte", // lift, shift -> NOT lifte
        "pte", // accept -> NOT accepte
        "rke", // work, mark -> NOT worke
        "lke", // walk, talk -> NOT walke
        "nke", // think, link -> NOT thinke
        "ade", // load -> NOT loade (load + ed, not loade + d)
        "rde", // record -> NOT recorde
        "nde", // send, find -> NOT sende
      )

      // If ends with a known non-silent-e pattern, use simple -ed removal
      if (nonSilentEEndings.any { ending.endsWith(it) }) {
        return withoutEd
      }

      val beforeE = withoutD[withoutD.length - 2]
      val beforeBeforeE = withoutD[withoutD.length - 3]

      // Common pattern: vowel + consonant + e (save, close, change, etc.)
      // e.g., "change" = ch-a-ng-e, last 3 = "nge", beforeE='g', beforeBeforeE='n'
      // Actually for change: withoutD="onChange", beforeE='g', beforeBeforeE='n'
      // We need to check if the pattern BEFORE the consonant has a vowel
      if (beforeE.isConsonant()) {
        // Check for vowel + single consonant + e pattern (save, close, etc.)
        if (beforeBeforeE.isVowel()) {
          return withoutD
        }
        // Check for vowel + double consonant + e pattern (change, etc.)
        if (beforeBeforeE.isConsonant() && withoutD.length >= 5) {
          val threeBack = withoutD[withoutD.length - 4]
          if (threeBack.isVowel()) {
            return withoutD
          }
        }
      }
    }

    // Default: just return with "ed" removed (e.g., onClicked -> onClick)
    return withoutEd
  }

  private fun Char.isConsonant(): Boolean {
    return this.lowercaseChar() !in "aeiou" && this.isLetter()
  }

  private fun Char.isVowel(): Boolean {
    return this.lowercaseChar() in "aeiou"
  }
}
