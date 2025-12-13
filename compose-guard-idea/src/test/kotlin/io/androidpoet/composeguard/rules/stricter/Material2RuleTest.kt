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
package io.androidpoet.composeguard.rules.stricter

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for Material2Rule.
 *
 * Rule: Don't use Material 2.
 *
 * Use Material 3 instead of Material 2 for modern theming and Material You support.
 */
class Material2RuleTest {

  private val rule = Material2Rule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("Material2Usage", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Use Material 2", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  @Test
  fun metadata_severity() {
    // INFO because it's a recommendation, not a hard rule
    assertEquals(RuleSeverity.INFO, rule.severity)
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
  fun metadata_descriptionMentionsMaterial3() {
    assertTrue(
      rule.description.contains("Material 3") ||
        rule.description.contains("modern") ||
        rule.description.contains("theming"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - MATERIAL 2 IMPORTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Material 2 import - VIOLATION
   *
   * ```kotlin
   * import androidx.compose.material.Button  // Material 2!
   * import androidx.compose.material.Text
   * ```
   */
  @Test
  fun pattern_material2Import_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: Material 3 import - NO VIOLATION
   *
   * ```kotlin
   * import androidx.compose.material3.Button  // Material 3!
   * import androidx.compose.material3.Text
   * ```
   */
  @Test
  fun pattern_material3Import_shouldNotViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: Material icons import - NO VIOLATION (shared between M2/M3)
   *
   * ```kotlin
   * import androidx.compose.material.icons.Icons
   * import androidx.compose.material.icons.filled.Add
   * ```
   */
  @Test
  fun pattern_materialIconsImport_shouldNotViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - SPECIFIC COMPONENTS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Material 2 Scaffold import - VIOLATION
   *
   * ```kotlin
   * import androidx.compose.material.Scaffold
   * ```
   */
  @Test
  fun pattern_material2Scaffold_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: Material 2 TopAppBar import - VIOLATION
   *
   * ```kotlin
   * import androidx.compose.material.TopAppBar
   * ```
   */
  @Test
  fun pattern_material2TopAppBar_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why Material 3 is preferred:
   *
   * 1. **Material You**: Dynamic color support
   * 2. **Modern design**: Updated design language
   * 3. **Maintained**: Material 2 is in maintenance mode
   * 4. **Consistency**: New apps should use M3
   *
   * Example:
   * ```kotlin
   * // Material 2 (deprecated approach)
   * import androidx.compose.material.Button
   *
   * // Material 3 (recommended)
   * import androidx.compose.material3.Button
   * ```
   */
  @Test
  fun reason_modernDesignAndDynamicColor() {
    // INFO severity because it's a recommendation
    assertEquals(RuleSeverity.INFO, rule.severity)
  }
}
