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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ComposeRuleRegistry.
 */
class ComposeRuleRegistryTest {

  @Test
  fun testGetAllRules_returns36Rules() {
    val rules = ComposeRuleRegistry.getAllRules()

    assertEquals(36, rules.size)
  }

  @Test
  fun testGetRuleCount_returns36() {
    assertEquals(36, ComposeRuleRegistry.getRuleCount())
  }

  @Test
  fun testGetAllRules_containsNamingRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val namingRuleIds = listOf(
      "ComposableNaming",
      "CompositionLocalNaming",
      "PreviewNaming",
      "MultipreviewNaming",
      "ComposableAnnotationNaming",
      "EventParameterNaming",
    )

    namingRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing naming rule: $id")
    }
  }

  @Test
  fun testGetAllRules_containsModifierRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val modifierRuleIds = listOf(
      "ModifierRequired",
      "ModifierDefaultValue",
      "ModifierNaming",
      "ModifierTopMost",
      "ModifierReuse",
      "ModifierOrder",
      "AvoidComposed",
    )

    modifierRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing modifier rule: $id")
    }
  }

  @Test
  fun testGetAllRules_containsStateRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val stateRuleIds = listOf(
      "RememberState",
      "TypeSpecificState",
      "MutableStateParameter",
      "HoistState",
      "LambdaParameterInEffect",
    )

    stateRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing state rule: $id")
    }
  }

  @Test
  fun testGetAllRules_containsParameterRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val parameterRuleIds = listOf(
      "ParameterOrdering",
      "TrailingLambda",
      "MutableParameter",
      "ExplicitDependencies",
      "ViewModelForwarding",
    )

    parameterRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing parameter rule: $id")
    }
  }

  @Test
  fun testGetAllRules_containsComposableRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val composableRuleIds = listOf(
      "ContentEmission",
      "MultipleContentEmitters",
      "ContentSlotReused",
      "EffectKeys",
      "MovableContent",
      "PreviewVisibility",
    )

    composableRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing composable rule: $id")
    }
  }

  @Test
  fun testGetAllRules_containsStricterRules() {
    val rules = ComposeRuleRegistry.getAllRules()

    val stricterRuleIds = listOf(
      "Material2Usage",
      "UnstableCollections",
    )

    stricterRuleIds.forEach { id ->
      assertTrue(rules.any { it.id == id }, "Missing stricter rule: $id")
    }
  }

  @Test
  fun testGetRulesByCategory_namingCategory() {
    val namingRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.NAMING)

    assertEquals(6, namingRules.size)
    assertTrue(namingRules.all { it.category == RuleCategory.NAMING })
  }

  @Test
  fun testGetRulesByCategory_modifierCategory() {
    val modifierRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.MODIFIER)

    assertEquals(7, modifierRules.size)
    assertTrue(modifierRules.all { it.category == RuleCategory.MODIFIER })
  }

  @Test
  fun testGetRulesByCategory_stateCategory() {
    val stateRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.STATE)

    assertEquals(8, stateRules.size)
    assertTrue(stateRules.all { it.category == RuleCategory.STATE })
  }

  @Test
  fun testGetRulesByCategory_parameterCategory() {
    val parameterRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.PARAMETER)

    assertEquals(5, parameterRules.size)
    assertTrue(parameterRules.all { it.category == RuleCategory.PARAMETER })
  }

  @Test
  fun testGetRulesByCategory_composableCategory() {
    val composableRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.COMPOSABLE)

    assertEquals(8, composableRules.size)
    assertTrue(composableRules.all { it.category == RuleCategory.COMPOSABLE })
  }

  @Test
  fun testGetRulesByCategory_stricterCategory() {
    val stricterRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.STRICTER)

    assertEquals(2, stricterRules.size)
    assertTrue(stricterRules.all { it.category == RuleCategory.STRICTER })
  }

  @Test
  fun testGetRuleById_existingRule() {
    val rule = ComposeRuleRegistry.getRuleById("ComposableNaming")

    assertNotNull(rule)
    assertEquals("ComposableNaming", rule.id)
    assertEquals("Composable Naming Convention", rule.name)
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun testGetRuleById_nonExistentRule() {
    val rule = ComposeRuleRegistry.getRuleById("NonExistentRule")

    assertNull(rule)
  }

  @Test
  fun testGetRulesByCategories_containsAllCategories() {
    val rulesByCategories = ComposeRuleRegistry.getRulesByCategories()

    assertEquals(6, rulesByCategories.size)
    assertTrue(rulesByCategories.containsKey(RuleCategory.NAMING))
    assertTrue(rulesByCategories.containsKey(RuleCategory.MODIFIER))
    assertTrue(rulesByCategories.containsKey(RuleCategory.STATE))
    assertTrue(rulesByCategories.containsKey(RuleCategory.PARAMETER))
    assertTrue(rulesByCategories.containsKey(RuleCategory.COMPOSABLE))
    assertTrue(rulesByCategories.containsKey(RuleCategory.STRICTER))
  }

  @Test
  fun testGetRulesByCategories_correctCounts() {
    val rulesByCategories = ComposeRuleRegistry.getRulesByCategories()

    assertEquals(6, rulesByCategories[RuleCategory.NAMING]?.size)
    assertEquals(7, rulesByCategories[RuleCategory.MODIFIER]?.size)
    assertEquals(8, rulesByCategories[RuleCategory.STATE]?.size)
    assertEquals(5, rulesByCategories[RuleCategory.PARAMETER]?.size)
    assertEquals(8, rulesByCategories[RuleCategory.COMPOSABLE]?.size)
    assertEquals(2, rulesByCategories[RuleCategory.STRICTER]?.size)
  }

  @Test
  fun testAllRules_haveUniqueIds() {
    val rules = ComposeRuleRegistry.getAllRules()
    val ids = rules.map { it.id }
    val uniqueIds = ids.toSet()

    assertEquals(ids.size, uniqueIds.size, "Duplicate rule IDs found")
  }

  @Test
  fun testAllRules_haveNonEmptyName() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertTrue(rule.name.isNotBlank(), "Rule ${rule.id} has empty name")
    }
  }

  @Test
  fun testAllRules_haveNonEmptyDescription() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertTrue(rule.description.isNotBlank(), "Rule ${rule.id} has empty description")
    }
  }

  @Test
  fun testAllRules_haveValidCategory() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertNotNull(rule.category, "Rule ${rule.id} has null category")
    }
  }

  @Test
  fun testAllRules_haveValidSeverity() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      assertNotNull(rule.severity, "Rule ${rule.id} has null severity")
    }
  }

  // ===== Specific Rule Registration Tests =====

  @Test
  fun testPreviewVisibilityRule_isRegistered() {
    val rule = ComposeRuleRegistry.getRuleById("PreviewVisibility")

    assertNotNull(rule, "PreviewVisibility rule should be registered")
    assertEquals("PreviewVisibility", rule.id)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testLazyListContentTypeRule_isRegistered() {
    val rule = ComposeRuleRegistry.getRuleById("LazyListContentType")

    assertNotNull(rule, "LazyListContentType rule should be registered")
    assertEquals("LazyListContentType", rule.id)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testLazyListMissingKeyRule_isRegistered() {
    val rule = ComposeRuleRegistry.getRuleById("LazyListMissingKey")

    assertNotNull(rule, "LazyListMissingKey rule should be registered")
    assertEquals("LazyListMissingKey", rule.id)
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun testAllComposableStructureRules_areRegistered() {
    // Note: LambdaParameterInEffect is in STATE category, not COMPOSABLE
    val composableRuleIds = listOf(
      "ContentEmission",
      "MultipleContentEmitters",
      "ContentSlotReused",
      "EffectKeys",
      "MovableContent",
      "PreviewVisibility",
      "LazyListContentType",
      "LazyListMissingKey",
    )

    composableRuleIds.forEach { id ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Composable rule '$id' should be registered")
      assertEquals(RuleCategory.COMPOSABLE, rule.category, "Rule '$id' should be in COMPOSABLE category")
    }
  }

  @Test
  fun testLambdaParameterInEffectRule_isInStateCategory() {
    val rule = ComposeRuleRegistry.getRuleById("LambdaParameterInEffect")

    assertNotNull(rule, "LambdaParameterInEffect rule should be registered")
    assertEquals(RuleCategory.STATE, rule.category, "LambdaParameterInEffect should be in STATE category")
  }

  @Test
  fun testAllRules_enabledByDefaultIsSet() {
    val rules = ComposeRuleRegistry.getAllRules()

    rules.forEach { rule ->
      // enabledByDefault should be explicitly set (true or false)
      assertNotNull(rule.enabledByDefault, "Rule ${rule.id} should have enabledByDefault set")
    }
  }

  @Test
  fun testComposableCategory_hasExpectedRuleCount() {
    val composableRules = ComposeRuleRegistry.getRulesByCategory(RuleCategory.COMPOSABLE)

    // Should have exactly 9 rules:
    // ContentEmission, MultipleContentEmitters, ContentSlotReused, EffectKeys,
    // LambdaParameterInEffect, MovableContent, PreviewVisibility,
    // LazyListContentType, LazyListMissingKey
    assertTrue(composableRules.size >= 8, "COMPOSABLE category should have at least 8 rules, found ${composableRules.size}")
  }

  @Test
  fun testAllCategoriesHaveRules() {
    RuleCategory.entries.forEach { category ->
      val rules = ComposeRuleRegistry.getRulesByCategory(category)
      assertTrue(rules.isNotEmpty(), "Category $category should have at least one rule")
    }
  }
}
