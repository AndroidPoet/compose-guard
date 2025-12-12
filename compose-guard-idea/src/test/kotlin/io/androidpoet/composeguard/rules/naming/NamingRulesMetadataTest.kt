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

/**
 * Tests for naming rules metadata (id, name, description, category, severity).
 */
class NamingRulesMetadataTest {

  // ===== ComposableNamingRule tests =====

  @Test
  fun testComposableNamingRule_metadata() {
    val rule = ComposableNamingRule()

    assertEquals("ComposableNaming", rule.id)
    assertEquals("Composable Naming Convention", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testComposableNamingRule_documentationUrl() {
    val rule = ComposableNamingRule()

    assertTrue(rule.documentationUrl!!.contains("mrmans0n.github.io/compose-rules"))
    assertTrue(rule.documentationUrl!!.contains("naming"))
  }

  // ===== CompositionLocalNamingRule tests =====

  @Test
  fun testCompositionLocalNamingRule_metadata() {
    val rule = CompositionLocalNamingRule()

    assertEquals("CompositionLocalNaming", rule.id)
    assertEquals("CompositionLocal Naming", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testCompositionLocalNamingRule_descriptionMentionsLocal() {
    val rule = CompositionLocalNamingRule()

    assertTrue(rule.description.contains("Local"))
  }

  // ===== PreviewNamingRule tests =====

  @Test
  fun testPreviewNamingRule_metadata() {
    val rule = PreviewNamingRule()

    assertEquals("PreviewNaming", rule.id)
    assertEquals("Preview Naming Convention", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testPreviewNamingRule_descriptionMentionsPreview() {
    val rule = PreviewNamingRule()

    assertTrue(rule.description.contains("Preview"))
  }

  // ===== MultipreviewNamingRule tests =====

  @Test
  fun testMultipreviewNamingRule_metadata() {
    val rule = MultipreviewNamingRule()

    assertEquals("MultipreviewNaming", rule.id)
    assertEquals("Multipreview Naming", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  // ===== ComposableAnnotationNamingRule tests =====

  @Test
  fun testComposableAnnotationNamingRule_metadata() {
    val rule = ComposableAnnotationNamingRule()

    assertEquals("ComposableAnnotationNaming", rule.id)
    assertEquals("Composable Annotation Naming", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  // ===== EventParameterNamingRule tests =====

  @Test
  fun testEventParameterNamingRule_metadata() {
    val rule = EventParameterNamingRule()

    assertEquals("EventParameterNaming", rule.id)
    assertEquals("Event Parameter Naming", rule.name)
    assertTrue(rule.description.isNotBlank())
    assertEquals(RuleCategory.NAMING, rule.category)
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
    assertTrue(rule.enabledByDefault)
    assertNotNull(rule.documentationUrl)
  }

  @Test
  fun testEventParameterNamingRule_descriptionMentionsOn() {
    val rule = EventParameterNamingRule()

    assertTrue(rule.description.contains("on"))
  }

  @Test
  fun testEventParameterNamingRule_pastTenseConversion() {
    val rule = EventParameterNamingRule()

    // Use reflection to test the private function
    val method = rule.javaClass.getDeclaredMethod("convertPastTenseToPresent", String::class.java)
    method.isAccessible = true

    // Test doubled consonant cases
    assertEquals("onSubmit", method.invoke(rule, "onSubmitted"))
    assertEquals("onStop", method.invoke(rule, "onStopped"))
    assertEquals("onDrop", method.invoke(rule, "onDropped"))
    assertEquals("onSkip", method.invoke(rule, "onSkipped"))

    // Test simple -ed removal
    assertEquals("onClick", method.invoke(rule, "onClicked"))
    assertEquals("onStart", method.invoke(rule, "onStarted"))
    assertEquals("onSelect", method.invoke(rule, "onSelected"))
    assertEquals("onLoad", method.invoke(rule, "onLoaded"))

    // Test words ending in 'e' (only 'd' was added)
    assertEquals("onChange", method.invoke(rule, "onChanged"))
    assertEquals("onClose", method.invoke(rule, "onClosed"))
    assertEquals("onSave", method.invoke(rule, "onSaved"))
    assertEquals("onComplete", method.invoke(rule, "onCompleted"))
  }
}
