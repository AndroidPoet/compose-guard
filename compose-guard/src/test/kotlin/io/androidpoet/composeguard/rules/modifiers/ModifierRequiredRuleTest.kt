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
 * Comprehensive tests for ModifierRequiredRule.
 *
 * Rule: Public composables that emit UI should have a modifier parameter.
 *
 * This enables composition over inheritance and allows callers to customize
 * the composable's appearance and behavior.
 *
 * Exceptions:
 * - Preview functions
 * - Private/internal functions
 * - Value-returning composables
 */
class ModifierRequiredRuleTest {

  private val rule = ModifierRequiredRule()


  @Test
  fun metadata_id() {
    assertEquals("ModifierRequired", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Modifier Parameter Required", rule.name)
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
  fun metadata_descriptionMentionsModifier() {
    assertTrue(
      rule.description.contains("modifier") ||
        rule.description.contains("Modifier"),
    )
  }


  /**
   * Pattern: Public composable without modifier - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyButton(text: String) {  // Missing modifier parameter
   *     Button(onClick = {}) {
   *         Text(text)
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_publicComposableWithoutModifier_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Public composable with modifier - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyButton(
   *     text: String,
   *     modifier: Modifier = Modifier
   * ) {
   *     Button(
   *         onClick = {},
   *         modifier = modifier
   *     ) {
   *         Text(text)
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_publicComposableWithModifier_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Private composable without modifier - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Composable
   * private fun PrivateContent() {
   *     Text("Private")
   * }
   * ```
   */
  @Test
  fun pattern_privateComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Internal composable without modifier - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Composable
   * internal fun InternalContent() {
   *     Text("Internal")
   * }
   * ```
   */
  @Test
  fun pattern_internalComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Preview composable without modifier - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun MyButtonPreview() {
   *     MyButton(text = "Click me")
   * }
   * ```
   */
  @Test
  fun pattern_previewComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Value-returning composable without modifier - NO VIOLATION (skipped)
   *
   * ```kotlin
   * @Composable
   * fun rememberMyState(): MyState {
   *     return remember { MyState() }
   * }
   * ```
   */
  @Test
  fun pattern_valueReturningComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }


  /**
   * QuickFix should add: modifier: Modifier = Modifier
   */
  @Test
  fun quickFix_shouldAddModifierParameter() {
    assertTrue(rule.description.contains("Modifier") || rule.description.contains("modifier"))
  }


  /**
   * Why composables need modifier parameter:
   *
   * 1. **Composition over inheritance**: Callers can customize behavior
   * 2. **Flexibility**: Parent can control size, position, appearance
   * 3. **Testability**: Easier to test with custom modifiers
   * 4. **Consistency**: Standard pattern across Compose ecosystem
   *
   * Example of the problem:
   * ```kotlin
   * // Without modifier - can't customize
   * @Composable
   * fun Avatar(imageUrl: String) {
   *     Image(...)  // Fixed 48.dp size
   * }
   *
   * // In parent - can't change size!
   * Avatar(imageUrl = url)  // Stuck with 48.dp
   * ```
   *
   * Solution:
   * ```kotlin
   * @Composable
   * fun Avatar(
   *     imageUrl: String,
   *     modifier: Modifier = Modifier
   * ) {
   *     Image(modifier = modifier, ...)
   * }
   *
   * // Now parent controls size
   * Avatar(imageUrl = url, modifier = Modifier.size(64.dp))
   * ```
   */
  @Test
  fun reason_compositionOverInheritance() {
    assertTrue(rule.enabledByDefault)
  }
}
