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
import kotlin.test.assertTrue

class RuleDetectionTest {


  @Test
  fun testAllRulesRegistered() {
    val rules = ComposeRuleRegistry.getAllRules()
    assertEquals(36, rules.size, "Expected 36 rules to be registered")
  }

  @Test
  fun testAllRulesHaveUniqueIds() {
    val rules = ComposeRuleRegistry.getAllRules()
    val ids = rules.map { it.id }
    val uniqueIds = ids.toSet()
    assertEquals(ids.size, uniqueIds.size, "Found duplicate rule IDs: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}")
  }


  @Test
  fun testNamingRules_allPresent() {
    val expectedRules = listOf(
      "ComposableNaming" to "Composable Naming Convention",
      "CompositionLocalNaming" to "CompositionLocal Naming",
      "PreviewNaming" to "Preview Naming Convention",
      "MultipreviewNaming" to "Multipreview Naming",
      "ComposableAnnotationNaming" to "Composable Annotation Naming",
      "EventParameterNaming" to "Event Parameter Naming",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing naming rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.NAMING, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testModifierRules_allPresent() {
    val expectedRules = listOf(
      "ModifierRequired" to "Modifier Parameter Required",
      "ModifierDefaultValue" to "Modifier Should Have Default Value",
      "ModifierNaming" to "Modifier Naming Convention",
      "ModifierTopMost" to "Modifier at Top-Most Layout",
      "ModifierReuse" to "Don't Re-use Modifiers",
      "ModifierOrder" to "Modifier Order Matters",
      "AvoidComposed" to "Avoid composed {} Modifier",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing modifier rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.MODIFIER, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testStateRules_allPresent() {
    val expectedRules = listOf(
      "RememberState" to "State Should Be Remembered",
      "TypeSpecificState" to "Use Type-Specific State Variants",
      "MutableStateParameter" to "Don't Use MutableState as Parameter",
      "HoistState" to "Consider Hoisting State",
      "LambdaParameterInEffect" to "Lambda Parameters in Restartable Effects",
      "DerivedStateOfCandidate" to "Consider Using remember with keys",
      "FrequentRecomposition" to "Potential Excessive Recomposition",
      "DeferStateReads" to "Defer State Reads",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing state rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.STATE, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testParameterRules_allPresent() {
    val expectedRules = listOf(
      "ParameterOrdering" to "Parameter Ordering",
      "TrailingLambda" to "Trailing Lambda Rules",
      "MutableParameter" to "Don't Use Mutable Types as Parameters",
      "ExplicitDependencies" to "Make Dependencies Explicit",
      "ViewModelForwarding" to "Don't Forward ViewModels",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing parameter rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.PARAMETER, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testComposableRules_allPresent() {
    val expectedRules = listOf(
      "ContentEmission" to "Don't Emit Content and Return",
      "MultipleContentEmitters" to "Multiple Content Emitters",
      "ContentSlotReused" to "Content Slots Should Not Be Reused",
      "EffectKeys" to "Effect Keys Correctness",
      "MovableContent" to "Movable Content Should Be Remembered",
      "PreviewVisibility" to "Preview Should Be Private",
      "LazyListContentType" to "LazyList Missing ContentType",
      "LazyListMissingKey" to "LazyList Missing Key Parameter",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing composable rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.COMPOSABLE, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testStricterRules_allPresent() {
    val expectedRules = listOf(
      "Material2Usage" to "Don't Use Material 2",
      "UnstableCollections" to "Avoid Unstable Collections",
    )

    expectedRules.forEach { (id, name) ->
      val rule = ComposeRuleRegistry.getRuleById(id)
      assertNotNull(rule, "Missing stricter rule: $id")
      assertEquals(name, rule.name, "Wrong name for rule $id")
      assertEquals(RuleCategory.STRICTER, rule.category, "Wrong category for rule $id")
    }
  }


  @Test
  fun testAllRules_haveNonEmptyDescription() {
    ComposeRuleRegistry.getAllRules().forEach { rule ->
      assertTrue(rule.description.isNotBlank(), "Rule ${rule.id} has empty description")
    }
  }

  @Test
  fun testAllRules_haveDocumentationUrl() {
    ComposeRuleRegistry.getAllRules().forEach { rule ->
      val url = rule.documentationUrl
      if (url != null) {
        assertTrue(url.startsWith("https://"), "Rule ${rule.id} has invalid documentation URL: $url")
      }
    }
  }

  @Test
  fun testAllRules_haveValidSeverity() {
    val validSeverities = RuleSeverity.entries.toSet()
    ComposeRuleRegistry.getAllRules().forEach { rule ->
      assertTrue(rule.severity in validSeverities, "Rule ${rule.id} has invalid severity")
    }
  }


  @Test
  fun testCategoryDistribution() {
    val rulesByCategory = ComposeRuleRegistry.getRulesByCategories()

    val expectedCounts = mapOf(
      RuleCategory.NAMING to 6,
      RuleCategory.MODIFIER to 7,
      RuleCategory.STATE to 8,
      RuleCategory.PARAMETER to 5,
      RuleCategory.COMPOSABLE to 8,
      RuleCategory.STRICTER to 2,
    )

    expectedCounts.forEach { (category, expectedCount) ->
      val actualCount = rulesByCategory[category]?.size ?: 0
      assertEquals(
        expectedCount,
        actualCount,
        "Wrong count for ${category.displayName}: expected $expectedCount, got $actualCount",
      )
    }

    val total = rulesByCategory.values.sumOf { it.size }
    assertEquals(36, total, "Total rules should be 36")
  }


  @Test
  fun testRuleIds_followNamingConvention() {
    ComposeRuleRegistry.getAllRules().forEach { rule ->
      assertTrue(
        rule.id.matches(Regex("^[A-Z][a-zA-Z0-9]*$")),
        "Rule ID '${rule.id}' doesn't follow PascalCase convention",
      )
    }
  }
}
