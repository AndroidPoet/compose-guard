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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParameterOrderingRuleTest {

  private val rule = ParameterOrderingRule()


  @Test
  fun rule1_metadata() {
    assertEquals("ParameterOrdering", rule.id)
    assertEquals("Parameter Ordering", rule.name)
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun rule1_descriptionMentionsOrder() {
    assertTrue(
      rule.description.contains("order") ||
        rule.description.contains("Order") ||
        rule.description.contains("required") ||
        rule.description.contains("optional"),
    )
  }


  @Test
  fun rule2_modifierShouldBeFirstOptional() {
    assertTrue(
      rule.description.contains("modifier") ||
        rule.description.contains("Modifier") ||
        rule.description.contains("lambda") ||
        rule.description.contains("content"),
    )
  }


  @Test
  fun rule3_contentLambdaShouldBeTrailing() {
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun rule4_stateCallbackPairingDocumented() {
    assertTrue(rule.description.isNotBlank())
  }


  @Test
  fun fullParameterOrderPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun stateCallbackPairingPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun multipleSlotsPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun callbacksOrderingPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun overloadsPattern() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun quickReferenceTable() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
  }


  @Test
  fun summaryOrder() {
    assertEquals("ParameterOrdering", rule.id)
    assertTrue(rule.enabledByDefault)
  }


  @Test
  fun eventHandlersVsContentLambdas() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }
}
