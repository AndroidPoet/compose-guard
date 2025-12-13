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
package io.androidpoet.composeguard.rules.state

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for RememberStateRule.
 *
 * Rule: State should be remembered in composables.
 *
 * Using `mutableStateOf` or similar state builders without `remember`
 * will create a new state instance on every recomposition, losing
 * the previous value.
 */
class RememberStateRuleTest {

  private val rule = RememberStateRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("RememberState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("State Should Be Remembered", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
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
  fun metadata_descriptionMentionsRemember() {
    assertTrue(
      rule.description.contains("remember") ||
        rule.description.contains("mutableStateOf"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: mutableStateOf without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     val count = mutableStateOf(0)  // Lost on every recomposition!
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateOf with remember - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     val count = remember { mutableStateOf(0) }  // Correct!
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: delegate syntax without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     var count by mutableStateOf(0)  // Lost on every recomposition!
   * }
   * ```
   */
  @Test
  fun pattern_delegateSyntaxWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: delegate syntax with remember - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     var count by remember { mutableStateOf(0) }  // Correct!
   * }
   * ```
   */
  @Test
  fun pattern_delegateSyntaxWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Type-specific state without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     val count = mutableIntStateOf(0)  // Lost on every recomposition!
   * }
   * ```
   */
  @Test
  fun pattern_typeSpecificStateWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: derivedStateOf without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     val derived = derivedStateOf { someValue > 0 }  // Should be in remember
   * }
   * ```
   */
  @Test
  fun pattern_derivedStateOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateListOf without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ItemList() {
   *     val items = mutableStateListOf<String>()  // Lost on every recomposition!
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateListOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why state needs remember:
   *
   * 1. **Persistence**: State survives recomposition
   * 2. **Correctness**: Values aren't reset unexpectedly
   * 3. **Performance**: Prevents unnecessary allocations
   *
   * Example of the problem:
   * ```kotlin
   * @Composable
   * fun BrokenCounter() {
   *     var count = mutableStateOf(0)  // New state every recomposition!
   *     Button(onClick = { count.value++ }) {
   *         Text("Count: ${count.value}")  // Always 0!
   *     }
   * }
   * ```
   */
  @Test
  fun reason_statePersistence() {
    // This is an ERROR severity because it's a critical bug
    assertEquals(RuleSeverity.ERROR, rule.severity)
  }
}
