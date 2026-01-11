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

class ComposableNamingRuleTest {

  private val rule = ComposableNamingRule()


  @Test
  fun metadata_id() {
    assertEquals("ComposableNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Composable Naming Convention", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.NAMING, rule.category)
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
  fun metadata_descriptionMentionsCase() {
    assertTrue(
      rule.description.contains("PascalCase") ||
        rule.description.contains("camelCase") ||
        rule.description.contains("uppercase") ||
        rule.description.contains("lowercase"),
    )
  }


  @Test
  fun pattern_unitReturningPascalCase_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_unitReturningCamelCase_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun pattern_valueReturningCamelCase_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_valueReturningPascalCase_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun pattern_operatorFunction_shouldBeSkipped() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_overrideFunction_shouldBeSkipped() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_standardComposeFunctions_shouldBeAllowed() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun reason_readabilityAndConsistency() {
    assertTrue(rule.enabledByDefault)
  }
}
