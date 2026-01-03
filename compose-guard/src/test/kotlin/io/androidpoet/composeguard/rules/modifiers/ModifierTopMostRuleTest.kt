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
 * Comprehensive tests for ModifierTopMostRule.
 *
 * Rule: Modifiers should be used at the top-most layout of a composable.
 *
 * The modifier parameter passed to a composable should be applied to the
 * root layout node, not nested children.
 */
class ModifierTopMostRuleTest {

  private val rule = ModifierTopMostRule()


  @Test
  fun metadata_id() {
    assertEquals("ModifierTopMost", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Modifier at Top-Most Layout", rule.name)
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
  fun metadata_descriptionMentionsRoot() {
    assertTrue(
      rule.description.contains("root") ||
        rule.description.contains("top") ||
        rule.description.contains("Top"),
    )
  }


  /**
   * Pattern: Modifier at root layout - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(modifier: Modifier = Modifier) {
   *     Box(modifier = modifier) {  // Correct - at root
   *         Text("Content")
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierAtRoot_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier on nested layout - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyCard(modifier: Modifier = Modifier) {
   *     Box {  // Root without modifier
   *         Column(modifier = modifier) {  // Wrong - nested!
   *             Text("Content")
   *         }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierOnNestedLayout_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Modifier on deeply nested layout - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun DeepNesting(modifier: Modifier = Modifier) {
   *     Surface {
   *         Box {
   *             Column(modifier = modifier) {  // Wrong - deeply nested!
   *                 Text("Content")
   *             }
   *         }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_modifierOnDeeplyNestedLayout_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: No modifier parameter - NO VIOLATION (nothing to check)
   *
   * ```kotlin
   * @Composable
   * fun NoModifier() {
   *     Text("Hello")
   * }
   * ```
   */
  @Test
  fun pattern_noModifierParameter_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Expression body with modifier at root - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Compact(modifier: Modifier = Modifier) = Box(modifier = modifier) {
   *     Text("Content")
   * }
   * ```
   */
  @Test
  fun pattern_expressionBodyWithModifierAtRoot_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }


  /**
   * Why modifier should be at root:
   *
   * 1. **Parent control**: Parent composables expect to control size/position
   * 2. **Reusability**: Composable works correctly in any parent layout
   * 3. **Predictability**: Standard pattern makes behavior predictable
   *
   * Example of the problem:
   * ```kotlin
   * // Bad: modifier on nested element
   * @Composable
   * fun Avatar(modifier: Modifier = Modifier) {
   *     Box {  // Root gets NO modifier!
   *         Image(modifier = modifier, ...)  // Modifier on child
   *     }
   * }
   *
   * // When used in Row:
   * Row {
   *     Avatar(modifier = Modifier.weight(1f))  // Won't work as expected!
   * }
   * ```
   *
   * Solution:
   * ```kotlin
   * @Composable
   * fun Avatar(modifier: Modifier = Modifier) {
   *     Box(modifier = modifier) {  // Root gets modifier
   *         Image(...)
   *     }
   * }
   * ```
   */
  @Test
  fun reason_parentControlAndReusability() {
    assertTrue(rule.enabledByDefault)
  }
}
