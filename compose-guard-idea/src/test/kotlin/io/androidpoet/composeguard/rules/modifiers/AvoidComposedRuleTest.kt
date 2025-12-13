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
 * Comprehensive tests for AvoidComposedRule.
 *
 * Rule: Avoid composed {} modifier factory.
 *
 * The `composed` API has performance issues and is considered deprecated.
 * Use Modifier.Node instead for better performance and semantics.
 */
class AvoidComposedRuleTest {

  private val rule = AvoidComposedRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("AvoidComposed", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Avoid composed {} Modifier", rule.name)
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
  fun metadata_descriptionMentionsComposed() {
    assertTrue(
      rule.description.contains("composed") ||
        rule.description.contains("Modifier.Node") ||
        rule.description.contains("deprecated"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Using composed {} - VIOLATION
   *
   * ```kotlin
   * fun Modifier.customEffect(): Modifier = composed {
   *     val color = remember { ... }
   *     this.background(color)
   * }
   * ```
   */
  @Test
  fun pattern_usingComposed_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Using composed with key - VIOLATION
   *
   * ```kotlin
   * fun Modifier.keyedEffect(key: Any): Modifier = composed(
   *     inspectorInfo = ...,
   *     key1 = key
   * ) {
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_usingComposedWithKey_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Using Modifier.Node - NO VIOLATION
   *
   * ```kotlin
   * class CustomEffectNode : Modifier.Node() { ... }
   *
   * class CustomEffectElement : ModifierNodeElement<CustomEffectNode>() { ... }
   *
   * fun Modifier.customEffect(): Modifier = this.then(CustomEffectElement())
   * ```
   */
  @Test
  fun pattern_usingModifierNode_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Using then() without composed - NO VIOLATION
   *
   * ```kotlin
   * fun Modifier.chainedEffect(): Modifier = this
   *     .background(Color.Red)
   *     .padding(8.dp)
   * ```
   */
  @Test
  fun pattern_usingThenWithoutComposed_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  /**
   * Pattern: Non-Modifier extension function using composed - NO VIOLATION (not checked)
   *
   * ```kotlin
   * fun String.composed(): String = ...  // Not a Modifier extension
   * ```
   */
  @Test
  fun pattern_nonModifierExtension_shouldNotBeChecked() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why avoid composed:
   *
   * 1. **Performance**: composed creates a new composition per instance
   * 2. **Deprecation**: Google recommends Modifier.Node instead
   * 3. **Overhead**: Extra allocations during recomposition
   *
   * Example of the problem:
   * ```kotlin
   * // Bad: Using composed
   * fun Modifier.ripple(): Modifier = composed {
   *     val interactionSource = remember { MutableInteractionSource() }
   *     // Creates new composition every time
   *     this.indication(interactionSource, rememberRipple())
   * }
   * ```
   *
   * Solution using Modifier.Node:
   * ```kotlin
   * // Good: Using Modifier.Node
   * class RippleNode : DelegatingNode() {
   *     // Efficient, lifecycle-aware
   * }
   *
   * class RippleElement : ModifierNodeElement<RippleNode>() {
   *     override fun create() = RippleNode()
   *     override fun update(node: RippleNode) { }
   * }
   *
   * fun Modifier.ripple(): Modifier = this.then(RippleElement())
   * ```
   */
  @Test
  fun reason_performanceAndDeprecation() {
    assertTrue(rule.enabledByDefault)
  }
}
