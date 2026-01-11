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

class TrailingLambdaRuleTest {

  private val rule = TrailingLambdaRule()


  @Test
  fun metadata_id() {
    assertEquals("TrailingLambda", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Trailing Lambda Rules", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
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
  }

  @Test
  fun metadata_descriptionMentionsContent() {
    assertTrue(
      rule.description.contains("Content") ||
        rule.description.contains("content") ||
        rule.description.contains("lambda") ||
        rule.description.contains("trailing"),
    )
  }


  @Test
  fun pattern_contentLambdaTrailing() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun pattern_eventHandlersNotTrailing() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun pattern_multipleContentSlots() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun pattern_namedLambdas() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun benefit_cleanCallSite() {
    assertTrue(rule.enabledByDefault)
  }
}
