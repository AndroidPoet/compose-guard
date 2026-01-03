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
package io.androidpoet.composeguard.rules.state

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for state rules metadata (id, name, description, category, severity).
 */
class StateRulesMetadataTest {


  @Test
  fun testRememberStateRule_metadata() {
    val rule = RememberStateRule()

    assertEquals("RememberState", rule.id)
    assertEquals("State Should Be Remembered", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.ERROR, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testRememberStateRule_descriptionMentionsRemember() {
    val rule = RememberStateRule()

    assertTrue(rule.description.contains("remember"))
  }


  @Test
  fun testTypeSpecificStateRule_metadata() {
    val rule = TypeSpecificStateRule()

    assertEquals("TypeSpecificState", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testTypeSpecificStateRule_descriptionMentionsState() {
    val rule = TypeSpecificStateRule()

    assertTrue(rule.description.contains("State") || rule.description.contains("state"))
  }


  @Test
  fun testMutableStateParameterRule_metadata() {
    val rule = MutableStateParameterRule()

    assertEquals("MutableStateParameter", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testMutableStateParameterRule_descriptionMentionsMutableState() {
    val rule = MutableStateParameterRule()

    assertTrue(rule.description.contains("MutableState") || rule.description.contains("mutable"))
  }


  @Test
  fun testHoistStateRule_metadata() {
    val rule = HoistStateRule()

    assertEquals("HoistState", rule.id)
    assertEquals("Consider Hoisting State", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STATE, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testHoistStateRule_descriptionMentionsHoist() {
    val rule = HoistStateRule()

    assertTrue(rule.description.contains("hoist") || rule.description.contains("Hoist"))
  }


  @Test
  fun testAllStateRules_haveDocumentationUrls() {
    val rules = listOf(
      RememberStateRule(),
      TypeSpecificStateRule(),
      MutableStateParameterRule(),
      HoistStateRule(),
    )

    rules.forEach { rule ->
      assertNotNull(
        rule.documentationUrl,
        "Rule ${rule.id} should have documentation URL",
      )
    }
  }
}
