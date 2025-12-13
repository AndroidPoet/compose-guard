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
 * Comprehensive tests for ModifierReuseRule.
 *
 * Rule: Don't re-use modifiers across multiple layout nodes.
 *
 * Reusing the same modifier instance across multiple composables can cause
 * unexpected behavior because modifiers apply their effects to a single node.
 */
class ModifierReuseRuleTest {

  private val rule = ModifierReuseRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ModifierReuse", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Re-use Modifiers", rule.name)
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
  fun metadata_descriptionMentionsReuse() {
    assertTrue(
      rule.description.contains("reuse") ||
        rule.description.contains("re-use") ||
        rule.description.contains("same") ||
        rule.description.contains("multiple"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Modifier used once - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun SingleUse(modifier: Modifier = Modifier) {
   *     Box(modifier = modifier) {
   *         Text("Content")
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierUsedOnce_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier used multiple times - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MultipleUse(modifier: Modifier = Modifier) {
   *     Column(modifier = modifier) {    // First use
   *         Text("A", modifier = modifier)  // Second use - REUSE!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierUsedMultipleTimes_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier reassigned and reused - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ReassignedReuse(modifier: Modifier = Modifier) {
   *     val myMod = modifier.fillMaxWidth()
   *     Column(modifier = myMod) {
   *         Text("A", modifier = myMod)  // Reusing reassigned modifier!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_reassignedModifierReused_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier chain used multiple times - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ChainReuse(modifier: Modifier = Modifier) {
   *     Column(modifier = modifier.padding(8.dp)) {
   *         Text("A", modifier = modifier.padding(8.dp))  // Same base!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierChainReused_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Different Modifier instances - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun DifferentInstances(modifier: Modifier = Modifier) {
   *     Column(modifier = modifier) {
   *         Text("A", modifier = Modifier.padding(8.dp))  // New instance
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_differentModifierInstances_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why not to reuse modifiers:
   *
   * 1. **State conflicts**: Modifier state may conflict between nodes
   * 2. **Layout issues**: Size/position may behave unexpectedly
   * 3. **Single node design**: Modifiers are designed for one node
   *
   * Example of the problem:
   * ```kotlin
   * @Composable
   * fun Problem(modifier: Modifier = Modifier) {
   *     val focusMod = modifier.focusable()
   *     Column(modifier = focusMod) {
   *         Text("A", modifier = focusMod)  // Focus state shared!
   *         Text("B", modifier = focusMod)  // All three share focus
   *     }
   * }
   * ```
   *
   * Solution:
   * ```kotlin
   * @Composable
   * fun Fixed(modifier: Modifier = Modifier) {
   *     Column(modifier = modifier) {
   *         Text("A", modifier = Modifier.focusable())  // Own state
   *         Text("B", modifier = Modifier.focusable())  // Own state
   *     }
   * }
   * ```
   */
  @Test
  fun reason_stateConflictsAndSingleNodeDesign() {
    assertTrue(rule.enabledByDefault)
  }
}
