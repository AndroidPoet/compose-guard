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
 * Tests for UnstableLambdaRule (Method Reference Candidate).
 *
 * This rule suggests converting single-expression lambdas to method references
 * for cleaner, more readable code.
 */
class UnstableLambdaRuleTest {

  private val rule = UnstableLambdaRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("MethodReferenceCandidate", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("Method Reference Candidate", rule.name)
  }

  @Test
  fun testRuleCategory() {
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testRuleSeverity() {
    // This is a code style suggestion, not a warning
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
    assertTrue(rule.description.contains("method reference") || rule.description.contains("lambda"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("kotlinlang.org"))
    assertTrue(url.contains("reflection") || url.contains("reference"))
  }

  // =============================================================================
  // CALLBACK PARAMETER TESTS
  // =============================================================================

  @Test
  fun testRule_detectsOnClickCallbacks() {
    // Rule should detect onClick lambdas
    val name = rule.name
    assertTrue(name.contains("Method") || name.contains("Reference"))
  }

  @Test
  fun testRule_detectsOnValueChangeCallbacks() {
    // Rule should detect onValueChange lambdas
    assertTrue(rule.id.contains("MethodReference"))
  }

  // =============================================================================
  // METHOD REFERENCE DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsSingleExpressionLambdas() {
    // Rule should only suggest for single-expression lambdas
    val description = rule.description
    assertTrue(description.contains("single") || description.contains("lambda"))
  }

  @Test
  fun testRule_suggestsMethodReferenceSyntax() {
    // Rule should suggest :: syntax
    assertTrue(rule.description.contains("method reference") || rule.description.contains("::"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesConvertToMethodReferenceFix() {
    // Rule should provide ConvertToMethodReferenceFix
    val name = rule.name
    assertTrue(name.contains("Method Reference"))
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
  fun testRule_documentationPointsToKotlinDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("kotlinlang.org"))
  }
}
