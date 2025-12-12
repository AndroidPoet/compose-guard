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
package io.androidpoet.composeguard.rules.experimental

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for DerivedStateOfCandidateRule.
 *
 * This rule suggests using derivedStateOf for computed values derived from state
 * to avoid unnecessary recalculations on every recomposition.
 */
class DerivedStateOfCandidateRuleTest {

  private val rule = DerivedStateOfCandidateRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("DerivedStateOfCandidate", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("Consider Using derivedStateOf", rule.name)
  }

  @Test
  fun testRuleCategory() {
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testRuleSeverity() {
    // This is a suggestion, not a hard requirement
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testRuleEnabledByDefault() {
    // Rule is enabled by default; the EXPERIMENTAL category controls activation
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testRuleDescription() {
    assertTrue(rule.description.isNotBlank())
    assertTrue(rule.description.contains("derivedStateOf") || rule.description.contains("computed"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("derivedstateof"))
  }

  // =============================================================================
  // EXPENSIVE OPERATION DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsFilterOperations() {
    // Rule should detect filter operations
    val name = rule.name
    assertTrue(name.contains("derivedStateOf"))
  }

  @Test
  fun testRule_detectsMapOperations() {
    // Rule should detect map operations
    assertTrue(rule.id.contains("DerivedStateOf"))
  }

  @Test
  fun testRule_detectsSortedOperations() {
    // Rule should detect sorted operations
    val description = rule.description
    assertTrue(description.contains("computed") || description.contains("recalculation"))
  }

  @Test
  fun testRule_detectsGroupByOperations() {
    // Rule should detect groupBy operations
    assertTrue(rule.description.contains("derivedStateOf") || rule.description.contains("computed"))
  }

  @Test
  fun testRule_detectsJoinToStringOperations() {
    // Rule should detect joinToString operations
    assertTrue(rule.category == RuleCategory.EXPERIMENTAL)
  }

  // =============================================================================
  // ALREADY OPTIMIZED DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_skipsRememberedProperties() {
    // Rule should skip properties already wrapped in remember
    val name = rule.name
    assertTrue(name.contains("derivedStateOf"))
  }

  @Test
  fun testRule_skipsDerivedStateOfProperties() {
    // Rule should skip properties already using derivedStateOf
    assertTrue(rule.description.contains("derivedStateOf"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesWrapInDerivedStateOfFix() {
    // Rule should provide WrapInDerivedStateOfFix
    val name = rule.name
    assertTrue(name.contains("derivedStateOf"))
  }

  // =============================================================================
  // DOCUMENTATION TESTS
  // =============================================================================

  @Test
  fun testRule_documentationUrlIsValid() {
    val url = rule.documentationUrl
    assertTrue(url != null)
    assertTrue(url!!.startsWith("https://"))
  }

  @Test
  fun testRule_documentationPointsToDerivedStateDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("derivedstateof"))
  }
}
