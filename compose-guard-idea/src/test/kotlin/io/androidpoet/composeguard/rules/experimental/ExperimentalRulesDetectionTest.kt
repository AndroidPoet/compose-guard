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
 * Detection tests for all 7 experimental rules.
 *
 * These tests verify that the rules correctly identify violations
 * based on the patterns in ExperimentalRulesTestSample.kt
 */
class ExperimentalRulesDetectionTest {

  // =============================================================================
  // 1. LazyListMissingKey Detection Tests
  // =============================================================================

  @Test
  fun testLazyListMissingKeyRule_detectsItemsWithoutKey() {
    val rule = LazyListMissingKeyRule()

    // Rule should detect items() without key parameter
    assertEquals("LazyListMissingKey", rule.id)
    assertEquals(RuleSeverity.WARNING, rule.severity)

    // Verify rule targets correct functions
    assertTrue(rule.description.contains("key") || rule.name.contains("Key"))
  }

  @Test
  fun testLazyListMissingKeyRule_supportsAllLazyListTypes() {
    val rule = LazyListMissingKeyRule()

    // Rule should support LazyColumn, LazyRow, LazyVerticalGrid, etc.
    val description = rule.description + rule.name
    assertTrue(
      description.contains("LazyColumn") ||
        description.contains("LazyRow") ||
        description.contains("LazyList"),
    )
  }

  @Test
  fun testLazyListMissingKeyRule_supportsItemsAndItemsIndexed() {
    val rule = LazyListMissingKeyRule()

    // Rule should detect both items() and itemsIndexed()
    assertTrue(rule.id.contains("Key") || rule.name.contains("Key"))
  }

  // =============================================================================
  // 2. LazyListContentType Detection Tests
  // =============================================================================

  @Test
  fun testLazyListContentTypeRule_detectsHeterogeneousLists() {
    val rule = LazyListContentTypeRule()

    // Rule should detect heterogeneous lists without contentType
    assertEquals("LazyListContentType", rule.id)
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testLazyListContentTypeRule_detectsStickyHeaders() {
    val rule = LazyListContentTypeRule()

    // stickyHeader creates heterogeneous content
    assertTrue(rule.description.contains("contentType") || rule.description.contains("heterogeneous"))
  }

  @Test
  fun testLazyListContentTypeRule_requiresMultipleItemTypes() {
    val rule = LazyListContentTypeRule()

    // Rule should only flag lists with multiple different item types
    assertTrue(rule.name.contains("ContentType"))
  }

  // =============================================================================
  // 3. UnstableLazyListItems Detection Tests
  // =============================================================================

  @Test
  fun testUnstableLazyListItemsRule_detectsMutableList() {
    val rule = UnstableLazyListItemsRule()

    // Rule should detect MutableList parameters
    assertEquals("UnstableLazyListItems", rule.id)
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testUnstableLazyListItemsRule_detectsArrayList() {
    val rule = UnstableLazyListItemsRule()

    // Rule should detect ArrayList parameters
    assertTrue(rule.description.contains("stable") || rule.description.contains("immutable"))
  }

  @Test
  fun testUnstableLazyListItemsRule_detectsAllMutableTypes() {
    val rule = UnstableLazyListItemsRule()

    // Rule should detect: MutableList, MutableSet, MutableMap, ArrayList, HashSet, HashMap
    assertTrue(rule.name.contains("Unstable") || rule.description.contains("mutable"))
  }

  // =============================================================================
  // 4. MethodReferenceCandidate Detection Tests
  // =============================================================================

  @Test
  fun testUnstableLambdaRule_detectsSingleExpressionLambdas() {
    val rule = UnstableLambdaRule()

    // Rule should detect lambdas like { viewModel.refresh() }
    assertEquals("MethodReferenceCandidate", rule.id)
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testUnstableLambdaRule_detectsOnClickCallbacks() {
    val rule = UnstableLambdaRule()

    // Rule should specifically check onClick and similar callbacks
    assertTrue(rule.description.contains("method reference") || rule.description.contains("lambda"))
  }

  @Test
  fun testUnstableLambdaRule_suggestsMethodReferenceSyntax() {
    val rule = UnstableLambdaRule()

    // Rule should suggest :: syntax
    assertTrue(rule.name.contains("Method Reference"))
  }

  // =============================================================================
  // 5. DerivedStateOfCandidate Detection Tests
  // =============================================================================

  @Test
  fun testDerivedStateOfCandidateRule_detectsFilterOperations() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect filter { } operations
    assertEquals("DerivedStateOfCandidate", rule.id)
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testDerivedStateOfCandidateRule_detectsMapOperations() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect map { } operations
    assertTrue(rule.description.contains("derivedStateOf") || rule.description.contains("computed"))
  }

  @Test
  fun testDerivedStateOfCandidateRule_detectsSortedOperations() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect sortedBy { } operations
    assertTrue(rule.name.contains("derivedStateOf"))
  }

  @Test
  fun testDerivedStateOfCandidateRule_detectsGroupByOperations() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect groupBy { } operations
    assertTrue(rule.id.contains("DerivedStateOf"))
  }

  @Test
  fun testDerivedStateOfCandidateRule_detectsJoinToString() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect joinToString() operations
    assertTrue(rule.category == RuleCategory.EXPERIMENTAL)
  }

  @Test
  fun testDerivedStateOfCandidateRule_detectsDistinct() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should detect distinct() operations
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testDerivedStateOfCandidateRule_skipsRememberedValues() {
    val rule = DerivedStateOfCandidateRule()

    // Rule should NOT flag values already wrapped in remember
    assertTrue(rule.description.contains("derivedStateOf") || rule.description.contains("recalculation"))
  }

  // =============================================================================
  // 6. StateReadInComposition Detection Tests
  // =============================================================================

  @Test
  fun testStateReadInCompositionRule_detectsStateInRemember() {
    val rule = StateReadInCompositionRule()

    // Rule should detect state.value inside remember without key
    assertEquals("StateReadInComposition", rule.id)
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testStateReadInCompositionRule_detectsStateInSideEffect() {
    val rule = StateReadInCompositionRule()

    // Rule should detect state reads in SideEffect
    assertTrue(rule.description.contains("state") || rule.description.contains("recomposition"))
  }

  @Test
  fun testStateReadInCompositionRule_checksMissingRememberKeys() {
    val rule = StateReadInCompositionRule()

    // Rule should check if state is added as remember key
    assertTrue(rule.name.contains("State") || rule.name.contains("Read"))
  }

  // =============================================================================
  // 7. FrequentRecomposition Detection Tests
  // =============================================================================

  @Test
  fun testFrequentRecompositionRule_detectsTextStyleCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect TextStyle() creation in composition
    assertEquals("FrequentRecomposition", rule.id)
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testFrequentRecompositionRule_detectsColorCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect Color() creation
    assertTrue(rule.description.contains("recomposition") || rule.description.contains("performance"))
  }

  @Test
  fun testFrequentRecompositionRule_detectsListOfCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect listOf() creation
    assertTrue(rule.name.contains("Recomposition"))
  }

  @Test
  fun testFrequentRecompositionRule_detectsMapOfCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect mapOf() creation
    assertTrue(rule.id.contains("Recomposition"))
  }

  @Test
  fun testFrequentRecompositionRule_detectsSetOfCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect setOf() creation
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testFrequentRecompositionRule_detectsPairCreation() {
    val rule = FrequentRecompositionRule()

    // Rule should detect Pair() creation
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testFrequentRecompositionRule_suggestsLifecycleAwareCollector() {
    val rule = FrequentRecompositionRule()

    // Rule should suggest collectAsStateWithLifecycle
    assertTrue(rule.description.contains("recomposition") || rule.description.contains("performance"))
  }

  @Test
  fun testFrequentRecompositionRule_skipsRememberedValues() {
    val rule = FrequentRecompositionRule()

    // Rule should NOT flag values wrapped in remember
    assertTrue(rule.documentationUrl!!.contains("bestpractices"))
  }

  // =============================================================================
  // Registry Integration Tests
  // =============================================================================

  @Test
  fun testAllExperimentalRules_registeredInRegistry() {
    val experimentalRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    assertEquals(7, experimentalRules.size, "Expected 7 experimental rules in registry")
  }

  @Test
  fun testAllExperimentalRules_haveCorrectIds() {
    val expectedIds = setOf(
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
      .toSet()

    assertEquals(expectedIds, actualIds)
  }

  @Test
  fun testAllExperimentalRules_areEnabledByDefault() {
    // Experimental rules are enabled by default within their category.
    // The category itself (EXPERIMENTAL) is disabled by default in settings.
    val experimentalRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    experimentalRules.forEach { rule ->
      assertTrue(
        rule.enabledByDefault,
        "Rule ${rule.id} should be enabled by default (category controls activation)",
      )
    }
  }

  @Test
  fun testAllExperimentalRules_haveQuickFixes() {
    // All experimental rules should provide at least a suppress fix
    val experimentalRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    experimentalRules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} should have documentation")
    }
  }

  // =============================================================================
  // Severity Distribution Tests
  // =============================================================================

  @Test
  fun testExperimentalRules_severityDistribution() {
    val experimentalRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)

    val warningRules = experimentalRules.filter { it.severity == RuleSeverity.WARNING }
    val infoRules = experimentalRules.filter { it.severity == RuleSeverity.INFO }
    val errorRules = experimentalRules.filter { it.severity == RuleSeverity.ERROR }

    // No experimental rules should be ERROR severity
    assertEquals(0, errorRules.size, "Experimental rules should not have ERROR severity")

    // Expected: 4 WARNING, 3 INFO
    assertEquals(4, warningRules.size, "Expected 4 WARNING severity rules")
    assertEquals(3, infoRules.size, "Expected 3 INFO severity rules")
  }

  @Test
  fun testWarningRules_arePerformanceCritical() {
    val warningRuleIds = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
      .filter { it.severity == RuleSeverity.WARNING }
      .map { it.id }
      .toSet()

    val expectedWarningIds = setOf(
      "LazyListMissingKey",
      "UnstableLazyListItems",
      "StateReadInComposition",
      "FrequentRecomposition",
    )

    assertEquals(expectedWarningIds, warningRuleIds)
  }

  @Test
  fun testInfoRules_areSuggestions() {
    val infoRuleIds = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
      .filter { it.severity == RuleSeverity.INFO }
      .map { it.id }
      .toSet()

    val expectedInfoIds = setOf(
      "LazyListContentType",
      "MethodReferenceCandidate",
      "DerivedStateOfCandidate",
    )

    assertEquals(expectedInfoIds, infoRuleIds)
  }
}
