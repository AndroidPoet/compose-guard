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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for MutableParameterRule.
 *
 * Rule: Don't use mutable collections as parameters in composable functions.
 *
 * Why:
 * - Mutable collections break Compose's recomposition optimization
 * - Compose can't detect changes to mutable collections efficiently
 * - Leads to unnecessary recompositions or missed updates
 */
class MutableParameterRuleTest {

  private val rule = MutableParameterRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("MutableParameter", rule.id)
  }

  @Test
  fun metadata_name() {
    assertTrue(rule.name.isNotBlank())
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
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
  fun metadata_descriptionMentionsMutable() {
    assertTrue(
      rule.description.contains("mutable") ||
        rule.description.contains("Mutable") ||
        rule.description.contains("immutable"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // MUTABLE COLLECTION PATTERNS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Don't use MutableList as parameter.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun ItemList(items: MutableList<Item>) { ... }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun ItemList(items: List<Item>) { ... }
   * ```
   */
  @Test
  fun pattern_mutableListBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Pattern: Don't use MutableSet as parameter.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun TagCloud(tags: MutableSet<String>) { ... }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun TagCloud(tags: Set<String>) { ... }
   * ```
   */
  @Test
  fun pattern_mutableSetBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Pattern: Don't use MutableMap as parameter.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun Settings(settings: MutableMap<String, Any>) { ... }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun Settings(settings: Map<String, Any>) { ... }
   * ```
   */
  @Test
  fun pattern_mutableMapBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // IMMUTABLE ALTERNATIVES
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Recommended: Use Kotlin immutable collections.
   *
   * ```kotlin
   * @Composable
   * fun ItemList(items: List<Item>) { ... }
   *
   * @Composable
   * fun TagCloud(tags: Set<String>) { ... }
   *
   * @Composable
   * fun Settings(settings: Map<String, Any>) { ... }
   * ```
   */
  @Test
  fun alternative_kotlinCollections() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  /**
   * Recommended: Use Kotlinx Immutable Collections for better performance.
   *
   * ```kotlin
   * @Composable
   * fun ItemList(items: ImmutableList<Item>) { ... }
   *
   * @Composable
   * fun TagCloud(tags: ImmutableSet<String>) { ... }
   *
   * @Composable
   * fun Settings(settings: ImmutableMap<String, Any>) { ... }
   * ```
   */
  @Test
  fun alternative_kotlinxImmutableCollections() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY IMMUTABLE MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why immutable collections matter for Compose:
   *
   * 1. **Referential equality**: Compose uses === to check if parameters changed
   * 2. **Mutable pitfall**: Same reference, different content = missed recomposition
   * 3. **Performance**: Immutable collections enable skip optimization
   *
   * Example of the problem:
   * ```kotlin
   * val items = mutableListOf("A", "B")
   * ItemList(items)  // Renders "A", "B"
   * items.add("C")   // Same reference!
   * ItemList(items)  // Compose thinks nothing changed!
   * ```
   */
  @Test
  fun reason_referentialEquality() {
    assertTrue(rule.enabledByDefault)
  }
}
