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

class ComposableAnnotationNamingRuleTest {

  private val rule = ComposableAnnotationNamingRule()


  @Test
  fun metadata_id() {
    assertEquals("ComposableAnnotationNaming", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Composable Annotation Naming", rule.name)
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
  fun metadata_descriptionMentionsComposable() {
    assertTrue(
      rule.description.contains("Composable") ||
        rule.description.contains("suffix") ||
        rule.description.contains("annotation"),
    )
  }


  @Test
  fun pattern_withComposableSuffix_shouldNotViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_withoutComposableSuffix_shouldViolate() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_nonComposableAnnotation_shouldNotBeChecked() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }

  @Test
  fun pattern_previewAnnotation_specialCase() {
    assertEquals(RuleCategory.NAMING, rule.category)
  }


  @Test
  fun reason_clarityAndIntent() {
    assertTrue(rule.enabledByDefault)
  }
}
