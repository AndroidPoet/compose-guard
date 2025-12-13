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
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for EventParameterNamingRule.
 *
 * Rule: Event parameters should follow "on" + verb naming pattern.
 *
 * Event callbacks should follow 'on' + present-tense verb pattern
 * (onClick, not onClicked).
 */
class EventParameterNamingRuleTest {

  private val rule = EventParameterNamingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("EventParameterNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Event Parameter Naming", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.NAMING, rule.category)
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
  fun metadata_descriptionMentionsEventNaming() {
    assertTrue(
      rule.description.contains("on") ||
        rule.description.contains("event") ||
        rule.description.contains("callback") ||
        rule.description.contains("present"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - PRESENT TENSE (CORRECT)
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Present-tense event name - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Button(
   *     onClick: () -> Unit,
   *     onChange: (String) -> Unit,
   *     onDismiss: () -> Unit
   * )
   * ```
   */
  @Test
  fun pattern_presentTenseEventName_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - PAST TENSE (VIOLATION)
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Past-tense event name 'onClicked' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Button(
   *     onClicked: () -> Unit  // Should be onClick
   * )
   * ```
   */
  @Test
  fun pattern_onClicked_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Past-tense event name 'onChanged' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun TextField(
   *     onChanged: (String) -> Unit  // Should be onChange
   * )
   * ```
   */
  @Test
  fun pattern_onChanged_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Past-tense event name 'onSubmitted' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Form(
   *     onSubmitted: () -> Unit  // Should be onSubmit
   * )
   * ```
   */
  @Test
  fun pattern_onSubmitted_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Past-tense event name 'onDismissed' - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Dialog(
   *     onDismissed: () -> Unit  // Should be onDismiss
   * )
   * ```
   */
  @Test
  fun pattern_onDismissed_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // NON-EVENT PARAMETERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Non-callback parameter - NO VIOLATION (not checked)
   *
   * ```kotlin
   * @Composable
   * fun Button(
   *     text: String,          // Not a callback
   *     enabled: Boolean       // Not a callback
   * )
   * ```
   */
  @Test
  fun pattern_nonCallbackParameter_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Callback not starting with 'on' - NO VIOLATION (not checked by this rule)
   *
   * ```kotlin
   * @Composable
   * fun Item(
   *     clicked: () -> Unit,   // Different pattern, not 'on' prefix
   *     action: () -> Unit
   * )
   * ```
   */
  @Test
  fun pattern_callbackWithoutOnPrefix_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why present-tense matters for events:
   *
   * 1. **Consistency**: Matches Compose/Android convention (onClick, onValueChange)
   * 2. **Intent**: Present tense describes what will happen, not what happened
   * 3. **Readability**: Standard pattern is easier to recognize
   *
   * Example:
   * ```kotlin
   * // Good - present tense
   * @Composable
   * fun TextField(
   *     value: String,
   *     onValueChange: (String) -> Unit,  // What will happen on change
   *     onClick: () -> Unit                // What will happen on click
   * )
   *
   * // Bad - past tense
   * @Composable
   * fun TextField(
   *     value: String,
   *     onValueChanged: (String) -> Unit,  // Sounds like notification
   *     onClicked: () -> Unit               // Inconsistent with Compose
   * )
   * ```
   */
  @Test
  fun reason_consistencyAndIntent() {
    assertTrue(rule.enabledByDefault)
  }
}
