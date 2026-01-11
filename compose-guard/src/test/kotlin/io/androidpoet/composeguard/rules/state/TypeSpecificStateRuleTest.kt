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

class TypeSpecificStateRuleTest {

  private val rule = TypeSpecificStateRule()


  @Test
  fun metadata_id() {
    assertEquals("TypeSpecificState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Use Type-Specific State Variants", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WARNING, rule.severity)
  }

  @Test
  fun metadata_enabledByDefault() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun metadata_documentationUrl() {
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.startsWith("https://"))
  }

  @Test
  fun metadata_descriptionMentionsTypeSpecific() {
    assertTrue(
      rule.description.contains("type-specific") ||
        rule.description.contains("mutableIntStateOf") ||
        rule.description.contains("autoboxing"),
    )
  }


  @Test
  fun pattern_mutableStateOfInt_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableIntStateOf_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateOfLong_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateOfFloat_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateOfDouble_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateOfString_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun reason_avoidAutoboxing() {
    assertTrue(rule.enabledByDefault)
  }
}
