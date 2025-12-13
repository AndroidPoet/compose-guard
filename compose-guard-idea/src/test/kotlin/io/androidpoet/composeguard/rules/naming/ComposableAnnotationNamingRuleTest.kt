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
 * Comprehensive tests for ComposableAnnotationNamingRule.
 *
 * Rule: Custom composable annotations should end with "Composable" suffix.
 *
 * When creating custom annotations that are meant to be used on @Composable functions
 * (like custom previews or markers), they should follow the naming convention of
 * ending with "Composable" to make their purpose clear.
 */
class ComposableAnnotationNamingRuleTest {

  private val rule = ComposableAnnotationNamingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ComposableAnnotationNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Composable Annotation Naming", rule.name)
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
  fun metadata_descriptionMentionsComposable() {
    assertTrue(
      rule.description.contains("Composable") ||
        rule.description.contains("suffix") ||
        rule.description.contains("annotation"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Composable annotation with 'Composable' suffix - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * annotation class MyCustomComposable
   * ```
   */
  @Test
  fun pattern_withComposableSuffix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Composable annotation without 'Composable' suffix - VIOLATION
   *
   * ```kotlin
   * @Composable
   * annotation class MyCustom  // Should be MyCustomComposable
   * ```
   */
  @Test
  fun pattern_withoutComposableSuffix_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Non-composable annotation - NO VIOLATION (not checked)
   *
   * ```kotlin
   * annotation class MyAnnotation  // Not a composable annotation
   * ```
   */
  @Test
  fun pattern_nonComposableAnnotation_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Preview annotation with @Preview - allowed exception
   *
   * ```kotlin
   * @Preview
   * @Composable
   * annotation class MyCustomPreview  // Preview annotations are an exception
   * ```
   */
  @Test
  fun pattern_previewAnnotation_specialCase() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why 'Composable' suffix matters for annotations:
   *
   * 1. **Clarity**: Clear that annotation is related to Compose
   * 2. **Intent**: Shows the annotation is for composable functions
   * 3. **Discovery**: Easy to find Compose-related annotations
   *
   * Example:
   * ```kotlin
   * // Good - clearly a Compose-related annotation
   * @Composable
   * annotation class FeatureFlagComposable
   *
   * // Bad - not clear this is for composables
   * @Composable
   * annotation class FeatureFlag
   * ```
   */
  @Test
  fun reason_clarityAndIntent() {
    assertTrue(rule.enabledByDefault)
  }
}
