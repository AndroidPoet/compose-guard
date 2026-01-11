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

class DerivedStateOfCandidateRuleTest {

  private val rule = DerivedStateOfCandidateRule()


  @Test
  fun metadata_id() {
    assertEquals("DerivedStateOfCandidate", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Consider Using remember with keys", rule.name)
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
    assertTrue(rule.documentationUrl!!.contains("android.com"))
  }

  @Test
  fun metadata_descriptionMentionsDerivedStateOf() {
    assertTrue(
      rule.description.contains("derivedStateOf") ||
        rule.description.contains("remember") ||
        rule.description.contains("input"),
    )
  }


  @Test
  fun pattern_scrollStateThreshold_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_scrollStateWithDerivedStateOf_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun pattern_filterWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_mapWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_sortedByWithoutRemember_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_expensiveOperationWithRemember_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun reason_scrollPerformance() {
    assertTrue(rule.enabledByDefault)
  }
}
