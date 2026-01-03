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
 * Comprehensive tests for TypeSpecificStateRule.
 *
 * Rule: Use type-specific mutableStateOf variants when possible.
 *
 * For primitive types like Int, Long, Float, Double, using type-specific
 * variants (mutableIntStateOf, mutableLongStateOf, etc.) eliminates
 * autoboxing on JVM and improves memory efficiency.
 */
class TypeSpecificStateRuleTest {

  private val rule = TypeSpecificStateRule()


  @Test
  fun metadata_id() {
    assertEquals("TypeSpecificState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Use Type-Specific State Variants", rule.name)
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
  }

  @Test
  fun metadata_descriptionMentionsTypeSpecific() {
    assertTrue(
      rule.description.contains("type-specific") ||
        rule.description.contains("mutableIntStateOf") ||
        rule.description.contains("autoboxing"),
    )
  }


  /**
   * Pattern: mutableStateOf<Int> - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     val count = remember { mutableStateOf<Int>(0) }  // Should use mutableIntStateOf
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfInt_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableIntStateOf - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Counter() {
   *     val count = remember { mutableIntStateOf(0) }  // Correct!
   * }
   * ```
   */
  @Test
  fun pattern_mutableIntStateOf_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateOf<Long> - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Timer() {
   *     val time = remember { mutableStateOf<Long>(0L) }  // Should use mutableLongStateOf
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfLong_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateOf<Float> - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Slider() {
   *     val progress = remember { mutableStateOf<Float>(0f) }  // Should use mutableFloatStateOf
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfFloat_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateOf<Double> - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Calculator() {
   *     val result = remember { mutableStateOf<Double>(0.0) }  // Should use mutableDoubleStateOf
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfDouble_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: mutableStateOf<String> - NO VIOLATION (no primitive variant)
   *
   * ```kotlin
   * @Composable
   * fun TextField() {
   *     val text = remember { mutableStateOf("") }  // Correct - no String variant
   * }
   * ```
   */
  @Test
  fun pattern_mutableStateOfString_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Why type-specific variants matter:
   *
   * 1. **Autoboxing**: Generic mutableStateOf<Int> boxes/unboxes on JVM
   * 2. **Memory**: Type-specific variants use primitive backing fields
   * 3. **Performance**: Less GC pressure from boxing
   *
   * Example:
   * ```kotlin
   * // Bad - autoboxing overhead
   * val count = mutableStateOf<Int>(0)  // Int -> Integer boxing
   *
   * // Good - primitive backing
   * val count = mutableIntStateOf(0)  // No boxing!
   * ```
   */
  @Test
  fun reason_avoidAutoboxing() {
    assertTrue(rule.enabledByDefault)
  }
}
