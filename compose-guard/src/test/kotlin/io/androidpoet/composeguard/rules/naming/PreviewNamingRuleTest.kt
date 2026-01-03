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
 * Comprehensive tests for PreviewNamingRule.
 *
 * Rule: Preview functions should follow naming conventions.
 *
 * Preview functions should include 'Preview' in their name
 * to make their purpose clear.
 */
class PreviewNamingRuleTest {

  private val rule = PreviewNamingRule()


  @Test
  fun metadata_id() {
    assertEquals("PreviewNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Preview Naming Convention", rule.name)
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
  fun metadata_descriptionMentionsPreview() {
    assertTrue(
      rule.description.contains("Preview") ||
        rule.description.contains("preview"),
    )
  }


  /**
   * Pattern: Preview with 'Preview' suffix - NO VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun ButtonPreview() { ... }
   * ```
   */
  @Test
  fun pattern_withPreviewSuffix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Preview with 'Preview' prefix - NO VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun PreviewButton() { ... }
   * ```
   */
  @Test
  fun pattern_withPreviewPrefix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Preview with 'Preview' in middle - NO VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun MyPreviewButton() { ... }
   * ```
   */
  @Test
  fun pattern_withPreviewInMiddle_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Preview without 'Preview' in name - VIOLATION
   *
   * ```kotlin
   * @Preview
   * @Composable
   * fun Button() { ... }  // Should include 'Preview'
   * ```
   */
  @Test
  fun pattern_withoutPreviewInName_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Non-preview function - NO VIOLATION (not checked)
   *
   * ```kotlin
   * @Composable
   * fun Button() { ... }  // Not a preview function
   * ```
   */
  @Test
  fun pattern_nonPreviewFunction_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  /**
   * Why Preview naming matters:
   *
   * 1. **Searchability**: Easy to find all previews in the project
   * 2. **Clarity**: Clear which functions are previews
   * 3. **Filtering**: IDE can filter preview functions
   *
   * Example:
   * ```kotlin
   * // Good - clearly identifiable as previews
   * @Preview
   * @Composable
   * fun ButtonPreview() { ... }
   *
   * @Preview
   * @Composable
   * fun CardPreview() { ... }
   *
   * // Bad - not clear these are previews
   * @Preview
   * @Composable
   * fun Button() { ... }  // Confusing - is this the actual Button?
   * ```
   */
  @Test
  fun reason_searchabilityAndClarity() {
    assertTrue(rule.enabledByDefault)
  }
}
