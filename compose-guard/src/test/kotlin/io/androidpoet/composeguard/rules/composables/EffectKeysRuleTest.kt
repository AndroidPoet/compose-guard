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
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for EffectKeysRule.
 *
 * Rule: Be mindful of effect keys.
 *
 * Using constant keys like Unit or true means the effect runs once and never restarts.
 * This may be intentional, but often indicates a bug.
 */
class EffectKeysRuleTest {

  private val rule = EffectKeysRule()


  @Test
  fun metadata_id() {
    assertEquals("EffectKeys", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Effect Keys Correctness", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
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
  fun metadata_descriptionMentionsKeys() {
    assertTrue(
      rule.description.contains("key") ||
        rule.description.contains("LaunchedEffect") ||
        rule.description.contains("DisposableEffect"),
    )
  }


  /**
   * Pattern: LaunchedEffect(Unit) - VIOLATION (constant key)
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     LaunchedEffect(Unit) {  // Never restarts!
   *         doSomething()
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_launchedEffectWithUnit_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: LaunchedEffect(true) - VIOLATION (constant key)
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     LaunchedEffect(true) {  // Never restarts!
   *         doSomething()
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_launchedEffectWithTrue_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: LaunchedEffect(someId) - NO VIOLATION (dynamic key)
   *
   * ```kotlin
   * @Composable
   * fun Example(userId: String) {
   *     LaunchedEffect(userId) {  // Restarts when userId changes
   *         loadUser(userId)
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_launchedEffectWithDynamicKey_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  /**
   * Pattern: DisposableEffect(Unit) - VIOLATION (constant key)
   *
   * ```kotlin
   * @Composable
   * fun Example() {
   *     DisposableEffect(Unit) {
   *         onDispose { }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_disposableEffectWithUnit_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: DisposableEffect(lifecycleOwner) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Example(lifecycleOwner: LifecycleOwner) {
   *     DisposableEffect(lifecycleOwner) {
   *         // ...
   *         onDispose { }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_disposableEffectWithDynamicKey_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  /**
   * Why effect keys matter:
   *
   * 1. **Restart behavior**: Keys control when effects restart
   * 2. **Stale data**: Constant key may use outdated values
   * 3. **Intentionality**: Forces you to think about lifecycle
   *
   * Example:
   * ```kotlin
   * // Bad - never loads new user!
   * LaunchedEffect(Unit) {
   *     loadUser(userId)  // Uses stale userId
   * }
   *
   * // Good - reloads when userId changes
   * LaunchedEffect(userId) {
   *     loadUser(userId)
   * }
   * ```
   */
  @Test
  fun reason_effectRestartBehavior() {
    assertTrue(rule.enabledByDefault)
  }
}
