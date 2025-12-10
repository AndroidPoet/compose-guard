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
package io.androidpoet.composeguard.rules.modifiers

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for modifier rules metadata (id, name, description, category, severity).
 */
class ModifierRulesMetadataTest {

  // ===== ModifierRequiredRule tests =====

  @Test
  fun testModifierRequiredRule_metadata() {
    val rule = ModifierRequiredRule()

    assertEquals("ModifierRequired", rule.id)
    assertEquals("Modifier Parameter Required", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testModifierRequiredRule_descriptionMentionsModifier() {
    val rule = ModifierRequiredRule()

    assertTrue(rule.description.contains("Modifier"))
  }

  // ===== ModifierDefaultValueRule tests =====

  @Test
  fun testModifierDefaultValueRule_metadata() {
    val rule = ModifierDefaultValueRule()

    assertEquals("ModifierDefaultValue", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testModifierDefaultValueRule_descriptionMentionsDefault() {
    val rule = ModifierDefaultValueRule()

    assertTrue(rule.description.contains("default") || rule.description.contains("Modifier"))
  }

  // ===== ModifierNamingRule tests =====

  @Test
  fun testModifierNamingRule_metadata() {
    val rule = ModifierNamingRule()

    assertEquals("ModifierNaming", rule.id)
    assertEquals("Modifier Naming Convention", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testModifierNamingRule_descriptionMentionsModifier() {
    val rule = ModifierNamingRule()

    assertTrue(rule.description.contains("modifier") || rule.description.contains("Modifier"))
  }

  // ===== ModifierTopMostRule tests =====

  @Test
  fun testModifierTopMostRule_metadata() {
    val rule = ModifierTopMostRule()

    assertEquals("ModifierTopMost", rule.id)
    assertEquals("Modifier at Top-Most Layout", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  // ===== ModifierReuseRule tests =====

  @Test
  fun testModifierReuseRule_metadata() {
    val rule = ModifierReuseRule()

    assertEquals("ModifierReuse", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
  }

  // ===== ModifierOrderRule tests =====

  @Test
  fun testModifierOrderRule_metadata() {
    val rule = ModifierOrderRule()

    assertEquals("ModifierOrder", rule.id)
    assertEquals("Modifier Order Matters", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  // ===== AvoidComposedRule tests =====

  @Test
  fun testAvoidComposedRule_metadata() {
    val rule = AvoidComposedRule()

    assertEquals("AvoidComposed", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.MODIFIER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testAvoidComposedRule_descriptionMentionsComposed() {
    val rule = AvoidComposedRule()

    assertTrue(rule.description.contains("composed") || rule.description.contains("Composed"))
  }

  // ===== All modifier rules have documentation URLs =====

  @Test
  fun testAllModifierRules_haveDocumentationUrls() {
    val rules = listOf(
      ModifierRequiredRule(),
      ModifierDefaultValueRule(),
      ModifierNamingRule(),
      ModifierTopMostRule(),
      ModifierOrderRule(),
      AvoidComposedRule(),
    )

    rules.forEach { rule ->
      assertNotNull(
        rule.documentationUrl,
        "Rule ${rule.id} should have documentation URL",
      )
    }
  }
}
