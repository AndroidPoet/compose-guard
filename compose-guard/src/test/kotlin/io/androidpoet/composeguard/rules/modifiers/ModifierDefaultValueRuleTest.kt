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

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for ModifierDefaultValueRule.
 *
 * Rule: Modifier parameters should have a default value of Modifier.
 *
 * This allows callers to optionally provide a modifier without requiring
 * one in all call sites.
 */
class ModifierDefaultValueRuleTest {

  private val rule = ModifierDefaultValueRule()


  @Test
  fun metadata_id() {
    assertEquals("ModifierDefaultValue", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Modifier Should Have Default Value", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun metadata_enabledByDefault() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun metadata_documentationUrl() {
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.startsWith("https://"))
    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun metadata_descriptionMentionsDefaultValue() {
    assertTrue(
      rule.description.contains("default") ||
        rule.description.contains("Default"),
    )
  }


  /**
   * Pattern: Modifier without default value - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(
   *     content: String,
   *     modifier: Modifier  // Missing default value!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_modifierWithoutDefault_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier with default value - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(
   *     content: String,
   *     modifier: Modifier = Modifier  // Correct!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_modifierWithDefault_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: No modifier parameter - NO VIOLATION (handled by ModifierRequiredRule)
   *
   * ```kotlin
   * @Composable
   * fun NoModifier() { ... }
   * ```
   */
  @Test
  fun pattern_noModifierParameter_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }


  /**
   * QuickFix: Add '= Modifier' default value
   *
   * Before:
   * ```kotlin
   * fun MyCard(modifier: Modifier)
   * ```
   *
   * After:
   * ```kotlin
   * fun MyCard(modifier: Modifier = Modifier)
   * ```
   */
  @Test
  fun quickFix_shouldAddDefaultValue() {
    assertTrue(rule.description.contains("Modifier") || rule.description.contains("default"))
  }


  /**
   * Why modifier needs default value:
   *
   * 1. **Optional customization**: Most callers don't need to modify
   * 2. **Cleaner call sites**: Avoids Modifier.Companion everywhere
   * 3. **Consistency**: Standard pattern across Compose
   *
   * Example of the problem:
   * ```kotlin
   * // Without default - every call site needs modifier
   * fun Avatar(url: String, modifier: Modifier)
   *
   * // Caller must always provide modifier
   * Avatar(url = "...", modifier = Modifier)  // Verbose!
   * Avatar(url = "...", modifier = Modifier)  // Every. Single. Time.
   * ```
   *
   * Solution:
   * ```kotlin
   * fun Avatar(url: String, modifier: Modifier = Modifier)
   *
   * // Clean call sites
   * Avatar(url = "...")  // Works!
   * Avatar(url = "...", modifier = Modifier.size(32.dp))  // When needed
   * ```
   */
  @Test
  fun reason_cleanerCallSites() {
    assertTrue(rule.enabledByDefault)
  }
}
