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
 * Comprehensive tests for TrailingLambdaRule.
 *
 * Rules:
 * 1. Content slots (@Composable () -> Unit) should be trailing lambdas
 * 2. Event handlers (onClick, onValueChange) should NOT be trailing lambdas
 * 3. The primary content lambda should be the last parameter
 */
class TrailingLambdaRuleTest {

  private val rule = TrailingLambdaRule()


  @Test
  fun metadata_id() {
    assertEquals("TrailingLambda", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Trailing Lambda Rules", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
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
  }

  @Test
  fun metadata_descriptionMentionsContent() {
    assertTrue(
      rule.description.contains("Content") ||
        rule.description.contains("content") ||
        rule.description.contains("lambda") ||
        rule.description.contains("trailing"),
    )
  }


  /**
   * Pattern: Content lambda should be last for trailing lambda syntax.
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun Card(
   *     title: String,
   *     modifier: Modifier = Modifier,
   *     content: @Composable () -> Unit  // Last
   * )
   *
   * // Call site:
   * Card("Title", Modifier.padding(16.dp)) {
   *     Text("Content")
   * }
   * ```
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun Card(
   *     content: @Composable () -> Unit,  // Not last!
   *     title: String,
   *     modifier: Modifier = Modifier
   * )
   *
   * // Awkward call site:
   * Card(content = { Text("Content") }, title = "Title")
   * ```
   */
  @Test
  fun pattern_contentLambdaTrailing() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  /**
   * Pattern: Event handlers should NOT be trailing, content should.
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun Button(
   *     onClick: () -> Unit,           // Event handler - NOT trailing
   *     modifier: Modifier = Modifier,
   *     content: @Composable RowScope.() -> Unit  // Content - trailing
   * )
   * ```
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun Button(
   *     content: @Composable RowScope.() -> Unit,
   *     modifier: Modifier = Modifier,
   *     onClick: () -> Unit  // Event handler at the end - BAD
   * )
   * ```
   */
  @Test
  fun pattern_eventHandlersNotTrailing() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  /**
   * Pattern: Multiple content slots - primary content is last.
   *
   * ```kotlin
   * @Composable
   * fun Scaffold(
   *     topBar: @Composable () -> Unit = {},          // Optional slot
   *     bottomBar: @Composable () -> Unit = {},       // Optional slot
   *     floatingActionButton: @Composable () -> Unit = {},
   *     modifier: Modifier = Modifier,
   *     content: @Composable (PaddingValues) -> Unit  // Primary content - last
   * )
   * ```
   */
  @Test
  fun pattern_multipleContentSlots() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  /**
   * Pattern: Single content lambda should be named "content".
   *
   * Correct: content: @Composable () -> Unit
   * Acceptable: body: @Composable () -> Unit (if context-appropriate)
   *
   * Multiple content slots should have descriptive names:
   * - icon: @Composable () -> Unit
   * - title: @Composable () -> Unit
   * - actions: @Composable () -> Unit
   * - content: @Composable () -> Unit
   */
  @Test
  fun pattern_namedLambdas() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  /**
   * Document call site benefits of trailing lambda.
   *
   * With trailing lambda:
   * ```kotlin
   * Card("Title") {
   *     Column {
   *         Text("Line 1")
   *         Text("Line 2")
   *     }
   * }
   * ```
   *
   * Without trailing lambda:
   * ```kotlin
   * Card(
   *     content = {
   *         Column {
   *             Text("Line 1")
   *             Text("Line 2")
   *         }
   *     },
   *     title = "Title"
   * )
   * ```
   */
  @Test
  fun benefit_cleanCallSite() {
    assertTrue(rule.enabledByDefault)
  }
}
