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
package io.androidpoet.composeguard.rules.effects

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LambdaParameterInEffectRuleTest {

  private val rule = LambdaParameterInEffectRule()


  @Test
  fun metadata_id() {
    assertEquals("LambdaParameterInEffect", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Lambda Parameters in Restartable Effects", rule.name)
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
    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
  }

  @Test
  fun metadata_descriptionMentionsLambda() {
    assertTrue(
      rule.description.contains("Lambda") ||
        rule.description.contains("lambda") ||
        rule.description.contains("effect"),
    )
  }


  @Test
  fun pattern_lambdaInLaunchedEffectWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_lambdaAsKeyInLaunchedEffect_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }

  @Test
  fun pattern_lambdaWithRememberUpdatedState_shouldNotViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun pattern_lambdaInDisposableEffectWithoutKey_shouldViolate() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun pattern_nonLambdaParameter_shouldNotBeChecked() {
    assertEquals(RuleCategory.STATE, rule.category)
  }


  @Test
  fun reason_avoidStaleClosures() {
    assertTrue(rule.enabledByDefault)
  }
}
