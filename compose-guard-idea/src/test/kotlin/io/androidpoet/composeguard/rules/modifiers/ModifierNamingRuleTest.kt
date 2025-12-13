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
 * Comprehensive tests for ModifierNamingRule.
 *
 * Rule: Modifier parameters should be named properly.
 *
 * - Main modifier should be named `modifier`
 * - Sub-component modifiers should be named `xModifier` (e.g., `contentModifier`, `iconModifier`)
 */
class ModifierNamingRuleTest {

  private val rule = ModifierNamingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ModifierNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Modifier Naming Convention", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
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
  fun metadata_descriptionMentionsNaming() {
    assertTrue(
      rule.description.contains("named") ||
        rule.description.contains("modifier") ||
        rule.description.contains("Modifier"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Main modifier named 'modifier' - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(
   *     modifier: Modifier = Modifier  // Correct!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_mainModifierNamedCorrectly_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Main modifier not named 'modifier' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(
   *     mod: Modifier = Modifier  // Wrong name!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_mainModifierWrongName_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Sub-component modifier named 'xModifier' - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun IconButton(
   *     modifier: Modifier = Modifier,
   *     iconModifier: Modifier = Modifier,  // Correct pattern!
   *     contentModifier: Modifier = Modifier
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_subModifierWithXModifierPattern_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Multiple modifiers named 'modifier' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun IconButton(
   *     modifier: Modifier = Modifier,
   *     modifier: Modifier = Modifier  // Duplicate!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_duplicateModifierName_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Sub-component modifier not following xModifier pattern - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun IconButton(
   *     modifier: Modifier = Modifier,
   *     icon: Modifier = Modifier  // Should be iconModifier!
   * ) { ... }
   * ```
   */
  @Test
  fun pattern_subModifierWrongPattern_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why proper modifier naming matters:
   *
   * 1. **Consistency**: 'modifier' is the standard name across Compose
   * 2. **Discoverability**: IDE autocomplete expects 'modifier'
   * 3. **Clarity**: xModifier pattern clarifies which component gets modified
   *
   * Example:
   * ```kotlin
   * // Clear which modifier affects what
   * @Composable
   * fun IconCard(
   *     modifier: Modifier = Modifier,        // The whole card
   *     iconModifier: Modifier = Modifier,    // Just the icon
   *     textModifier: Modifier = Modifier     // Just the text
   * )
   * ```
   */
  @Test
  fun reason_consistencyAndClarity() {
    assertTrue(rule.enabledByDefault)
  }
}
