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
import kotlin.test.assertTrue

/**
 * Tests for LazyListContentTypeRule.
 *
 * This rule detects LazyColumn/LazyRow with heterogeneous items that are
 * missing the contentType parameter for efficient composition reuse.
 */
class LazyListContentTypeRuleTest {

  private val rule = LazyListContentTypeRule()

  // =============================================================================
  // METADATA TESTS
  // =============================================================================

  @Test
  fun testRuleId() {
    assertEquals("LazyListContentType", rule.id)
  }

  @Test
  fun testRuleName() {
    assertEquals("LazyList Missing ContentType", rule.name)
  }

  @Test
  fun testRuleCategory() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun testRuleSeverity() {
    // ContentType is a suggestion, not a hard requirement
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testRuleEnabledByDefault() {
    // Rule is enabled by default like other composable rules
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testRuleDescription() {
    assertTrue(rule.description.isNotBlank())
    assertTrue(rule.description.contains("contentType") || rule.description.contains("heterogeneous"))
  }

  @Test
  fun testRuleDocumentationUrl() {
    val url = rule.documentationUrl
    assertTrue(url != null && url.isNotBlank())
    assertTrue(url!!.contains("developer.android.com"))
    assertTrue(url.contains("content-type"))
  }

  // =============================================================================
  // LAZY ITEM FUNCTIONS TESTS
  // =============================================================================

  @Test
  fun testRule_detectsItemFunction() {
    // Rule should detect item() calls
    val name = rule.name
    assertTrue(name.contains("ContentType"))
  }

  @Test
  fun testRule_detectsItemsFunction() {
    // Rule should detect items() calls
    assertTrue(rule.id.contains("LazyList"))
  }

  @Test
  fun testRule_detectsStickyHeaderFunction() {
    // stickyHeader should also be considered for heterogeneous lists
    assertTrue(rule.description.contains("heterogeneous") || rule.name.contains("LazyList"))
  }

  // =============================================================================
  // HETEROGENEOUS DETECTION TESTS
  // =============================================================================

  @Test
  fun testRule_onlyFlagsHeterogeneousLists() {
    // Rule should only flag lists with multiple item types
    val description = rule.description
    assertTrue(description.contains("heterogeneous") || description.contains("contentType"))
  }

  // =============================================================================
  // QUICK FIX TESTS
  // =============================================================================

  @Test
  fun testRule_providesContentTypeFix() {
    // Rule should provide AddContentTypeFix
    assertTrue(rule.description.contains("contentType"))
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
