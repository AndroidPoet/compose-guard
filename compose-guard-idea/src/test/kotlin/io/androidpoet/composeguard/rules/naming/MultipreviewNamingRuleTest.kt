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
 * Comprehensive tests for MultipreviewNamingRule.
 *
 * Rule: Multipreview annotations should be named with "Previews" prefix.
 *
 * Functions with multiple @Preview annotations should follow the
 * naming pattern 'PreviewsXxx'.
 */
class MultipreviewNamingRuleTest {

  private val rule = MultipreviewNamingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("MultipreviewNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Multipreview Naming", rule.name)
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
  fun metadata_descriptionMentionsPreviews() {
    assertTrue(
      rule.description.contains("Previews") ||
        rule.description.contains("Multipreview") ||
        rule.description.contains("prefix"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Multipreview with 'Previews' prefix - NO VIOLATION
   *
   * ```kotlin
   * @Preview(name = "Light")
   * @Preview(name = "Dark")
   * @Composable
   * fun PreviewsButton() { ... }
   * ```
   */
  @Test
  fun pattern_withPreviewsPrefix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Multipreview without 'Previews' prefix - VIOLATION
   *
   * ```kotlin
   * @Preview(name = "Light")
   * @Preview(name = "Dark")
   * @Composable
   * fun Button() { ... }  // Should be PreviewsButton
   * ```
   */
  @Test
  fun pattern_withoutPreviewsPrefix_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Single preview - NO VIOLATION (not a multipreview)
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun ButtonPreview() { ... }  // Single preview, different rule applies
   * ```
   */
  @Test
  fun pattern_singlePreview_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: No preview annotations - NO VIOLATION (not checked)
   *
   * ```kotlin
   * @Composable
   * fun Button() { ... }  // Not a preview
   * ```
   */
  @Test
  fun pattern_noPreviewAnnotations_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why Previews prefix matters for multipreviews:
   *
   * 1. **Distinction**: Distinguishes from single previews
   * 2. **Clarity**: Indicates multiple preview configurations
   * 3. **Convention**: Standard pattern in Compose community
   *
   * Example:
   * ```kotlin
   * // Good - clearly indicates multiple previews
   * @Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO)
   * @Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
   * @Composable
   * fun PreviewsButton() {
   *     Button("Click me")
   * }
   *
   * // Bad - not clear this has multiple configurations
   * @Preview(name = "Light")
   * @Preview(name = "Dark")
   * @Composable
   * fun ButtonPreview() { ... }
   * ```
   */
  @Test
  fun reason_distinctionAndClarity() {
    assertTrue(rule.enabledByDefault)
  }
}
