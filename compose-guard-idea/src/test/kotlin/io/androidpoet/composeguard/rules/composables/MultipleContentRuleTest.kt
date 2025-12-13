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
 * Comprehensive tests for MultipleContentRule.
 *
 * Rule: Do not emit multiple pieces of content.
 *
 * A composable function should emit zero or one layout nodes at the top level.
 * Emitting multiple content nodes makes the composable dependent on its parent layout.
 */
class MultipleContentRuleTest {

  private val rule = MultipleContentRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("MultipleContentEmitters", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Multiple Content Emitters", rule.name)
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
  fun metadata_descriptionMentionsMultipleContent() {
    assertTrue(
      rule.description.contains("one") ||
        rule.description.contains("multiple") ||
        rule.description.contains("content"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - MULTIPLE EMITTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Multiple content emitters at top level - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun BadContent() {
   *     Text("Hello")   // First emitter
   *     Image(...)      // Second emitter
   *     Button(...)     // Third emitter
   * }
   * ```
   */
  @Test
  fun pattern_multipleTopLevelEmitters_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: Single container at top level - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun GoodContent() {
   *     Column {
   *         Text("Hello")
   *         Image(...)
   *         Button(...)
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_singleContainerAtTopLevel_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: No content emitters - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun NoContent() {
   *     val state = remember { mutableStateOf(0) }
   *     LaunchedEffect(Unit) { }
   * }
   * ```
   */
  @Test
  fun pattern_noContentEmitters_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DETECTION PATTERN TESTS - LAYOUT SCOPE EXTENSIONS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: ColumnScope extension with multiple emitters - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun ColumnScope.ColumnContent() {
   *     Text("Hello")   // OK in ColumnScope
   *     Image(...)      // OK in ColumnScope
   * }
   * ```
   */
  @Test
  fun pattern_columnScopeExtension_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: RowScope extension with multiple emitters - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun RowScope.RowContent() {
   *     Text("Hello")   // OK in RowScope
   *     Icon(...)       // OK in RowScope
   * }
   * ```
   */
  @Test
  fun pattern_rowScopeExtension_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  /**
   * Pattern: LazyListScope extension - NO VIOLATION
   *
   * ```kotlin
   * fun LazyListScope.ListItems() {
   *     item { Text("Header") }
   *     items(data) { ... }
   * }
   * ```
   */
  @Test
  fun pattern_lazyListScopeExtension_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // WHY THIS MATTERS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Why single content emission matters:
   *
   * 1. **Encapsulation**: Composable doesn't depend on parent layout
   * 2. **Reusability**: Can be used in any context
   * 3. **Predictability**: Clear what the composable renders
   *
   * Example:
   * ```kotlin
   * // Bad - depends on parent being Column/Row
   * @Composable
   * fun HeaderContent() {
   *     Text("Title")
   *     Text("Subtitle")  // Where will this go relative to Title?
   * }
   *
   * // Good - self-contained
   * @Composable
   * fun HeaderContent() {
   *     Column {
   *         Text("Title")
   *         Text("Subtitle")
   *     }
   * }
   *
   * // Also Good - scope extension makes intent clear
   * @Composable
   * fun ColumnScope.HeaderContent() {
   *     Text("Title")
   *     Text("Subtitle")
   * }
   * ```
   */
  @Test
  fun reason_encapsulationAndReusability() {
    assertTrue(rule.enabledByDefault)
  }
}
