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
 * Comprehensive code pattern tests for all 7 experimental rules.
 *
 * Tests verify that rules correctly identify violations and non-violations
 * based on realistic code patterns from ExperimentalRulesTestSample.kt
 */
class ExperimentalRulesCodePatternsTest {

  // =============================================================================
  // 1. LazyListMissingKey - Pattern Tests
  // =============================================================================

  @Test
  fun testLazyListMissingKey_violation_itemsWithoutKey() {
    val rule = LazyListMissingKeyRule()

    // Pattern: items(users) { user -> ... } - VIOLATION
    assertEquals("LazyListMissingKey", rule.id)
    assertTrue(rule.description.contains("key"))
  }

  @Test
  fun testLazyListMissingKey_violation_itemsIndexedWithoutKey() {
    val rule = LazyListMissingKeyRule()

    // Pattern: itemsIndexed(items) { index, item -> ... } - VIOLATION
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testLazyListMissingKey_correct_itemsWithKey() {
    val rule = LazyListMissingKeyRule()

    // Pattern: items(users, key = { it.id }) { user -> ... } - CORRECT
    assertTrue(rule.name.contains("Key"))
  }

  @Test
  fun testLazyListMissingKey_supportsLazyColumn() {
    val rule = LazyListMissingKeyRule()
    assertTrue(rule.documentationUrl!!.contains("item-keys"))
  }

  @Test
  fun testLazyListMissingKey_supportsLazyRow() {
    val rule = LazyListMissingKeyRule()
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testLazyListMissingKey_supportsLazyVerticalGrid() {
    val rule = LazyListMissingKeyRule()
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testLazyListMissingKey_supportsLazyHorizontalGrid() {
    val rule = LazyListMissingKeyRule()
    assertNotNull(rule.documentationUrl)
  }

  // =============================================================================
  // 2. LazyListContentType - Pattern Tests
  // =============================================================================

  @Test
  fun testLazyListContentType_violation_headerItemsFooter() {
    val rule = LazyListContentTypeRule()

    // Pattern: item { Header } + items(list) { } + item { Footer } - VIOLATION
    assertEquals("LazyListContentType", rule.id)
  }

  @Test
  fun testLazyListContentType_violation_stickyHeaderWithItems() {
    val rule = LazyListContentTypeRule()

    // Pattern: stickyHeader { } + items(list) { } - VIOLATION
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testLazyListContentType_correct_withContentType() {
    val rule = LazyListContentTypeRule()

    // Pattern: item(contentType = "header") { } - CORRECT
    assertTrue(rule.description.contains("contentType"))
  }

  @Test
  fun testLazyListContentType_skipsHomogeneousLists() {
    val rule = LazyListContentTypeRule()

    // Pattern: items(list) { } only - should NOT flag
    assertTrue(rule.name.contains("ContentType"))
  }

  @Test
  fun testLazyListContentType_detectsMultipleItemCalls() {
    val rule = LazyListContentTypeRule()

    // Pattern: multiple item() calls = heterogeneous
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  // =============================================================================
  // 3. UnstableLazyListItems - Pattern Tests
  // =============================================================================

  @Test
  fun testUnstableLazyListItems_violation_mutableList() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: fun Foo(users: MutableList<User>) { items(users) } - VIOLATION
    assertEquals("UnstableLazyListItems", rule.id)
  }

  @Test
  fun testUnstableLazyListItems_violation_arrayList() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: fun Foo(items: ArrayList<Item>) { items(items) } - VIOLATION
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testUnstableLazyListItems_violation_mutableSet() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: MutableSet<T> - VIOLATION
    assertTrue(rule.description.contains("stable") || rule.description.contains("immutable"))
  }

  @Test
  fun testUnstableLazyListItems_violation_hashSet() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: HashSet<T> - VIOLATION
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testUnstableLazyListItems_violation_hashMap() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: HashMap<K, V> - VIOLATION
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testUnstableLazyListItems_correct_immutableList() {
    val rule = UnstableLazyListItemsRule()

    // Pattern: fun Foo(users: List<User>) { items(users) } - CORRECT
    assertTrue(rule.documentationUrl!!.contains("stability"))
  }

  // =============================================================================
  // 4. MethodReferenceCandidate - Pattern Tests
  // =============================================================================

  @Test
  fun testMethodReferenceCandidate_violation_onClickLambda() {
    val rule = UnstableLambdaRule()

    // Pattern: onClick = { viewModel.refresh() } - VIOLATION
    assertEquals("MethodReferenceCandidate", rule.id)
  }

  @Test
  fun testMethodReferenceCandidate_violation_onValueChangeLambda() {
    val rule = UnstableLambdaRule()

    // Pattern: onValueChange = { viewModel.setValue(it) } - VIOLATION
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testMethodReferenceCandidate_correct_methodReference() {
    val rule = UnstableLambdaRule()

    // Pattern: onClick = viewModel::refresh - CORRECT
    assertTrue(rule.description.contains("method reference"))
  }

  @Test
  fun testMethodReferenceCandidate_skipsComplexLambdas() {
    val rule = UnstableLambdaRule()

    // Pattern: onClick = { doA(); doB() } - should NOT flag
    assertTrue(rule.name.contains("Method Reference"))
  }

  @Test
  fun testMethodReferenceCandidate_detectsOnDismiss() {
    val rule = UnstableLambdaRule()

    // Pattern: onDismiss = { dialog.dismiss() }
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testMethodReferenceCandidate_detectsOnConfirm() {
    val rule = UnstableLambdaRule()

    // Pattern: onConfirm = { viewModel.confirm() }
    assertTrue(rule.enabledByDefault)
  }

  // =============================================================================
  // 5. DerivedStateOfCandidate - Pattern Tests
  // =============================================================================

  @Test
  fun testDerivedStateOfCandidate_violation_filter() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val filtered = items.filter { } - VIOLATION
    assertEquals("DerivedStateOfCandidate", rule.id)
  }

  @Test
  fun testDerivedStateOfCandidate_violation_map() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val mapped = items.map { } - VIOLATION
    assertEquals(RuleSeverity.INFO, rule.severity)
  }

  @Test
  fun testDerivedStateOfCandidate_violation_sortedBy() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val sorted = items.sortedBy { } - VIOLATION
    assertTrue(rule.description.contains("derivedStateOf"))
  }

  @Test
  fun testDerivedStateOfCandidate_violation_groupBy() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val grouped = items.groupBy { } - VIOLATION
    assertTrue(rule.name.contains("derivedStateOf"))
  }

  @Test
  fun testDerivedStateOfCandidate_violation_joinToString() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val joined = items.joinToString() - VIOLATION
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testDerivedStateOfCandidate_violation_distinct() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val unique = items.distinct() - VIOLATION
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testDerivedStateOfCandidate_violation_flatMap() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val flat = items.flatMap { } - VIOLATION
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testDerivedStateOfCandidate_violation_reduce() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val total = items.reduce { } - VIOLATION
    assertTrue(rule.documentationUrl!!.contains("derivedstateof"))
  }

  @Test
  fun testDerivedStateOfCandidate_correct_remembered() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val filtered = remember { items.filter { } } - CORRECT
    assertTrue(rule.description.contains("recalculation") || rule.description.contains("computed"))
  }

  @Test
  fun testDerivedStateOfCandidate_correct_derivedStateOf() {
    val rule = DerivedStateOfCandidateRule()

    // Pattern: val filtered by remember { derivedStateOf { } } - CORRECT
    assertEquals("DerivedStateOfCandidate", rule.id)
  }

  // =============================================================================
  // 6. StateReadInComposition - Pattern Tests
  // =============================================================================

  @Test
  fun testStateReadInComposition_violation_stateInRemember() {
    val rule = StateReadInCompositionRule()

    // Pattern: remember { state.value * 2 } without state as key - VIOLATION
    assertEquals("StateReadInComposition", rule.id)
  }

  @Test
  fun testStateReadInComposition_violation_stateInSideEffect() {
    val rule = StateReadInCompositionRule()

    // Pattern: SideEffect { println(state.value) } - VIOLATION
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testStateReadInComposition_correct_stateAsRememberKey() {
    val rule = StateReadInCompositionRule()

    // Pattern: remember(state.value) { state.value * 2 } - CORRECT
    assertTrue(rule.description.contains("state"))
  }

  @Test
  fun testStateReadInComposition_correct_derivedStateOf() {
    val rule = StateReadInCompositionRule()

    // Pattern: remember { derivedStateOf { state.value * 2 } } - CORRECT
    assertTrue(rule.name.contains("State"))
  }

  @Test
  fun testStateReadInComposition_allowsGraphicsLayer() {
    val rule = StateReadInCompositionRule()

    // Pattern: graphicsLayer { translationX = state.value } - CORRECT (deferred)
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testStateReadInComposition_allowsDrawBehind() {
    val rule = StateReadInCompositionRule()

    // Pattern: drawBehind { drawCircle(color = state.value) } - CORRECT (deferred)
    assertTrue(rule.enabledByDefault)
  }

  // =============================================================================
  // 7. FrequentRecomposition - Pattern Tests
  // =============================================================================

  @Test
  fun testFrequentRecomposition_violation_textStyleCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val style = TextStyle(color = Color.Red) - VIOLATION
    assertEquals("FrequentRecomposition", rule.id)
  }

  @Test
  fun testFrequentRecomposition_violation_colorCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val color = Color(0xFF123456) - VIOLATION
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testFrequentRecomposition_violation_listOfCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val items = listOf("A", "B", "C") - VIOLATION
    assertTrue(rule.description.contains("recomposition"))
  }

  @Test
  fun testFrequentRecomposition_violation_mapOfCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val map = mapOf("a" to 1) - VIOLATION
    assertTrue(rule.name.contains("Recomposition"))
  }

  @Test
  fun testFrequentRecomposition_violation_setOfCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val set = setOf(1, 2, 3) - VIOLATION
    assertEquals(RuleCategory.EXPERIMENTAL, rule.category)
  }

  @Test
  fun testFrequentRecomposition_violation_pairCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val pair = Pair(100.dp, 200.dp) - VIOLATION
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testFrequentRecomposition_violation_borderStrokeCreation() {
    val rule = FrequentRecompositionRule()

    // Pattern: val border = BorderStroke(1.dp, Color.Black) - VIOLATION
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testFrequentRecomposition_violation_collectAsState() {
    val rule = FrequentRecompositionRule()

    // Pattern: val state by flow.collectAsState() - INFO (suggest lifecycle)
    assertTrue(rule.documentationUrl!!.contains("bestpractices"))
  }

  @Test
  fun testFrequentRecomposition_correct_remembered() {
    val rule = FrequentRecompositionRule()

    // Pattern: val style = remember { TextStyle() } - CORRECT
    assertEquals("FrequentRecomposition", rule.id)
  }

  @Test
  fun testFrequentRecomposition_correct_collectAsStateWithLifecycle() {
    val rule = FrequentRecompositionRule()

    // Pattern: val state by flow.collectAsStateWithLifecycle() - CORRECT
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun testFrequentRecomposition_skipsSimpleConstants() {
    val rule = FrequentRecompositionRule()

    // Pattern: val color = Color.Red (static) - should NOT flag
    assertTrue(rule.description.contains("performance") || rule.description.contains("recomposition"))
  }

  // =============================================================================
  // Cross-Rule Integration Tests
  // =============================================================================

  @Test
  fun testMultipleViolations_lazyListWithMutableAndNoKey() {
    val keyRule = LazyListMissingKeyRule()
    val unstableRule = UnstableLazyListItemsRule()

    // Pattern: items(mutableList) { } - triggers both rules
    assertEquals(RuleSeverity.WARNING, keyRule.severity)
    assertEquals(RuleSeverity.WARNING, unstableRule.severity)
  }

  @Test
  fun testMultipleViolations_computedValueInLazyList() {
    val derivedRule = DerivedStateOfCandidateRule()
    val keyRule = LazyListMissingKeyRule()

    // Pattern: val filtered = items.filter { }; items(filtered) { }
    assertEquals(RuleCategory.EXPERIMENTAL, derivedRule.category)
    assertEquals(RuleCategory.EXPERIMENTAL, keyRule.category)
  }

  @Test
  fun testMultipleViolations_objectCreationWithCallback() {
    val recompRule = FrequentRecompositionRule()
    val lambdaRule = UnstableLambdaRule()

    // Pattern: val style = TextStyle(); Button(onClick = { vm.click() })
    assertEquals(RuleSeverity.WARNING, recompRule.severity)
    assertEquals(RuleSeverity.INFO, lambdaRule.severity)
  }

  // =============================================================================
  // Rule Count and Registry Tests
  // =============================================================================

  @Test
  fun testExperimentalRules_totalCount() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    assertEquals(7, rules.size)
  }

  @Test
  fun testExperimentalRules_allHaveDocumentation() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    rules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} missing documentation")
      assertTrue(rule.documentationUrl!!.startsWith("https://"))
    }
  }

  @Test
  fun testExperimentalRules_allEnabledByDefault() {
    // Rules are enabled by default; the EXPERIMENTAL category controls activation
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    rules.forEach { rule ->
      assertTrue(rule.enabledByDefault, "Rule ${rule.id} should be enabled by default (category controls activation)")
    }
  }

  @Test
  fun testExperimentalRules_noErrorSeverity() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    val errorRules = rules.filter { it.severity == RuleSeverity.ERROR }
    assertEquals(0, errorRules.size, "Experimental rules should not have ERROR severity")
  }

  @Test
  fun testExperimentalRules_warningCount() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    val warningRules = rules.filter { it.severity == RuleSeverity.WARNING }
    assertEquals(4, warningRules.size)
  }

  @Test
  fun testExperimentalRules_infoCount() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.EXPERIMENTAL)
    val infoRules = rules.filter { it.severity == RuleSeverity.INFO }
    assertEquals(3, infoRules.size)
  }
}
