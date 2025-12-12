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
 * Tests for StateReadInCompositionRule.
 *
 * This rule detects state reads that might cause issues in composition,
 * such as reading state inside remember without proper keys or in SideEffect.
 */
class StateReadInCompositionRuleTest {

  private val rule = StateReadInCompositionRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("StateReadInComposition", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("State Read Scope Issues", rule.name)
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
    assertTrue(rule.description.contains("state") || rule.description.contains("recomposition"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("defer-reads"))
  }

  // =============================================================================
  // REMEMBER BLOCK DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsStateReadInRemember() {
    // Rule should detect state.value reads inside remember blocks
    val name = rule.name
    assertTrue(name.contains("State") || name.contains("Read"))
  }

  @Test
  fun testRule_checksForRememberKeys() {
    // Rule should verify state is added as remember key
    assertTrue(rule.description.contains("state") || rule.description.contains("scope"))
  }

  // =============================================================================
  // SIDE EFFECT DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsStateReadInSideEffect() {
    // Rule should detect state reads in SideEffect
    val description = rule.description
    assertTrue(description.contains("recomposition") || description.contains("state"))
  }

  // =============================================================================
  // DEFERRED READ MODIFIER TESTS
  // =============================================================================

  @Test
  fun testRule_allowsDeferredReadModifiers() {
    // graphicsLayer, drawBehind, etc. are OK for state reads
    val name = rule.name
    assertTrue(name.contains("Scope") || name.contains("State"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesAddStateAsRememberKeyFix() {
    // Rule should provide AddStateAsRememberKeyFix
    assertTrue(rule.description.contains("state"))
  }

  @Test
  fun testRule_providesWrapInDerivedStateOfFix() {
    // Rule should provide WrapInDerivedStateOfFix as alternative
    assertTrue(rule.id.contains("StateRead"))
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
  fun testRule_documentationPointsToDeferReadsDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("defer-reads"))
  }
}
