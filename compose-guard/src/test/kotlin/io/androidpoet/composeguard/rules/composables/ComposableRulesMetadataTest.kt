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

/**
 * Tests for composable rules metadata (id, name, description, category, severity).
 */
class ComposableRulesMetadataTest {


  @Test
  fun testContentEmissionRule_metadata() {
    val rule = ContentEmissionRule()

    assertEquals("ContentEmission", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testContentEmissionRule_descriptionMentionsContent() {
    val rule = ContentEmissionRule()

    assertTrue(
      rule.description.contains("content") ||
        rule.description.contains("Content") ||
        rule.description.contains("emit"),
    )
  }


  @Test
  fun testMultipleContentRule_metadata() {
    val rule = MultipleContentRule()

    assertEquals("MultipleContentEmitters", rule.id)
    assertEquals("Multiple Content Emitters", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testMultipleContentRule_descriptionMentionsMultiple() {
    val rule = MultipleContentRule()

    assertTrue(
      rule.description.contains("emit") ||
        rule.description.contains("content") ||
        rule.description.contains("Content"),
    )
  }


  @Test
  fun testEffectKeysRule_metadata() {
    val rule = EffectKeysRule()

    assertEquals("EffectKeys", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testEffectKeysRule_descriptionMentionsEffect() {
    val rule = EffectKeysRule()

    assertTrue(
      rule.description.contains("effect") ||
        rule.description.contains("Effect") ||
        rule.description.contains("key"),
    )
  }


  @Test
  fun testMovableContentRule_metadata() {
    val rule = MovableContentRule()

    assertEquals("MovableContent", rule.id)
    assertEquals("Movable Content Should Be Remembered", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.ERROR, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testMovableContentRule_descriptionMentionsMovable() {
    val rule = MovableContentRule()

    assertTrue(
      rule.description.contains("movable") ||
        rule.description.contains("Movable") ||
        rule.description.contains("remember"),
    )
  }


  @Test
  fun testPreviewVisibilityRule_metadata() {
    val rule = PreviewVisibilityRule()

    assertEquals("PreviewVisibility", rule.id)
    assertTrue(rule.name.isNotBlank())
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testPreviewVisibilityRule_descriptionMentionsPreview() {
    val rule = PreviewVisibilityRule()

    assertTrue(
      rule.description.contains("preview") ||
        rule.description.contains("Preview") ||
        rule.description.contains("private"),
    )
  }


  @Test
  fun testLazyListContentTypeRule_metadata() {
    val rule = LazyListContentTypeRule()

    assertEquals("LazyListContentType", rule.id)
    assertEquals("LazyList Missing ContentType", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.INFO, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testLazyListContentTypeRule_descriptionMentionsContentType() {
    val rule = LazyListContentTypeRule()

    assertTrue(
      rule.description.contains("contentType") ||
        rule.description.contains("heterogeneous") ||
        rule.description.contains("LazyColumn"),
    )
  }


  @Test
  fun testLazyListMissingKeyRule_metadata() {
    val rule = LazyListMissingKeyRule()

    assertEquals("LazyListMissingKey", rule.id)
    assertEquals("LazyList Missing Key Parameter", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.COMPOSABLE, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testLazyListMissingKeyRule_descriptionMentionsKey() {
    val rule = LazyListMissingKeyRule()

    assertTrue(
      rule.description.contains("key") ||
        rule.description.contains("Key") ||
        rule.description.contains("LazyColumn"),
    )
  }


  @Test
  fun testAllComposableRules_haveDocumentationUrls() {
    val rules = listOf(
      ContentEmissionRule(),
      MultipleContentRule(),
      EffectKeysRule(),
      MovableContentRule(),
      PreviewVisibilityRule(),
      LazyListContentTypeRule(),
      LazyListMissingKeyRule(),
    )

    rules.forEach { rule ->
      assertNotNull(rule.documentationUrl, "Rule ${rule.id} should have documentation URL")
    }
  }
}
