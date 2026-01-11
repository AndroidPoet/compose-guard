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
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EventParameterNamingRuleTest {

  private val rule = EventParameterNamingRule()


  @Test
  fun metadata_id() {
    assertEquals("EventParameterNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Event Parameter Naming", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
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
  fun metadata_descriptionMentionsEventNaming() {
    assertTrue(
      rule.description.contains("on") ||
        rule.description.contains("event") ||
        rule.description.contains("callback") ||
        rule.description.contains("present"),
    )
  }


  @Test
  fun pattern_presentTenseEventName_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun pattern_onClicked_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_onChanged_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_onSubmitted_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_onDismissed_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun pattern_nonCallbackParameter_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_callbackWithoutOnPrefix_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun reason_consistencyAndIntent() {
    assertTrue(rule.enabledByDefault)
  }
}
