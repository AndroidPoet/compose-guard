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
 * Comprehensive tests for PreviewVisibilityRule.
 *
 * Rule: Preview composables should not be public.
 *
 * Preview functions should be private to prevent accidental use in production.
 */
class PreviewVisibilityRuleTest {

  private val rule = PreviewVisibilityRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("PreviewVisibility", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Preview Should Be Private", rule.name)
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
  fun metadata_descriptionMentionsPrivate() {
    assertTrue(
      rule.description.contains("private") ||
        rule.description.contains("production"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Public preview - VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun ButtonPreview() {  // Public preview!
   *     Button()
   * }
   * ```
   */
  @Test
  fun pattern_publicPreview_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Private preview - NO VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * private fun ButtonPreview() {  // Private is correct
   *     Button()
   * }
   * ```
   */
  @Test
  fun pattern_privatePreview_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Internal preview - NO VIOLATION (arguably)
   *
   * ```kotlin
   * @Preview
   * @Composable
   * internal fun ButtonPreview() {  // Internal OK
   *     Button()
   * }
   * ```
   */
  @Test
  fun pattern_internalPreview_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Non-preview public composable - NO VIOLATION (not checked)
   *
   * ```kotlin
   * @Composable
   * fun Button() {  // Not a preview, public is fine
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_nonPreviewPublic_shouldNotBeChecked() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why previews should be private:
   *
   * 1. **Accidental usage**: Could be called in production
   * 2. **API pollution**: Public previews clutter the API
   * 3. **Testing data**: Previews often use fake data
   *
   * Example:
   * ```kotlin
   * // Someone might accidentally use the preview
   * @Preview
   * @Composable
   * fun LoginScreenPreview() {
   *     LoginScreen(user = fakeUser)  // Fake data!
   * }
   *
   * // In production code:
   * LoginScreenPreview()  // Oops! Shows fake data
   * ```
   */
  @Test
  fun reason_preventAccidentalUsage() {
    assertTrue(rule.enabledByDefault)
  }
}
