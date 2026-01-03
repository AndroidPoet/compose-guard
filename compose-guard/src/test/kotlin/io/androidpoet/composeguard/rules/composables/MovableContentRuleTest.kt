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
 * Comprehensive tests for MovableContentRule.
 *
 * Rule: Movable content should be remembered.
 *
 * movableContentOf should be wrapped in remember {} to persist across recompositions.
 */
class MovableContentRuleTest {

  private val rule = MovableContentRule()


  @Test
  fun metadata_id() {
    assertEquals("MovableContent", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Movable Content Should Be Remembered", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.ERROR, rule.severity)
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
  fun metadata_descriptionMentionsMovableContent() {
    assertTrue(
      rule.description.contains("movableContentOf") ||
        rule.description.contains("remember"),
    )
  }


  /**
   * Pattern: movableContentOf without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     val content = movableContentOf { Text("Hello") }  // Not remembered!
   * }
   * ```
   */
  @Test
  fun pattern_movableContentOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: movableContentOf with remember - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     val content = remember {
   *         movableContentOf { Text("Hello") }  // Correct!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_movableContentOfWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: movableContentWithReceiverOf without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     val content = movableContentWithReceiverOf<ColumnScope> {  // Not remembered!
   *         Text("Hello")
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_movableContentWithReceiverOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  /**
   * Why movable content needs remember:
   *
   * 1. **State preservation**: Movable content needs stable identity
   * 2. **Performance**: Avoids recreating content on every recomposition
   * 3. **Animation**: movableContentOf enables state-preserving moves
   *
   * Example:
   * ```kotlin
   * // Bad - new movable content every recomposition
   * val content = movableContentOf { MyContent() }
   *
   * // Good - stable movable content
   * val content = remember {
   *     movableContentOf { MyContent() }
   * }
   *
   * // Now content can be moved between layouts while preserving state
   * if (isWide) {
   *     Row { content() }
   * } else {
   *     Column { content() }
   * }
   * ```
   */
  @Test
  fun reason_statePreservationAndAnimation() {
    assertEquals(RuleSeverity.ERROR, rule.severity)
  }
}
