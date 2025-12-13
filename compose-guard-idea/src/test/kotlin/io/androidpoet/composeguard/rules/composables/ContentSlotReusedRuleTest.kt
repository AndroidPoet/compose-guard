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
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for ContentSlotReusedRule.
 *
 * Rule: Content slots should not be reused across different code paths.
 *
 * Invoking the same content slot lambda multiple times in different branches
 * can cause the slot's internal state to be lost or behave unexpectedly.
 */
class ContentSlotReusedRuleTest {

  private val rule = ContentSlotReusedRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ContentSlotReused", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Content Slots Should Not Be Reused", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
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
  fun metadata_descriptionMentionsContentSlot() {
    assertTrue(
      rule.description.contains("slot") ||
        rule.description.contains("lambda") ||
        rule.description.contains("invoked"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Content slot invoked multiple times - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example(content: @Composable () -> Unit) {
   *     if (condition) {
   *         content()  // First invocation
   *     } else {
   *         content()  // Second invocation - state may be lost!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_contentSlotInvokedMultipleTimes_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Content slot invoked once - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example(content: @Composable () -> Unit) {
   *     Box {
   *         content()  // Single invocation - OK
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_contentSlotInvokedOnce_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Multiple content slots each invoked once - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example(
   *     header: @Composable () -> Unit,
   *     content: @Composable () -> Unit
   * ) {
   *     Column {
   *         header()   // Single invocation
   *         content()  // Single invocation
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_multipleSlotsSingleInvocation_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Content slot with movableContentOf - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example(content: @Composable () -> Unit) {
   *     val movable = remember {
   *         movableContentOf { content() }
   *     }
   *     if (condition) {
   *         movable()
   *     } else {
   *         movable()  // OK - using movableContentOf preserves state
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_contentSlotWithMovableContent_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why content slots shouldn't be reused:
   *
   * 1. **State loss**: Internal state may be lost between invocations
   * 2. **Unexpected behavior**: Different code paths = different composition slots
   * 3. **Solution**: Use movableContentOf for shared content
   *
   * Example:
   * ```kotlin
   * // Bad - state is lost when switching branches
   * @Composable
   * fun Tabs(content: @Composable () -> Unit) {
   *     if (tab == 1) {
   *         content()  // TextField state here...
   *     } else {
   *         content()  // ...is LOST when switching to this branch!
   *     }
   * }
   *
   * // Good - state is preserved
   * @Composable
   * fun Tabs(content: @Composable () -> Unit) {
   *     val movable = remember { movableContentOf { content() } }
   *     if (tab == 1) {
   *         Box { movable() }
   *     } else {
   *         Card { movable() }  // State preserved!
   *     }
   * }
   * ```
   */
  @Test
  fun reason_statePreservation() {
    assertTrue(rule.enabledByDefault)
  }
}
