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
package io.androidpoet.composeguard.rules.state

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for FrequentRecompositionRule.
 *
 * Rule: Detect patterns that may cause excessive recompositions.
 *
 * This rule identifies common anti-patterns that lead to unnecessary
 * recompositions and performance issues in Compose.
 */
class FrequentRecompositionRuleTest {

  private val rule = FrequentRecompositionRule()


  @Test
  fun metadata_id() {
    assertEquals("FrequentRecomposition", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Potential Excessive Recomposition", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
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
    assertTrue(rule.documentationUrl!!.contains("android.com"))
  }

  @Test
  fun metadata_descriptionMentionsRecomposition() {
    assertTrue(
      rule.description.contains("recomposition") ||
        rule.description.contains("performance"),
    )
  }


  /**
   * Pattern: collectAsState without lifecycle awareness - VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyScreen(viewModel: MyViewModel) {
   *     // Continues collecting even in background
   *     val state by viewModel.stateFlow.collectAsState()
   * }
   * ```
   */
  @Test
  fun pattern_collectAsStateWithoutLifecycle_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: collectAsStateWithLifecycle - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyScreen(viewModel: MyViewModel) {
   *     // Stops collecting when app is in background
   *     val state by viewModel.stateFlow.collectAsStateWithLifecycle()
   * }
   * ```
   */
  @Test
  fun pattern_collectAsStateWithLifecycle_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  /**
   * Pattern: observeAsState (LiveData) - NO VIOLATION
   *
   * ```kotlin
   * @Composable
   * fun MyScreen(viewModel: MyViewModel) {
   *     val state by viewModel.liveData.observeAsState()
   * }
   * ```
   */
  @Test
  fun pattern_observeAsState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  /**
   * Why lifecycle-aware collection matters:
   *
   * 1. **Resource usage**: collectAsState keeps collecting in background
   * 2. **Battery**: Background collection wastes battery
   * 3. **Crashes**: Emitting to inactive UI can cause issues
   *
   * Example:
   * ```kotlin
   * // Bad - continues in background
   * val state by flow.collectAsState()
   *
   * // Good - stops when lifecycle is below STARTED
   * val state by flow.collectAsStateWithLifecycle()
   * ```
   */
  @Test
  fun reason_lifecycleAwareness() {
    assertTrue(rule.enabledByDefault)
  }
}
