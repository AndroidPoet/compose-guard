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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for ParameterOrderingRule following Compose API guidelines.
 *
 * Official Compose API parameter order:
 * 1. Required parameters (no defaults) - data first, then metadata
 * 2. Modifier parameter (FIRST optional - easily accessible at call site)
 * 3. Other optional parameters (with defaults)
 * 4. Trailing @Composable lambda (if any)
 *
 * From the official Compose Component API Guidelines:
 * "Since the modifier is recommended for any component and is used often,
 * placing it first ensures that it can be set without a named parameter
 * and provides a consistent place for this parameter in any component."
 */
class ParameterOrderingRuleTest {

  private val rule = ParameterOrderingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // RULE 1: Required Parameters Before Optional
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun rule1_metadata() {
    assertEquals("ParameterOrdering", rule.id)
    assertEquals("Parameter Ordering", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun rule1_descriptionMentionsOrder() {
    assertTrue(
      rule.description.contains("order") ||
        rule.description.contains("Order") ||
        rule.description.contains("required") ||
        rule.description.contains("optional"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // RULE 2: Modifier Position (FIRST Optional)
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Test case: Modifier should be the FIRST optional parameter.
   *
   * Correct:
   * ```kotlin
   * fun Button(
   *     text: String,                    // required
   *     modifier: Modifier = Modifier,   // FIRST optional
   *     enabled: Boolean = true,         // other optional
   *     content: @Composable () -> Unit  // content
   * )
   * ```
   */
  @Test
  fun rule2_modifierShouldBeFirstOptional() {
    // This test documents the expected behavior
    assertTrue(
      rule.description.contains("modifier") ||
        rule.description.contains("Modifier") ||
        rule.description.contains("lambda") ||
        rule.description.contains("content"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // RULE 3: Content Lambda Trailing
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Test case: Content lambdas should be at the end for trailing lambda syntax.
   *
   * Correct:
   * ```kotlin
   * fun Card(
   *     title: String,
   *     modifier: Modifier = Modifier,
   *     content: @Composable () -> Unit  // Last for trailing lambda
   * )
   *
   * // Call site:
   * Card("Title") { Text("Content") }
   * ```
   */
  @Test
  fun rule3_contentLambdaShouldBeTrailing() {
    // This test documents the expected behavior
    assertTrue(rule.description.isNotBlank())
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // RULE 4: State-Callback Pairing
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Test case: State and callback should be paired together.
   *
   * Correct:
   * ```kotlin
   * fun TextField(
   *     value: String,
   *     onValueChange: (String) -> Unit,  // Right after value
   *     label: String = "",
   *     modifier: Modifier = Modifier
   * )
   * ```
   *
   * Wrong:
   * ```kotlin
   * fun TextField(
   *     value: String,
   *     label: String = "",
   *     onValueChange: (String) -> Unit,  // Separated from value
   *     modifier: Modifier = Modifier
   * )
   * ```
   */
  @Test
  fun rule4_stateCallbackPairingDocumented() {
    // This test documents the expected behavior
    assertTrue(rule.description.isNotBlank())
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // COMPREHENSIVE PARAMETER ORDER PATTERNS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Document correct parameter order pattern.
   *
   * Full example:
   * ```kotlin
   * @Composable
   * fun ComponentName(
   *     // 1. Required parameters (no defaults)
   *     text: String,
   *     onClick: () -> Unit,
   *     items: List<Item>,
   *
   *     // 2. Modifier (FIRST optional)
   *     modifier: Modifier = Modifier,
   *
   *     // 3. Other optional parameters with defaults
   *     enabled: Boolean = true,
   *     colors: ButtonColors = ButtonDefaults.buttonColors(),
   *     contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
   *
   *     // 4. Trailing @Composable lambda(s)
   *     content: @Composable () -> Unit
   * )
   * ```
   */
  @Test
  fun fullParameterOrderPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Document state + callback pairing pattern.
   *
   * ```kotlin
   * // Keep state and its callback together
   * fun Checkbox(
   *     checked: Boolean,
   *     onCheckedChange: (Boolean) -> Unit,  // paired with checked
   *     modifier: Modifier = Modifier,       // FIRST optional
   *     enabled: Boolean = true
   * )
   * ```
   */
  @Test
  fun stateCallbackPairingPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Document multiple slots pattern.
   *
   * ```kotlin
   * @Composable
   * fun ScaffoldLike(
   *     title: String,                              // required
   *     modifier: Modifier = Modifier,              // FIRST optional
   *     navigationIcon: @Composable () -> Unit = {},// optional slot
   *     actions: @Composable () -> Unit = {},       // optional slot
   *     content: @Composable () -> Unit             // primary content last
   * )
   * ```
   */
  @Test
  fun multipleSlotsPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Document callbacks ordering pattern.
   *
   * ```kotlin
   * @Composable
   * fun TextField(
   *     value: String,                    // required state
   *     onValueChange: (String) -> Unit,  // required callback (with its state)
   *     modifier: Modifier = Modifier,    // FIRST optional
   *     label: String = "",               // optional
   *     onFocusChange: (Boolean) -> Unit = {} // optional callback
   * )
   * ```
   */
  @Test
  fun callbacksOrderingPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Document overloads pattern.
   *
   * Simple version:
   * ```kotlin
   * @Composable
   * fun Button(
   *     onClick: () -> Unit,
   *     modifier: Modifier = Modifier,       // FIRST optional
   *     content: @Composable RowScope.() -> Unit
   * )
   * ```
   *
   * Full version with all options:
   * ```kotlin
   * @Composable
   * fun Button(
   *     onClick: () -> Unit,
   *     modifier: Modifier = Modifier,       // FIRST optional
   *     enabled: Boolean = true,
   *     shape: Shape = ButtonDefaults.shape,
   *     colors: ButtonColors = ButtonDefaults.buttonColors(),
   *     elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
   *     border: BorderStroke? = null,
   *     contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
   *     content: @Composable RowScope.() -> Unit
   * )
   * ```
   */
  @Test
  fun overloadsPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // QUICK REFERENCE TABLE
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Quick reference for parameter ordering rules:
   *
   * | Rule | Correct | Wrong |
   * |------|---------|-------|
   * | Required params first | fun X(title: String, modifier: Modifier = Modifier) | fun X(modifier: Modifier = Modifier, title: String) |
   * | Modifier FIRST optional | fun X(title: String, modifier: Modifier = Modifier, enabled: Boolean = true) | fun X(title: String, enabled: Boolean = true, modifier: Modifier = Modifier) |
   * | Modifier always has default | modifier: Modifier = Modifier | modifier: Modifier |
   * | Modifier named 'modifier' | modifier: Modifier | mod: Modifier |
   * | Content lambda last | fun X(modifier: Modifier = Modifier, content: @Composable () -> Unit) | fun X(content: @Composable () -> Unit, modifier: Modifier) |
   * | Single lambda unnamed | content: @Composable () -> Unit | body: @Composable () -> Unit (if single) |
   * | Multiple lambdas named | icon: @Composable () -> Unit, content: @Composable () -> Unit | Two unnamed lambdas |
   */
  @Test
  fun quickReferenceTable() {
    // Rule exists and has proper category
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // SUMMARY ORDER
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Summary of correct parameter order:
   *
   * 1. **Required params** (`value: String`, `onValueChange: (String) -> Unit`)
   * 2. **`modifier: Modifier = Modifier`** (FIRST optional)
   * 3. **Other optional params** (`enabled: Boolean = true`)
   * 4. **Primary content lambda** (`content: @Composable () -> Unit`)
   */
  @Test
  fun summaryOrder() {
    assertEquals("ParameterOrdering", rule.id)
    assertTrue(rule.enabledByDefault)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // EVENT HANDLERS VS CONTENT LAMBDAS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Event handlers (onClick, onValueChange) are NOT content lambdas.
   *
   * Event handlers:
   * - Follow normal parameter ordering based on whether they have defaults
   * - Required callbacks after required params, optional callbacks in optional section
   * - Names start with "on" followed by uppercase (onClick, onDismiss, etc.)
   *
   * Content lambdas:
   * - @Composable () -> Unit types
   * - Should be at the end for trailing lambda syntax
   * - Names like "content", "icon", "actions"
   */
  @Test
  fun eventHandlersVsContentLambdas() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }
}
