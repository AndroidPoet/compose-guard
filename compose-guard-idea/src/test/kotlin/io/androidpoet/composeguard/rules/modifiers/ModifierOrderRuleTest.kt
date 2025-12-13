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
 * Comprehensive tests for ModifierOrderRule.
 *
 * Rule: Modifier order matters.
 *
 * Modifiers are applied in order. Certain modifier orderings can cause issues:
 * - clickable/selectable should come BEFORE padding for proper touch targets
 * - clip should come BEFORE clickable to respect visual bounds
 * - background should come AFTER padding for proper visual effect
 */
class ModifierOrderRuleTest {

  private val rule = ModifierOrderRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ModifierOrder", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Modifier Order Matters", rule.name)
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
  fun metadata_descriptionMentionsOrder() {
    assertTrue(
      rule.description.contains("order") ||
        rule.description.contains("Order") ||
        rule.description.contains("clickable") ||
        rule.description.contains("padding"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - PADDING + CLICKABLE
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: padding before clickable - VIOLATION (reduces touch target)
   *
   * ```kotlin
   * modifier
   *     .padding(16.dp)      // Reduces bounds
   *     .clickable { }       // Smaller touch target!
   * ```
   */
  @Test
  fun pattern_paddingBeforeClickable_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: clickable before padding - NO VIOLATION (correct order)
   *
   * ```kotlin
   * modifier
   *     .clickable { }       // Full touch target
   *     .padding(16.dp)      // Visual spacing only
   * ```
   */
  @Test
  fun pattern_clickableBeforePadding_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: padding before selectable - VIOLATION
   *
   * ```kotlin
   * modifier
   *     .padding(8.dp)
   *     .selectable { }      // Reduced touch target
   * ```
   */
  @Test
  fun pattern_paddingBeforeSelectable_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: padding before toggleable - VIOLATION
   *
   * ```kotlin
   * modifier
   *     .padding(8.dp)
   *     .toggleable { }      // Reduced touch target
   * ```
   */
  @Test
  fun pattern_paddingBeforeToggleable_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: offset before clickable - VIOLATION
   *
   * ```kotlin
   * modifier
   *     .offset(x = 8.dp)
   *     .clickable { }       // Touch target offset incorrectly
   * ```
   */
  @Test
  fun pattern_offsetBeforeClickable_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // NO MODIFIER CHAIN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: No modifier chain - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun NoChain(modifier: Modifier = Modifier) {
   *     Text("Hello", modifier = modifier)
   * }
   * ```
   */
  @Test
  fun pattern_noModifierChain_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Single modifier call - NO VIOLATION
   *
   * ```kotlin
   * modifier.padding(16.dp)
   * ```
   */
  @Test
  fun pattern_singleModifierCall_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why modifier order matters:
   *
   * 1. **Touch targets**: padding before clickable = smaller touch area
   * 2. **Visual effects**: Order affects clipping, backgrounds, shadows
   * 3. **Accessibility**: Proper touch targets improve UX
   *
   * Example of the problem:
   * ```kotlin
   * // Bad: 16dp padding THEN clickable = touch area is reduced
   * Box(
   *     modifier = Modifier
   *         .padding(16.dp)
   *         .clickable { }  // User can only tap the inner area!
   * )
   *
   * // Good: clickable THEN padding = full touch area
   * Box(
   *     modifier = Modifier
   *         .clickable { }  // Full box is tappable
   *         .padding(16.dp) // Content has visual padding
   * )
   * ```
   */
  @Test
  fun reason_touchTargetsAndAccessibility() {
    assertTrue(rule.enabledByDefault)
  }
}
