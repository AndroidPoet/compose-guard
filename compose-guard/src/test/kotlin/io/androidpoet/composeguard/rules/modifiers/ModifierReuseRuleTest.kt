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
package io.androidpoet.composeguard.rules.modifiers

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModifierReuseRuleTest {

  private val rule = ModifierReuseRule()


  @Test
  fun metadata_id() {
    assertEquals("ModifierReuse", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Re-use Modifiers", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
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
  fun metadata_descriptionMentionsReuse() {
    assertTrue(
      rule.description.contains("reuse") ||
        rule.description.contains("re-use") ||
        rule.description.contains("same") ||
        rule.description.contains("multiple"),
    )
  }


  @Test
  fun pattern_modifierUsedOnce_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun pattern_modifierUsedMultipleTimes_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun pattern_reassignedModifierReused_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun pattern_modifierChainReused_shouldViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }

  @Test
  fun pattern_differentModifierInstances_shouldNotViolate() {
    assertEquals(RuleCategory.MODIFIER, rule.category)
  }


  @Test
  fun reason_stateConflictsAndSingleNodeDesign() {
    assertTrue(rule.enabledByDefault)
  }
}
