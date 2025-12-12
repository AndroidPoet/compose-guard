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

import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for all 7 ComposeGuard experimental rules.
 * Tests rule metadata, categories, severities, and documentation URLs.
 */
class ExperimentalRulesMetadataTest {

  // =============================================================================
  // LAZY LIST MISSING KEY RULE
  // =============================================================================

  @Test
  fun testLazyListMissingKeyRule_metadata() {
    val rule = LazyListMissingKeyRule()

    assertEquals("LazyListMissingKey", rule.id)
    assertEquals("LazyList Missing Key Parameter", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testLazyListMissingKeyRule_enabledByDefault() {
    val rule = LazyListMissingKeyRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // LAZY LIST CONTENT TYPE RULE
  // =============================================================================

  @Test
  fun testLazyListContentTypeRule_metadata() {
    val rule = LazyListContentTypeRule()

    assertEquals("LazyListContentType", rule.id)
    assertEquals("LazyList Missing ContentType", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testLazyListContentTypeRule_enabledByDefault() {
    val rule = LazyListContentTypeRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // UNSTABLE LAZY LIST ITEMS RULE
  // =============================================================================

  @Test
  fun testUnstableLazyListItemsRule_metadata() {
    val rule = UnstableLazyListItemsRule()

    assertEquals("UnstableLazyListItems", rule.id)
    assertEquals("Unstable Items in LazyList", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testUnstableLazyListItemsRule_enabledByDefault() {
    val rule = UnstableLazyListItemsRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // UNSTABLE LAMBDA RULE (METHOD REFERENCE CANDIDATE)
  // =============================================================================

  @Test
  fun testUnstableLambdaRule_metadata() {
    val rule = UnstableLambdaRule()

    assertEquals("MethodReferenceCandidate", rule.id)
    assertEquals("Method Reference Candidate", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("kotlinlang.org"))
  }

  @Test
  fun testUnstableLambdaRule_enabledByDefault() {
    val rule = UnstableLambdaRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // DERIVED STATE OF CANDIDATE RULE
  // =============================================================================

  @Test
  fun testDerivedStateOfCandidateRule_metadata() {
    val rule = DerivedStateOfCandidateRule()

    assertEquals("DerivedStateOfCandidate", rule.id)
    assertEquals("Consider Using derivedStateOf", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testDerivedStateOfCandidateRule_enabledByDefault() {
    val rule = DerivedStateOfCandidateRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // STATE READ IN COMPOSITION RULE
  // =============================================================================

  @Test
  fun testStateReadInCompositionRule_metadata() {
    val rule = StateReadInCompositionRule()

    assertEquals("StateReadInComposition", rule.id)
    assertEquals("State Read Scope Issues", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testStateReadInCompositionRule_enabledByDefault() {
    val rule = StateReadInCompositionRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // FREQUENT RECOMPOSITION RULE
  // =============================================================================

  @Test
  fun testFrequentRecompositionRule_metadata() {
    val rule = FrequentRecompositionRule()

    assertEquals("FrequentRecomposition", rule.id)
    assertEquals("Potential Excessive Recomposition", rule.name)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.contains("developer.android.com"))
  }

  @Test
  fun testFrequentRecompositionRule_enabledByDefault() {
    val rule = FrequentRecompositionRule()
    assertTrue(rule.enabledByDefault, "Rule enabled by default; category controls activation")
  }

  // =============================================================================
  // CATEGORY AND REGISTRY TESTS
  // =============================================================================

  @Test
  fun testAllExperimentalRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    assertEquals(7, rules.size, "Expected 7 experimental rules")
  }

  @Test
  fun testAllExperimentalRules_areEnabledByDefault() {
    // Individual rules are enabled by default; the EXPERIMENTAL category controls activation
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    rules.forEach { rule ->
      assertTrue(
        rule.enabledByDefault,
        "Experimental rule ${rule.id} should be enabled by default (category controls activation)",
      )
    }
  }

  @Test
  fun testAllExperimentalRules_haveDocumentationUrl() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    rules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} should have documentation URL")
      assertTrue(
        rule.documentationUrl!!.startsWith("http"),
        "Rule ${rule.id} documentation URL should be valid",
      )
    }
  }

  @Test
  fun testAllExperimentalRuleIds_areUnique() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    val ids = rules.map { it.id }
    val uniqueIds = ids.toSet()

    assertEquals(
      ids.size,
      uniqueIds.size,
      "Duplicate rule IDs found: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}",
    )
  }

  @Test
  fun testAllExperimentalRuleIds_matchExpectedList() {
    val expectedIds = listOf(
      "LazyListMissingKey",
      "LazyListContentType",
      "UnstableLazyListItems",
      "MethodReferenceCandidate",
      "DerivedStateOfCandidate",
      "StateReadInComposition",
      "FrequentRecomposition",
    )

    val actualIds = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
      .map { it.id }
      .sorted()
    val expectedSorted = expectedIds.sorted()

    assertEquals(expectedSorted, actualIds)
  }

  // =============================================================================
  // SEVERITY DISTRIBUTION TESTS
  // =============================================================================

  @Test
  fun testExperimentalRuleSeverities_distribution() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    val warningCount = rules.count { it.severity == RuleSeverity.WARNING }
    val infoCount = rules.count { it.severity == RuleSeverity.INFO }
    val errorCount = rules.count { it.severity == RuleSeverity.ERROR }

    // Experimental rules should not have ERROR severity
    assertEquals(0, errorCount, "Experimental rules should not have ERROR severity")

    // Verify counts add up
    assertEquals(7, warningCount + infoCount)
  }

  @Test
  fun testWarningLevelExperimentalRules() {
    val warningRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
      .filter { it.severity == RuleSeverity.WARNING }
      .map { it.id }

    assertTrue("LazyListMissingKey" in warningRules)
    assertTrue("UnstableLazyListItems" in warningRules)
    assertTrue("StateReadInComposition" in warningRules)
    assertTrue("FrequentRecomposition" in warningRules)
  }

  @Test
  fun testInfoLevelExperimentalRules() {
    val infoRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
      .filter { it.severity == RuleSeverity.INFO }
      .map { it.id }

    assertTrue("LazyListContentType" in infoRules)
    assertTrue("MethodReferenceCandidate" in infoRules)
    assertTrue("DerivedStateOfCandidate" in infoRules)
  }
}
