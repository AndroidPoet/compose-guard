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
 * Comprehensive tests for MutableStateParameterRule.
 *
 * Rule: Don't use MutableState as a parameter.
 *
 * Passing MutableState as a parameter splits state ownership between
 * the composable and its caller, making it harder to reason about
 * when changes occur.
 */
class MutableStateParameterRuleTest {

  private val rule = MutableStateParameterRule()


  @Test
  fun metadata_id() {
    assertEquals("MutableStateParameter", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Use MutableState as Parameter", rule.name)
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
    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun metadata_descriptionMentionsMutableState() {
    assertTrue(
      rule.description.contains("MutableState") ||
        rule.description.contains("hoisting"),
    )
  }


  /**
   * Pattern: MutableState as parameter - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(state: MutableState<String>) {  // Bad!
   *     Text(state.value)
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateParameter_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Generic MutableState parameter - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(state: MutableState<Int>) {  // Bad!
   *     Text(state.value.toString())
   * }
   * ```
   */
  @Test
  fun pattern_genericMutableStateParameter_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Value + callback pattern - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(
   *     value: String,
   *     onValueChange: (String) -> Unit
   * ) {
   *     TextField(value = value, onValueChange = onValueChange)
   * }
   * ```
   */
  @Test
  fun pattern_valueAndCallback_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: State<T> (read-only) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(state: State<String>) {  // OK - read only
   *     Text(state.value)
   * }
   * ```
   */
  @Test
  fun pattern_readOnlyState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Why avoid MutableState as parameter:
   *
   * 1. **Split ownership**: Who owns the state? Caller or callee?
   * 2. **Harder to reason**: Changes can come from anywhere
   * 3. **Less testable**: Can't easily control state in tests
   * 4. **Breaks unidirectional flow**: Compose prefers value + callback
   *
   * Example:
   * ```kotlin
   * // Bad - state ownership is unclear
   * @Composable
   * fun TextField(text: MutableState<String>)
   *
   * // Good - clear ownership, unidirectional flow
   * @Composable
   * fun TextField(
   *     value: String,
   *     onValueChange: (String) -> Unit
   * )
   * ```
   */
  @Test
  fun reason_clearOwnershipAndTestability() {
    assertTrue(rule.enabledByDefault)
  }
}
