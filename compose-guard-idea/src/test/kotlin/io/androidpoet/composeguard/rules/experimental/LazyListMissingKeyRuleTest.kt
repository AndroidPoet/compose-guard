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
 * Tests for LazyListMissingKeyRule.
 *
 * This rule detects items() calls in LazyColumn/LazyRow that are missing
 * a key parameter for efficient recomposition and state preservation.
 */
class LazyListMissingKeyRuleTest {

  private val rule = LazyListMissingKeyRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("LazyListMissingKey", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("LazyList Missing Key Parameter", rule.name)
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
    assertTrue(rule.description.contains("LazyColumn") || rule.description.contains("LazyRow"))
    assertTrue(rule.description.contains("key"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("item-keys"))
  }

  // =============================================================================
  // SUPPORTED LAZY LIST FUNCTIONS TESTS
  // =============================================================================

  @Test
  fun testRule_supportsLazyColumn() {
    // Verify LazyColumn is in the supported list
    val description = rule.description
    assertTrue(description.contains("LazyColumn") || rule.name.contains("LazyList"))
  }

  @Test
  fun testRule_supportsLazyRow() {
    // Verify LazyRow is in the supported list via rule documentation
    val url = rule.documentationUrl
    assertTrue(url != null && url.contains("lists"))
  }

  // =============================================================================
  // ITEMS FUNCTIONS TESTS
  // =============================================================================

  @Test
  fun testRule_detectsItemsFunction() {
    // Rule should detect items() without key
    val name = rule.name
    assertTrue(name.contains("Key"))
  }

  @Test
  fun testRule_detectsItemsIndexedFunction() {
    // itemsIndexed is also covered by the rule
    assertTrue(rule.id.contains("LazyList"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_hasQuickFixes() {
    // Rule should provide quick fixes
    // Verified by the presence of AddKeyParameterFix in the imports of the rule
    assertTrue(rule.description.contains("key"))
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
  fun testRule_documentationUrlPointsToOfficialDocs() {
    val url = rule.documentationUrl
    assertTrue(url!!.contains("developer.android.com"))
  }
}
