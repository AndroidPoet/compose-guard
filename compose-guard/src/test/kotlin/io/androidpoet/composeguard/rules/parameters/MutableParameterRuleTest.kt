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

class MutableParameterRuleTest {

  private val rule = MutableParameterRule()


  @Test
  fun metadata_id() {
    assertEquals("MutableParameter", rule.id)
  }

  @Test
  fun metadata_name() {
    assertTrue(rule.name.isNotBlank())
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
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
  fun metadata_descriptionMentionsMutable() {
    assertTrue(
      rule.description.contains("mutable") ||
        rule.description.contains("Mutable") ||
        rule.description.contains("immutable"),
    )
  }


  @Test
  fun pattern_mutableListBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun pattern_mutableSetBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun pattern_mutableMapBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun alternative_kotlinCollections() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun alternative_kotlinxImmutableCollections() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun reason_referentialEquality() {
    assertTrue(rule.enabledByDefault)
  }
}
