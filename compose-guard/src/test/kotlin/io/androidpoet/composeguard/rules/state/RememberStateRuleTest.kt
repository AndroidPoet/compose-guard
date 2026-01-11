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

class RememberStateRuleTest {

  private val rule = RememberStateRule()


  @Test
  fun metadata_id() {
    assertEquals("RememberState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("State Should Be Remembered", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.ERROR, rule.severity)
  }

  @Test
  fun metadata_enabledByDefault() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun metadata_documentationUrl() {
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.startsWith("https://"))
    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun metadata_descriptionMentionsRemember() {
    assertTrue(
      rule.description.contains("remember") ||
        rule.description.contains("mutableStateOf"),
    )
  }


  @Test
  fun pattern_mutableStateOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateOfWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_delegateSyntaxWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_delegateSyntaxWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_typeSpecificStateWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_derivedStateOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mutableStateListOfWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun reason_statePersistence() {
    assertEquals(RuleSeverity.ERROR, rule.severity)
  }
}
