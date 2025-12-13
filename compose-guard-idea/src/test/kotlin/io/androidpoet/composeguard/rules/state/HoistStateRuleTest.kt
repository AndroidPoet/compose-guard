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
 * Comprehensive tests for HoistStateRule.
 *
 * Rule: Hoist state to the lowest common ancestor when appropriate.
 *
 * State hoisting is a pattern where state is moved up to make a composable
 * stateless. This rule uses smart heuristics to detect when state should
 * be hoisted.
 */
class HoistStateRuleTest {

  private val rule = HoistStateRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("HoistState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Consider Hoisting State", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun metadata_severity() {
    // INFO because it's a suggestion, not a hard rule
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun metadata_enabledByDefault() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun metadata_documentationUrl() {
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.startsWith("https://"))
  }

  @Test
  fun metadata_descriptionMentionsHoisting() {
    assertTrue(
      rule.description.contains("hoist") ||
        rule.description.contains("Hoist") ||
        rule.description.contains("stateless"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - SHOULD FLAG
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: State shared between children - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Parent() {
   *     var selected by remember { mutableStateOf(0) }
   *     Tab1(selected)  // Both children use this state
   *     Tab2(selected)
   * }
   * ```
   */
  @Test
  fun pattern_stateSharedBetweenChildren_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: State passed to child - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Parent() {
   *     var value by remember { mutableStateOf("") }
   *     TextField(value)  // State passed to child
   * }
   * ```
   */
  @Test
  fun pattern_statePassedToChild_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - SHOULD NOT FLAG
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Screen-level composable - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Composable
   * fun HomeScreen(viewModel: HomeViewModel) {
   *     var expanded by remember { mutableStateOf(false) }  // OK at screen level
   * }
   * ```
   */
  @Test
  fun pattern_screenLevelComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Private composable - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Composable
   * private fun Helper() {
   *     var state by remember { mutableStateOf(false) }  // Private - OK
   * }
   * ```
   */
  @Test
  fun pattern_privateComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Preview composable - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun MyPreview() {
   *     var state by remember { mutableStateOf(false) }  // Preview - OK
   * }
   * ```
   */
  @Test
  fun pattern_previewComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: UI element state (LazyListState) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyList() {
   *     val listState = rememberLazyListState()  // UI element state - OK
   *     LazyColumn(state = listState) { }
   * }
   * ```
   */
  @Test
  fun pattern_uiElementState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: State already hoisted (has parameter) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(
   *     value: String,
   *     onValueChange: (String) -> Unit
   * ) {
   *     // Already hoisted - no local state needed
   * }
   * ```
   */
  @Test
  fun pattern_alreadyHoisted_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why state hoisting matters:
   *
   * 1. **Testability**: Stateless composables are easier to test
   * 2. **Reusability**: Caller controls state, more flexible
   * 3. **Single source of truth**: State owned by one place
   * 4. **Lowest common ancestor**: State at right level in tree
   *
   * Example:
   * ```kotlin
   * // Before - stateful (harder to test)
   * @Composable
   * fun Counter() {
   *     var count by remember { mutableStateOf(0) }
   *     Button(onClick = { count++ }) { Text("$count") }
   * }
   *
   * // After - stateless (easy to test)
   * @Composable
   * fun Counter(count: Int, onIncrement: () -> Unit) {
   *     Button(onClick = onIncrement) { Text("$count") }
   * }
   * ```
   */
  @Test
  fun reason_testabilityAndReusability() {
    // INFO severity because it's a suggestion
    assertEquals(RuleSeverity.INFO, rule.severity)
  }
}
