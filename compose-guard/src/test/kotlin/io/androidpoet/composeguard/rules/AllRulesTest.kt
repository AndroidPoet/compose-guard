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
package io.androidpoet.composeguard.rules

import io.androidpoet.composeguard.rules.composables.ContentEmissionRule
import io.androidpoet.composeguard.rules.composables.ContentSlotReusedRule
import io.androidpoet.composeguard.rules.composables.EffectKeysRule
import io.androidpoet.composeguard.rules.composables.LazyListContentTypeRule
import io.androidpoet.composeguard.rules.composables.LazyListMissingKeyRule
import io.androidpoet.composeguard.rules.composables.MovableContentRule
import io.androidpoet.composeguard.rules.composables.MultipleContentRule
import io.androidpoet.composeguard.rules.composables.PreviewVisibilityRule
import io.androidpoet.composeguard.rules.effects.LambdaParameterInEffectRule
import io.androidpoet.composeguard.rules.modifiers.AvoidComposedRule
import io.androidpoet.composeguard.rules.modifiers.ModifierDefaultValueRule
import io.androidpoet.composeguard.rules.modifiers.ModifierNamingRule
import io.androidpoet.composeguard.rules.modifiers.ModifierOrderRule
import io.androidpoet.composeguard.rules.modifiers.ModifierRequiredRule
import io.androidpoet.composeguard.rules.modifiers.ModifierReuseRule
import io.androidpoet.composeguard.rules.modifiers.ModifierTopMostRule
import io.androidpoet.composeguard.rules.naming.ComposableAnnotationNamingRule
import io.androidpoet.composeguard.rules.naming.ComposableNamingRule
import io.androidpoet.composeguard.rules.naming.CompositionLocalNamingRule
import io.androidpoet.composeguard.rules.naming.EventParameterNamingRule
import io.androidpoet.composeguard.rules.naming.MultipreviewNamingRule
import io.androidpoet.composeguard.rules.naming.PreviewNamingRule
import io.androidpoet.composeguard.rules.parameters.ExplicitDependenciesRule
import io.androidpoet.composeguard.rules.parameters.MutableParameterRule
import io.androidpoet.composeguard.rules.parameters.ParameterOrderingRule
import io.androidpoet.composeguard.rules.parameters.TrailingLambdaRule
import io.androidpoet.composeguard.rules.parameters.ViewModelForwardingRule
import io.androidpoet.composeguard.rules.state.DerivedStateOfCandidateRule
import io.androidpoet.composeguard.rules.state.FrequentRecompositionRule
import io.androidpoet.composeguard.rules.state.HoistStateRule
import io.androidpoet.composeguard.rules.state.MutableStateParameterRule
import io.androidpoet.composeguard.rules.state.RememberStateRule
import io.androidpoet.composeguard.rules.state.TypeSpecificStateRule
import io.androidpoet.composeguard.rules.stricter.Material2Rule
import io.androidpoet.composeguard.rules.stricter.UnstableCollectionsRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AllRulesTest {


  @Test
  fun testComposableNamingRule_metadata() {
    val rule = ComposableNamingRule()

    assertEquals("ComposableNaming", rule.id)
    assertEquals("Composable Naming Convention", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertTrue(rule.documentationUrl.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun testCompositionLocalNamingRule_metadata() {
    val rule = CompositionLocalNamingRule()

    assertEquals("CompositionLocalNaming", rule.id)
    assertEquals("CompositionLocal Naming", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testPreviewNamingRule_metadata() {
    val rule = PreviewNamingRule()

    assertEquals("PreviewNaming", rule.id)
    assertEquals("Preview Naming Convention", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testMultipreviewNamingRule_metadata() {
    val rule = MultipreviewNamingRule()

    assertEquals("MultipreviewNaming", rule.id)
    assertEquals("Multipreview Naming", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testComposableAnnotationNamingRule_metadata() {
    val rule = ComposableAnnotationNamingRule()

    assertEquals("ComposableAnnotationNaming", rule.id)
    assertEquals("Composable Annotation Naming", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testEventParameterNamingRule_metadata() {
    val rule = EventParameterNamingRule()

    assertEquals("EventParameterNaming", rule.id)
    assertEquals("Event Parameter Naming", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun testModifierRequiredRule_metadata() {
    val rule = ModifierRequiredRule()

    assertEquals("ModifierRequired", rule.id)
    assertEquals("Modifier Parameter Required", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testModifierDefaultValueRule_metadata() {
    val rule = ModifierDefaultValueRule()

    assertEquals("ModifierDefaultValue", rule.id)
    assertEquals("Modifier Should Have Default Value", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testModifierNamingRule_metadata() {
    val rule = ModifierNamingRule()

    assertEquals("ModifierNaming", rule.id)
    assertEquals("Modifier Naming Convention", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testModifierTopMostRule_metadata() {
    val rule = ModifierTopMostRule()

    assertEquals("ModifierTopMost", rule.id)
    assertEquals("Modifier at Top-Most Layout", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testModifierReuseRule_metadata() {
    val rule = ModifierReuseRule()

    assertEquals("ModifierReuse", rule.id)
    assertEquals("Don't Re-use Modifiers", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testModifierOrderRule_metadata() {
    val rule = ModifierOrderRule()

    assertEquals("ModifierOrder", rule.id)
    assertEquals("Modifier Order Matters", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testAvoidComposedRule_metadata() {
    val rule = AvoidComposedRule()

    assertEquals("AvoidComposed", rule.id)
    assertEquals("Avoid composed {} Modifier", rule.name)
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun testRememberStateRule_metadata() {
    val rule = RememberStateRule()

    assertEquals("RememberState", rule.id)
    assertEquals("State Should Be Remembered", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.ERROR, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testTypeSpecificStateRule_metadata() {
    val rule = TypeSpecificStateRule()

    assertEquals("TypeSpecificState", rule.id)
    assertEquals("Use Type-Specific State Variants", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testMutableStateParameterRule_metadata() {
    val rule = MutableStateParameterRule()

    assertEquals("MutableStateParameter", rule.id)
    assertEquals("Don't Use MutableState as Parameter", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testHoistStateRule_metadata() {
    val rule = HoistStateRule()

    assertEquals("HoistState", rule.id)
    assertEquals("Consider Hoisting State", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testLambdaParameterInEffectRule_metadata() {
    val rule = LambdaParameterInEffectRule()

    assertEquals("LambdaParameterInEffect", rule.id)
    assertEquals("Lambda Parameters in Restartable Effects", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testDerivedStateOfCandidateRule_metadata() {
    val rule = DerivedStateOfCandidateRule()

    assertEquals("DerivedStateOfCandidate", rule.id)
    assertEquals("Consider Using remember with keys", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testFrequentRecompositionRule_metadata() {
    val rule = FrequentRecompositionRule()

    assertEquals("FrequentRecomposition", rule.id)
    assertEquals("Potential Excessive Recomposition", rule.name)
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
  }


  @Test
  fun testParameterOrderingRule_metadata() {
    val rule = ParameterOrderingRule()

    assertEquals("ParameterOrdering", rule.id)
    assertEquals("Parameter Ordering", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testTrailingLambdaRule_metadata() {
    val rule = TrailingLambdaRule()

    assertEquals("TrailingLambda", rule.id)
    assertEquals("Trailing Lambda Rules", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testMutableParameterRule_metadata() {
    val rule = MutableParameterRule()

    assertEquals("MutableParameter", rule.id)
    assertEquals("Don't Use Mutable Types as Parameters", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testExplicitDependenciesRule_metadata() {
    val rule = ExplicitDependenciesRule()

    assertEquals("ExplicitDependencies", rule.id)
    assertEquals("Make Dependencies Explicit", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testViewModelForwardingRule_metadata() {
    val rule = ViewModelForwardingRule()

    assertEquals("ViewModelForwarding", rule.id)
    assertEquals("Don't Forward ViewModels", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun testContentEmissionRule_metadata() {
    val rule = ContentEmissionRule()

    assertEquals("ContentEmission", rule.id)
    assertEquals("Don't Emit Content and Return", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testMultipleContentRule_metadata() {
    val rule = MultipleContentRule()

    assertEquals("MultipleContentEmitters", rule.id)
    assertEquals("Multiple Content Emitters", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testContentSlotReusedRule_metadata() {
    val rule = ContentSlotReusedRule()

    assertEquals("ContentSlotReused", rule.id)
    assertEquals("Content Slots Should Not Be Reused", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testEffectKeysRule_metadata() {
    val rule = EffectKeysRule()

    assertEquals("EffectKeys", rule.id)
    assertEquals("Effect Keys Correctness", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testMovableContentRule_metadata() {
    val rule = MovableContentRule()

    assertEquals("MovableContent", rule.id)
    assertEquals("Movable Content Should Be Remembered", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.ERROR, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testPreviewVisibilityRule_metadata() {
    val rule = PreviewVisibilityRule()

    assertEquals("PreviewVisibility", rule.id)
    assertEquals("Preview Should Be Private", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testLazyListContentTypeRule_metadata() {
    val rule = LazyListContentTypeRule()

    assertEquals("LazyListContentType", rule.id)
    assertEquals("LazyList Missing ContentType", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testLazyListMissingKeyRule_metadata() {
    val rule = LazyListMissingKeyRule()

    assertEquals("LazyListMissingKey", rule.id)
    assertEquals("LazyList Missing Key Parameter", rule.name)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
    assertNotNull(rule.documentationUrl)
  }


  @Test
  fun testMaterial2Rule_metadata() {
    val rule = Material2Rule()

    assertEquals("Material2Usage", rule.id)
    assertEquals("Don't Use Material 2", rule.name)
    assertEquals(RuleCategory.STRICTER, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }

  @Test
  fun testUnstableCollectionsRule_metadata() {
    val rule = UnstableCollectionsRule()

    assertEquals("UnstableCollections", rule.id)
    assertEquals("Avoid Unstable Collections", rule.name)
    assertEquals(RuleCategory.STRICTER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun testAllNamingRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.NAMING)
    assertEquals(6, rules.size)
  }

  @Test
  fun testAllModifierRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.MODIFIER)
    assertEquals(7, rules.size)
  }

  @Test
  fun testAllStateRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.STATE)
    assertEquals(8, rules.size)
  }

  @Test
  fun testAllParameterRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.PARAMETER)
    assertEquals(5, rules.size)
  }

  @Test
  fun testAllComposableRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.COMPOSABLE)
    assertEquals(8, rules.size)
  }

  @Test
  fun testAllStricterRules_count() {
    val rules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.STRICTER)
    assertEquals(2, rules.size)
  }

  @Test
  fun testTotalRuleCount() {
    assertEquals(36, ComposeRuleRegistry.getRuleCount())
  }


  @Test
  fun testAllRuleIds_areUnique() {
    val rules = ComposeRuleRegistry.getAllRules()
    val ids = rules.map { it.id }
    val uniqueIds = ids.toSet()

    assertEquals(ids.size, uniqueIds.size, "Duplicate rule IDs found: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}")
  }

  @Test
  fun testAllRuleIds_matchExpectedList() {
    val expectedIds = listOf(
      "ComposableNaming",
      "CompositionLocalNaming",
      "PreviewNaming",
      "MultipreviewNaming",
      "ComposableAnnotationNaming",
      "EventParameterNaming",
      "ModifierRequired",
      "ModifierDefaultValue",
      "ModifierNaming",
      "ModifierTopMost",
      "ModifierReuse",
      "ModifierOrder",
      "AvoidComposed",
      "RememberState",
      "TypeSpecificState",
      "MutableStateParameter",
      "HoistState",
      "LambdaParameterInEffect",
      "DerivedStateOfCandidate",
      "FrequentRecomposition",
      "DeferStateReads",
      "ParameterOrdering",
      "TrailingLambda",
      "MutableParameter",
      "ExplicitDependencies",
      "ViewModelForwarding",
      "ContentEmission",
      "MultipleContentEmitters",
      "ContentSlotReused",
      "EffectKeys",
      "MovableContent",
      "PreviewVisibility",
      "LazyListContentType",
      "LazyListMissingKey",
      "Material2Usage",
      "UnstableCollections",
    )

    val actualIds = ComposeRuleRegistry.getAllRules().map { it.id }.sorted()
    val expectedSorted = expectedIds.sorted()

    assertEquals(expectedSorted, actualIds)
  }


  @Test
  fun testAllRules_haveDocumentationUrl() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} has null documentation URL")
      assertTrue(
        rule.documentationUrl!!.isNotBlank(),
        "Rule ${rule.id} has empty documentation URL",
      )
    }
  }

  @Test
  fun testAllRules_documentationUrlIsValid() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} has null documentation URL")
      assertTrue(
        rule.documentationUrl!!.startsWith("http"),
        "Rule ${rule.id} has invalid documentation URL: ${rule.documentationUrl}",
      )
    }
  }


  @Test
  fun testRuleSeverities_distribution() {
    val rules = ComposeRuleRegistry.getAllRules()

    val errorCount = rules.count { it.severity == RuleSeverity.ERROR }
    val warningCount = rules.count { it.severity == RuleSeverity.WARNING }
    val weakWarningCount = rules.count { it.severity == RuleSeverity.WEAK_WARNING }
    val infoCount = rules.count { it.severity == RuleSeverity.INFO }

    val totalWarnings = warningCount + weakWarningCount
    assertTrue(totalWarnings > errorCount, "Expected more warnings than errors")
    assertTrue(totalWarnings > infoCount, "Expected more warnings than info")

    assertEquals(36, errorCount + warningCount + weakWarningCount + infoCount)
  }

  @Test
  fun testErrorSeverityRules() {
    val errorRules = ComposeRuleRegistry.getAllRules()
      .filter { it.severity == RuleSeverity.ERROR }
      .map { it.id }

    assertTrue("RememberState" in errorRules, "RememberState should be ERROR severity")
    assertTrue("MovableContent" in errorRules, "MovableContent should be ERROR severity")
  }

  @Test
  fun testInfoSeverityRules() {
    val infoRules = ComposeRuleRegistry.getAllRules()
      .filter { it.severity == RuleSeverity.INFO }
      .map { it.id }

    assertTrue("HoistState" in infoRules, "HoistState should be INFO severity")
    assertTrue("Material2Usage" in infoRules, "Material2Usage should be INFO severity")
    assertTrue("LazyListContentType" in infoRules, "LazyListContentType should be INFO severity")
  }

  @Test
  fun testWeakWarningSeverityRules() {
    val weakWarningRules = ComposeRuleRegistry.getAllRules()
      .filter { it.severity == RuleSeverity.WEAK_WARNING }
      .map { it.id }

    assertTrue("ExplicitDependencies" in weakWarningRules, "ExplicitDependencies should be WEAK_WARNING severity")
  }
}
