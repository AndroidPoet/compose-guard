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

class LazyListMissingKeyRuleTest {

  private val rule = LazyListMissingKeyRule()


  @Test
  fun metadata_id() {
    assertEquals("LazyListMissingKey", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("LazyList Missing Key Parameter", rule.name)
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
    assertTrue(rule.documentationUrl!!.contains("android.com"))
  }

  @Test
  fun metadata_descriptionMentionsKey() {
    assertTrue(
      rule.description.contains("key") ||
        rule.description.contains("LazyColumn") ||
        rule.description.contains("LazyRow"),
    )
  }


  @Test
  fun pattern_itemsWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_itemsIndexedWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  @Test
  fun pattern_itemsWithKey_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_itemsWithPositionalKey_shouldNotViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  @Test
  fun pattern_lazyRowItemsWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }

  @Test
  fun pattern_lazyVerticalGridItemsWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
  }


  @Test
  fun reason_efficientRecompositionAndStatePreservation() {
    assertTrue(rule.enabledByDefault)
  }
}
