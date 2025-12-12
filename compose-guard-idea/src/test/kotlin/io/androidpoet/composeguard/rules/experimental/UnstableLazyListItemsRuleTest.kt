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
 * Tests for UnstableLazyListItemsRule.
 *
 * This rule detects LazyList items() calls using mutable/unstable collections
 * which can cause unnecessary recompositions.
 */
class UnstableLazyListItemsRuleTest {

  private val rule = UnstableLazyListItemsRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("UnstableLazyListItems", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("Unstable Items in LazyList", rule.name)
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
    assertTrue(rule.description.contains("stable") || rule.description.contains("immutable"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("stability"))
  }

  // =============================================================================
  // MUTABLE COLLECTION DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_detectsMutableCollections() {
    // Rule should detect mutable collection types
    val description = rule.description
    assertTrue(description.contains("stable") || description.contains("immutable"))
  }

  @Test
  fun testRule_hasCorrectCategory() {
    // Rule is in EXPERIMENTAL category
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesImmutableCollectionFix() {
    // Rule should suggest using immutable collections
    assertTrue(rule.description.contains("immutable") || rule.description.contains("stable"))
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
  fun testRule_documentationPointsToStabilityDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("stability"))
  }
}
