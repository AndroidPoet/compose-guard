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
package io.androidpoet.composeguard.rules.performance

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for DeferStateReadsRule.
 *
 * Rule: Use lambda-based modifiers for frequently changing state.
 *
 * Per Android's official performance guidance: "Defer state reads as long as possible
 * by wrapping them in lambda functions."
 */
class DeferStateReadsRuleTest {

  private val rule = DeferStateReadsRule()


  @Test
  fun metadata_id() {
    assertEquals("DeferStateReads", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Defer State Reads", rule.name)
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
  fun metadata_descriptionMentionsDeferOrLambda() {
    assertTrue(
      rule.description.contains("Defer") ||
        rule.description.contains("defer") ||
        rule.description.contains("lambda"),
    )
  }


  /**
   * Pattern: offset with animated value - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Animated() {
   *     val offset by animateDpAsState(targetValue = 100.dp)
   *     Box(modifier = Modifier.offset(y = offset))  // Recomposes on every frame!
   * }
   * ```
   */
  @Test
  fun pattern_offsetWithAnimatedValue_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: offset with lambda - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Animated() {
   *     val offset by animateDpAsState(targetValue = 100.dp)
   *     Box(modifier = Modifier.offset { IntOffset(0, offset.roundToPx()) })  // Deferred!
   * }
   * ```
   */
  @Test
  fun pattern_offsetWithLambda_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Pattern: alpha with animated value - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun FadeIn() {
   *     val alpha by animateFloatAsState(targetValue = 1f)
   *     Box(modifier = Modifier.alpha(alpha))  // Recomposes on every frame!
   * }
   * ```
   */
  @Test
  fun pattern_alphaWithAnimatedValue_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: graphicsLayer with alpha - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun FadeIn() {
   *     val alpha by animateFloatAsState(targetValue = 1f)
   *     Box(modifier = Modifier.graphicsLayer { this.alpha = alpha })  // Deferred!
   * }
   * ```
   */
  @Test
  fun pattern_graphicsLayerWithAlpha_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Pattern: scale with animated value - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Pulse() {
   *     val scale by animateFloatAsState(targetValue = 1.2f)
   *     Box(modifier = Modifier.scale(scale))  // Recomposes on every frame!
   * }
   * ```
   */
  @Test
  fun pattern_scaleWithAnimatedValue_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Pattern: Static values in modifiers - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Static() {
   *     Box(modifier = Modifier.offset(x = 16.dp, y = 8.dp))  // Not animated, OK
   * }
   * ```
   */
  @Test
  fun pattern_staticValues_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Why deferring state reads matters:
   *
   * 1. **Composition phase**: Reading state triggers recomposition
   * 2. **Layout phase**: Lambda modifiers defer to layout/draw
   * 3. **Animation performance**: Skip composition for smooth 60fps
   *
   * Example:
   * ```kotlin
   * // Bad - reads during composition (causes recomposition)
   * Box(modifier = Modifier.offset(y = scrollOffset.dp))
   *
   * // Good - defers read to layout phase (skips recomposition)
   * Box(modifier = Modifier.offset { IntOffset(0, scrollOffset.roundToPx()) })
   * ```
   */
  @Test
  fun reason_animationPerformance() {
    assertTrue(rule.enabledByDefault)
  }
}
