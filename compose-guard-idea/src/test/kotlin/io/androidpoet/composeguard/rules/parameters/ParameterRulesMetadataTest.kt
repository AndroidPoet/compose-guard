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

/**
 * Tests for parameter rules metadata (id, name, description, category, severity).
 */
class ParameterRulesMetadataTest {

  // ===== ParameterOrderingRule tests =====

  @Test
  fun testParameterOrderingRule_metadata() {
    val rule = ParameterOrderingRule()

    assertEquals("ParameterOrdering", rule.id)
    assertEquals("Parameter Ordering", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testParameterOrderingRule_descriptionMentionsOrder() {
    val rule = ParameterOrderingRule()

    assertTrue(
      rule.description.contains("order") ||
        rule.description.contains("Order") ||
        rule.description.contains("required") ||
        rule.description.contains("modifier") ||
        rule.description.contains("guidelines"),
    )
  }

  // ===== TrailingLambdaRule tests =====

  @Test
  fun testTrailingLambdaRule_metadata() {
    val rule = TrailingLambdaRule()

    assertEquals("TrailingLambda", rule.id)
    assertEquals("Trailing Lambda Rules", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testTrailingLambdaRule_descriptionMentionsLambda() {
    val rule = TrailingLambdaRule()

    assertTrue(
      rule.description.contains("lambda") ||
        rule.description.contains("Lambda") ||
        rule.description.contains("Content") ||
        rule.description.contains("trailing"),
    )
  }

  // ===== MutableParameterRule tests =====

  @Test
  fun testMutableParameterRule_metadata() {
    val rule = MutableParameterRule()

    assertEquals("MutableParameter", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testMutableParameterRule_descriptionMentionsMutable() {
    val rule = MutableParameterRule()

    assertTrue(rule.description.contains("mutable") || rule.description.contains("Mutable"))
  }

  // ===== ExplicitDependenciesRule tests =====

  @Test
  fun testExplicitDependenciesRule_metadata() {
    val rule = ExplicitDependenciesRule()

    assertEquals("ExplicitDependencies", rule.id)
    assertEquals("Make Dependencies Explicit", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.PARAMETER, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testExplicitDependenciesRule_descriptionMentionsDependencies() {
    val rule = ExplicitDependenciesRule()

    assertTrue(
      rule.description.contains("ViewModel") ||
        rule.description.contains("CompositionLocal") ||
        rule.description.contains("parameter"),
    )
  }

  // ===== All parameter rules have documentation URLs =====

  @Test
  fun testAllParameterRules_haveDocumentationUrls() {
    val rules = listOf(
      ParameterOrderingRule(),
      TrailingLambdaRule(),
      MutableParameterRule(),
      ExplicitDependenciesRule(),
    )

    rules.forEach { rule ->
      assertNotNull(
        rule.documentationUrl,
        "Rule ${rule.id} should have documentation URL",
      )
    }
  }
}
