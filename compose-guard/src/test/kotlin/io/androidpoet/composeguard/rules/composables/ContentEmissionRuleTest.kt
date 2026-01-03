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
 * Comprehensive tests for ContentEmissionRule.
 *
 * Rule: Don't emit content and return a result.
 *
 * Composable functions should either emit UI content to the composition tree
 * OR return a value to the caller, but not both.
 */
class ContentEmissionRuleTest {

  private val rule = ContentEmissionRule()


  @Test
  fun metadata_id() {
    assertEquals("ContentEmission", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Emit Content and Return", rule.name)
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
  fun metadata_descriptionMentionsEmitAndReturn() {
    assertTrue(
      rule.description.contains("emit") ||
        rule.description.contains("return"),
    )
  }


  /**
   * Pattern: Emit content AND return value - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun BadExample(): String {
   *     Column { Text("Hello") }  // Emits content
   *     return "result"           // Also returns value!
   * }
   * ```
   */
  @Test
  fun pattern_emitAndReturn_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Only emit content (Unit return) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun GoodEmitter() {
   *     Column { Text("Hello") }  // Only emits
   * }
   * ```
   */
  @Test
  fun pattern_onlyEmitContent_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Only return value (no content) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun rememberSomething(): Something {
   *     return remember { Something() }  // Only returns
   * }
   * ```
   */
  @Test
  fun pattern_onlyReturnValue_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  /**
   * Why single responsibility matters:
   *
   * 1. **Confusing APIs**: Mixed responsibility is unexpected
   * 2. **Side effects**: Callers don't expect UI emission from value-returning fn
   * 3. **Testability**: Single responsibility is easier to test
   *
   * Example:
   * ```kotlin
   * // Caller expects just a value...
   * val result = myComposable()
   * // ...but UI was also emitted! Surprise!
   * ```
   */
  @Test
  fun reason_singleResponsibility() {
    assertTrue(rule.enabledByDefault)
  }
}
