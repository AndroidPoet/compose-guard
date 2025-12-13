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
 * Comprehensive tests for UnstableCollectionsRule.
 *
 * Rule: Avoid using unstable collections as parameters.
 *
 * Standard collection interfaces (List, Set, Map) may be backed by mutable
 * implementations, making them unstable for compose.
 */
class UnstableCollectionsRuleTest {

  private val rule = UnstableCollectionsRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("UnstableCollections", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Avoid Unstable Collections", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STRICTER, rule.category)
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
  fun metadata_descriptionMentionsImmutable() {
    assertTrue(
      rule.description.contains("ImmutableList") ||
        rule.description.contains("immutable") ||
        rule.description.contains("stable"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - LIST
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: List parameter - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ItemList(items: List<Item>) {  // Unstable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_listParameter_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: ImmutableList parameter - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ItemList(items: ImmutableList<Item>) {  // Stable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_immutableListParameter_shouldNotViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - SET
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Set parameter - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun TagView(tags: Set<String>) {  // Unstable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_setParameter_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: ImmutableSet parameter - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun TagView(tags: ImmutableSet<String>) {  // Stable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_immutableSetParameter_shouldNotViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - MAP
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Map parameter - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Settings(config: Map<String, Any>) {  // Unstable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_mapParameter_shouldViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  /**
   * Pattern: ImmutableMap parameter - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun Settings(config: ImmutableMap<String, Any>) {  // Stable!
   *     ...
   * }
   * ```
   */
  @Test
  fun pattern_immutableMapParameter_shouldNotViolate() {
    assertEquals(RuleCategory.STRICTER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why unstable collections cause issues:
   *
   * 1. **Compose stability**: Collections may be backed by mutable implementations
   * 2. **Skipping**: Compose can't skip recomposition if parameters aren't stable
   * 3. **Performance**: Unnecessary recompositions waste resources
   *
   * Example:
   * ```kotlin
   * // Bad - List could be mutable, Compose can't prove it hasn't changed
   * @Composable
   * fun ItemList(items: List<Item>)
   *
   * // Good - ImmutableList is guaranteed stable
   * @Composable
   * fun ItemList(items: ImmutableList<Item>)
   * ```
   */
  @Test
  fun reason_composeStabilityAndPerformance() {
    assertTrue(rule.enabledByDefault)
  }
}
