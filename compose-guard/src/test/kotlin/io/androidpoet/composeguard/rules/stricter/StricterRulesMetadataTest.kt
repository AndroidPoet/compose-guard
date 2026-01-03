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
package io.androidpoet.composeguard.rules.stricter

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for stricter rules metadata (id, name, description, category, severity).
 */
class StricterRulesMetadataTest {


  @Test
  fun testMaterial2Rule_metadata() {
    val rule = Material2Rule()

    assertEquals("Material2Usage", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STRICTER, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testMaterial2Rule_descriptionMentionsMaterial() {
    val rule = Material2Rule()

    assertTrue(rule.description.contains("Material") || rule.description.contains("material"))
  }


  @Test
  fun testUnstableCollectionsRule_metadata() {
    val rule = UnstableCollectionsRule()

    assertEquals("UnstableCollections", rule.id)
    assertEquals("Avoid Unstable Collections", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.STRICTER, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testUnstableCollectionsRule_descriptionMentionsUnstable() {
    val rule = UnstableCollectionsRule()

    assertTrue(
      rule.description.contains("Immutable") ||
        rule.description.contains("List") ||
        rule.description.contains("stable"),
    )
  }


  @Test
  fun testStricterRules_enabledByDefault() {
    val material2Rule = Material2Rule()
    val unstableRule = UnstableCollectionsRule()

    assertTrue(
      material2Rule.enabledByDefault,
      "Material2Rule should be enabled by default",
    )
    assertTrue(
      unstableRule.enabledByDefault,
      "UnstableCollectionsRule should be enabled by default",
    )
  }


  @Test
  fun testAllStricterRules_haveDocumentationUrls() {
    val rules = listOf(
      Material2Rule(),
      UnstableCollectionsRule(),
    )

    rules.forEach { rule ->
      assertNotNull(
        rule.documentationUrl,
        "Rule ${rule.id} should have documentation URL",
      )
    }
  }


  @Test
  fun testAllStricterRules_inStricterCategory() {
    val rules = listOf(
      Material2Rule(),
      UnstableCollectionsRule(),
    )

    rules.forEach { rule ->
      assertEquals(
        RuleCategory.STRICTER,
        rule.category,
        "Rule ${rule.id} should be in STRICTER category",
      )
    }
  }
}
