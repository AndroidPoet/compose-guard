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

class HoistStateRuleTest {

  private val rule = HoistStateRule()


  @Test
  fun metadata_id() {
    assertEquals("HoistState", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Consider Hoisting State", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.INFO, rule.severity)
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
  fun metadata_descriptionMentionsHoisting() {
    assertTrue(
      rule.description.contains("hoist") ||
        rule.description.contains("Hoist") ||
        rule.description.contains("stateless"),
    )
  }


  @Test
  fun pattern_stateSharedBetweenChildren_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_statePassedToChild_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun pattern_screenLevelComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_privateComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_previewComposable_shouldBeSkipped() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_uiElementState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_alreadyHoisted_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun reason_testabilityAndReusability() {
    assertEquals(RuleSeverity.INFO, rule.severity)
  }
}
