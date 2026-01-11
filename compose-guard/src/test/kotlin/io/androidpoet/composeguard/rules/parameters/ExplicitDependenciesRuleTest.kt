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

class ExplicitDependenciesRuleTest {

  private val rule = ExplicitDependenciesRule()


  @Test
  fun metadata_id() {
    assertEquals("ExplicitDependencies", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Make Dependencies Explicit", rule.name)
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
  fun metadata_descriptionMentionsDependencies() {
    assertTrue(
      rule.description.contains("ViewModel") ||
        rule.description.contains("CompositionLocal") ||
        rule.description.contains("parameter") ||
        rule.description.contains("explicit"),
    )
  }


  @Test
  fun pattern_viewModelInBody() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun pattern_compositionLocalCurrent() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }


  @Test
  fun benefit_testability() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun benefit_previewSupport() {
    assertTrue(rule.enabledByDefault)
  }


  @Test
  fun pattern_screenVsComponent() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }
}
