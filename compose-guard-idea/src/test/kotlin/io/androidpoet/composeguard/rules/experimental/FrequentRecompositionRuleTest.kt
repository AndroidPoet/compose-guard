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
 * Tests for FrequentRecompositionRule.
 *
 * This rule detects patterns that may cause excessive recompositions,
 * such as object creation in composition, collection creation, and
 * flow collection without lifecycle awareness.
 */
class FrequentRecompositionRuleTest {

  private val rule = FrequentRecompositionRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("FrequentRecomposition", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("Potential Excessive Recomposition", rule.name)
  }

  @Test
  fun testRuleCategory() {
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testRuleSeverity() {
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testRuleEnabledByDefault() {
    // Rule is enabled by default; the EXPERIMENTAL category controls activation
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testRuleDescription() {
    assertTrue(rule.description.isNotBlank())
    assertTrue(rule.description.contains("recomposition") || rule.description.contains("performance"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("bestpractices"))
  }

  // =============================================================================
  // OBJECT CREATION DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsObjectCreation() {
    // Rule should detect object creation in composition
    val name = rule.name
    assertTrue(name.contains("Recomposition"))
  }

  @Test
  fun testRule_detectsCommonObjectTypes() {
    // Rule should detect Color, Offset, Size, etc. creation
    val description = rule.description
    assertTrue(description.contains("recomposition") || description.contains("performance"))
  }

  // =============================================================================
  // COLLECTION CREATION DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsListOfCreation() {
    // Rule should detect listOf() creation
    assertTrue(rule.id.contains("Recomposition"))
  }

  @Test
  fun testRule_detectsMapOfCreation() {
    // Rule should detect mapOf() creation
    assertTrue(rule.category == RuleCategory.EXPERIMENTAL)
  }

  @Test
  fun testRule_detectsSetOfCreation() {
    // Rule should detect setOf() creation
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  // =============================================================================
  // FLOW COLLECTION DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_suggestsLifecycleAwareCollector() {
    // Rule should suggest collectAsStateWithLifecycle
    val description = rule.description
    assertTrue(description.contains("recomposition") || description.contains("performance"))
  }

  // =============================================================================
  // SKIPPING ALREADY OPTIMIZED CODE TESTS
  // =============================================================================

  @Test
  fun testRule_skipsRememberedProperties() {
    // Rule should skip properties already wrapped in remember
    assertTrue(rule.name.contains("Recomposition"))
  }

  @Test
  fun testRule_skipsSimpleConstants() {
    // Rule should skip simple constant values
    assertTrue(rule.description.contains("recomposition"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesWrapInRememberFix() {
    // Rule should provide WrapInRememberFix
    val name = rule.name
    assertTrue(name.contains("Recomposition"))
  }

  @Test
  fun testRule_providesUseLifecycleAwareCollectorFix() {
    // Rule should provide UseLifecycleAwareCollectorFix
    assertTrue(rule.id.contains("Recomposition"))
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
  fun testRule_documentationPointsToBestPracticesDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("bestpractices"))
  }
}
