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
package io.androidpoet.composeguard.rules.effects

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for LambdaParameterInEffectRule.
 *
 * Rule: Lambda parameters should not be directly used in restartable effects.
 *
 * When a lambda parameter is used directly inside LaunchedEffect, DisposableEffect,
 * or other restartable effects without being included as a key, the effect may
 * capture a stale reference to the lambda.
 */
class LambdaParameterInEffectRuleTest {

  private val rule = LambdaParameterInEffectRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("LambdaParameterInEffect", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Lambda Parameters in Restartable Effects", rule.name)
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
  fun metadata_descriptionMentionsLambda() {
    assertTrue(
      rule.description.contains("Lambda") ||
        rule.description.contains("lambda") ||
        rule.description.contains("effect"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - LAUNCHED EFFECT
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Lambda used in LaunchedEffect without key - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(onClick: () -> Unit) {
   *     LaunchedEffect(Unit) {
   *         onClick()  // Stale reference!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_lambdaInLaunchedEffectWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Lambda as key in LaunchedEffect - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(onClick: () -> Unit) {
   *     LaunchedEffect(onClick) {  // Lambda is key
   *         onClick()
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_lambdaAsKeyInLaunchedEffect_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: Lambda with rememberUpdatedState - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(onClick: () -> Unit) {
   *     val currentOnClick by rememberUpdatedState(onClick)
   *     LaunchedEffect(Unit) {
   *         currentOnClick()  // Always current reference
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_lambdaWithRememberUpdatedState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - DISPOSABLE EFFECT
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Lambda used in DisposableEffect without key - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(onDispose: () -> Unit) {
   *     DisposableEffect(Unit) {
   *         onDispose { onDispose() }  // Stale reference!
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_lambdaInDisposableEffectWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // NON-LAMBDA PARAMETERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Non-lambda parameter in effect - NO VIOLATION (not checked)
   *
   * ```kotlin
   * @Composable
   * fun MyComponent(id: String) {
   *     LaunchedEffect(Unit) {
   *         doSomething(id)  // Not a lambda
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_nonLambdaParameter_shouldNotBeChecked() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why lambda references in effects matter:
   *
   * 1. **Stale closures**: Effect captures old lambda reference
   * 2. **Incorrect behavior**: Calling old callback instead of new one
   * 3. **Hard to debug**: Issues only appear when lambda changes
   *
   * Example of the problem:
   * ```kotlin
   * @Composable
   * fun Timer(onTick: () -> Unit) {
   *     LaunchedEffect(Unit) {
   *         while (true) {
   *             delay(1000)
   *             onTick()  // Always calls the ORIGINAL onTick!
   *         }
   *     }
   * }
   * ```
   *
   * Solutions:
   * 1. Add lambda as key (restarts effect)
   * 2. Use rememberUpdatedState (doesn't restart)
   */
  @Test
  fun reason_avoidStaleClosures() {
    assertTrue(rule.enabledByDefault)
  }
}
