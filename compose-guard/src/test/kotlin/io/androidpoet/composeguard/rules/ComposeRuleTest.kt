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

/**
 * Tests for ComposeRule enums and data classes.
 */
class ComposeRuleTest {


  @Test
  fun testRuleCategory_naming() {
    assertEquals("Naming Conventions", RuleCategory.NAMING.displayName)
  }

  @Test
  fun testRuleCategory_modifier() {
    assertEquals("Modifier Rules", RuleCategory.MODIFIER.displayName)
  }

  @Test
  fun testRuleCategory_state() {
    assertEquals("State Management", RuleCategory.STATE.displayName)
  }

  @Test
  fun testRuleCategory_parameter() {
    assertEquals("Parameter Rules", RuleCategory.PARAMETER.displayName)
  }

  @Test
  fun testRuleCategory_composable() {
    assertEquals("Composable Structure", RuleCategory.COMPOSABLE.displayName)
  }

  @Test
  fun testRuleCategory_stricter() {
    assertEquals("Stricter Rules", RuleCategory.STRICTER.displayName)
  }

  @Test
  fun testRuleCategory_allValues() {
    val categories = RuleCategory.entries

    assertEquals(6, categories.size)
    assertEquals(RuleCategory.NAMING, categories[0])
    assertEquals(RuleCategory.MODIFIER, categories[1])
    assertEquals(RuleCategory.STATE, categories[2])
    assertEquals(RuleCategory.PARAMETER, categories[3])
    assertEquals(RuleCategory.COMPOSABLE, categories[4])
    assertEquals(RuleCategory.STRICTER, categories[5])
  }


  @Test
  fun testRuleSeverity_allValues() {
    val severities = RuleSeverity.entries

    assertEquals(4, severities.size)
    assertEquals(RuleSeverity.ERROR, severities[0])
    assertEquals(RuleSeverity.WARNING, severities[1])
    assertEquals(RuleSeverity.WEAK_WARNING, severities[2])
    assertEquals(RuleSeverity.INFO, severities[3])
  }
}
