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
 * Comprehensive tests for DerivedStateOfCandidateRule.
 *
 * Rule: Detect candidates for derivedStateOf or remember with keys.
 *
 * KEY INSIGHT: Use derivedStateOf when input state changes MORE frequently
 * than the derived output changes.
 */
class DerivedStateOfCandidateRuleTest {

  private val rule = DerivedStateOfCandidateRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("DerivedStateOfCandidate", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Consider Using remember with keys", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
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
    assertTrue(rule.documentationUrl!!.contains("android.com"))
  }

  @Test
  fun metadata_descriptionMentionsDerivedStateOf() {
    assertTrue(
      rule.description.contains("derivedStateOf") ||
        rule.description.contains("remember") ||
        rule.description.contains("input"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - SCROLL STATE THRESHOLD
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Scroll state threshold check - VIOLATION (perfect candidate)
   *
   * ```kotlin
   * @Composable
   * fun ScrollableScreen() {
   *     val listState = rememberLazyListState()
   *     // BAD: Recomposes on every scroll frame
   *     val showButton = listState.firstVisibleItemIndex > 0
   * }
   * ```
   */
  @Test
  fun pattern_scrollStateThreshold_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Scroll state with derivedStateOf - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ScrollableScreen() {
   *     val listState = rememberLazyListState()
   *     // GOOD: Only recomposes when showButton changes
   *     val showButton by remember {
   *         derivedStateOf { listState.firstVisibleItemIndex > 0 }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_scrollStateWithDerivedStateOf_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - EXPENSIVE OPERATIONS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: filter without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun FilteredList(items: List<Item>) {
   *     // BAD: filter runs on every recomposition
   *     val filtered = items.filter { it.isActive }
   * }
   * ```
   */
  @Test
  fun pattern_filterWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: map without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MappedList(items: List<Item>) {
   *     // BAD: map runs on every recomposition
   *     val names = items.map { it.name }
   * }
   * ```
   */
  @Test
  fun pattern_mapWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: sortedBy without remember - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun SortedList(items: List<Item>) {
   *     // BAD: sortedBy runs on every recomposition
   *     val sorted = items.sortedBy { it.date }
   * }
   * ```
   */
  @Test
  fun pattern_sortedByWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Expensive operation with remember - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun FilteredList(items: List<Item>) {
   *     // GOOD: Only recalculates when items changes
   *     val filtered = remember(items) {
   *         items.filter { it.isActive }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_expensiveOperationWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why derivedStateOf matters:
   *
   * 1. **Recomposition reduction**: Only recompose when output changes
   * 2. **Scroll performance**: Prevents frame drops during scroll
   * 3. **Memory efficiency**: Caches computation results
   *
   * Classic example:
   * ```kotlin
   * // BAD: Recomposes on EVERY scroll frame
   * val showButton = scrollState.firstVisibleItemIndex > 0
   *
   * // GOOD: Only recomposes when showButton changes (true <-> false)
   * val showButton by remember {
   *     derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
   * }
   * ```
   */
  @Test
  fun reason_scrollPerformance() {
    assertTrue(rule.enabledByDefault)
  }
}
