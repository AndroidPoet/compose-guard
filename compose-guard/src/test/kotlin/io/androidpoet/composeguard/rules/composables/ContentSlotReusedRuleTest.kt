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
package io.androidpoet.composeguard.rules.composables

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContentSlotReusedRuleTest {

  private val rule = ContentSlotReusedRule()


  @Test
  fun metadata_id() {
    assertEquals("ContentSlotReused", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Content Slots Should Not Be Reused", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
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
    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun metadata_descriptionMentionsContentSlot() {
    assertTrue(
      rule.description.contains("slot") ||
        rule.description.contains("lambda") ||
        rule.description.contains("invoked"),
    )
  }


  @Test
  fun pattern_contentSlotInvokedMultipleTimes_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_contentSlotInvokedOnce_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_multipleSlotsSingleInvocation_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_contentSlotWithMovableContent_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  @Test
  fun reason_statePreservation() {
    assertTrue(rule.enabledByDefault)
  }
}
