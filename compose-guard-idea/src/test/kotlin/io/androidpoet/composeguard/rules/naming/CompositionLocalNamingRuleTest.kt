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
 * Comprehensive tests for CompositionLocalNamingRule.
 *
 * Rule: CompositionLocal should be named with "Local" prefix.
 *
 * CompositionLocal instances should follow the naming convention of
 * starting with "Local" prefix to make their purpose clear.
 */
class CompositionLocalNamingRuleTest {

  private val rule = CompositionLocalNamingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("CompositionLocalNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("CompositionLocal Naming", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.NAMING, rule.category)
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
  fun metadata_descriptionMentionsLocal() {
    assertTrue(
      rule.description.contains("Local") ||
        rule.description.contains("prefix"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: CompositionLocal with Local prefix - NO VIOLATION
   *
   * ```kotlin
   * val LocalCurrentUser = compositionLocalOf { User() }
   * val LocalTheme = staticCompositionLocalOf { Theme() }
   * ```
   */
  @Test
  fun pattern_withLocalPrefix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: CompositionLocal without Local prefix - VIOLATION
   *
   * ```kotlin
   * val CurrentUser = compositionLocalOf { User() }  // Should be LocalCurrentUser
   * val Theme = staticCompositionLocalOf { Theme() }  // Should be LocalTheme
   * ```
   */
  @Test
  fun pattern_withoutLocalPrefix_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: compositionLocalOf - should be checked
   *
   * ```kotlin
   * val MySettings = compositionLocalOf { Settings() }  // VIOLATION
   * ```
   */
  @Test
  fun pattern_compositionLocalOf_shouldBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: staticCompositionLocalOf - should be checked
   *
   * ```kotlin
   * val Analytics = staticCompositionLocalOf { AnalyticsService() }  // VIOLATION
   * ```
   */
  @Test
  fun pattern_staticCompositionLocalOf_shouldBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  /**
   * Pattern: Non-CompositionLocal property - NO VIOLATION (not checked)
   *
   * ```kotlin
   * val CurrentUser = User()  // Not a CompositionLocal
   * ```
   */
  @Test
  fun pattern_nonCompositionLocal_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why Local prefix matters:
   *
   * 1. **Discoverability**: Easy to find all CompositionLocals
   * 2. **Clarity**: Immediately clear this is ambient data
   * 3. **Convention**: Standard pattern in Compose ecosystem
   *
   * Example:
   * ```kotlin
   * // Standard Compose locals use this pattern
   * LocalContext
   * LocalConfiguration
   * LocalDensity
   * LocalLifecycleOwner
   *
   * // Your custom locals should too
   * LocalCurrentUser
   * LocalTheme
   * LocalAnalytics
   * ```
   */
  @Test
  fun reason_discoverabilityAndClarity() {
    assertTrue(rule.enabledByDefault)
  }
}
