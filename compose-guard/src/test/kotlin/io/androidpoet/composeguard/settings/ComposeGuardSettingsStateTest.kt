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
package io.androidpoet.composeguard.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ComposeGuardSettingsState configuration.
 *
 * The settings use a simple design:
 * - Each rule has its own enabled/disabled state in ruleEnabledStates map
 * - If a rule is not in the map, it uses its default enabled state
 */
class ComposeGuardSettingsStateTest {


  @Test
  fun testDefaultSettings() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.showRuleGutterIcons)
    assertTrue(settings.showRuleInlayHints)
    assertTrue(settings.showRuleViolationsInToolWindow)
    assertTrue(settings.suppressBuiltInInspections)
  }

  @Test
  fun testGetState_returnsSelf() {
    val settings = ComposeGuardSettingsState()
    assertEquals(settings, settings.getState())
  }


  @Test
  fun testIsRuleEnabled_defaultsToTrue() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isRuleEnabled("ComposableNaming"))
    assertTrue(settings.isRuleEnabled("ModifierRequired"))
    assertTrue(settings.isRuleEnabled("RememberState"))
  }

  @Test
  fun testIsRuleEnabled_withDefaultEnabledTrue() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  @Test
  fun testIsRuleEnabled_withDefaultEnabledFalse() {
    val settings = ComposeGuardSettingsState()

    assertFalse(settings.isRuleEnabled("SomeRule", defaultEnabled = false))
  }

  @Test
  fun testIsRuleEnabled_respectsPerRuleSetting() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("ComposableNaming", false)

    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertTrue(settings.isRuleEnabled("ModifierRequired"))
  }

  @Test
  fun testIsRuleEnabled_explicitlyEnabled_overridesDefaultFalse() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("SomeRule", true)

    assertTrue(settings.isRuleEnabled("SomeRule", defaultEnabled = false))
  }

  @Test
  fun testIsRuleEnabled_explicitlyDisabled_overridesDefaultTrue() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("PreviewVisibility", false)

    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }


  @Test
  fun testSetRuleEnabled_storesState() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("TestRule", false)
    assertFalse(settings.isRuleEnabled("TestRule"))

    settings.setRuleEnabled("TestRule", true)
    assertTrue(settings.isRuleEnabled("TestRule"))
  }


  @Test
  fun testEnableRules_enablesMultipleRules() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("Rule1", false)
    settings.setRuleEnabled("Rule2", false)
    settings.setRuleEnabled("Rule3", false)

    settings.enableRules(listOf("Rule1", "Rule2", "Rule3"))

    assertTrue(settings.isRuleEnabled("Rule1"))
    assertTrue(settings.isRuleEnabled("Rule2"))
    assertTrue(settings.isRuleEnabled("Rule3"))
  }

  @Test
  fun testDisableRules_disablesMultipleRules() {
    val settings = ComposeGuardSettingsState()

    settings.disableRules(listOf("Rule1", "Rule2", "Rule3"))

    assertFalse(settings.isRuleEnabled("Rule1"))
    assertFalse(settings.isRuleEnabled("Rule2"))
    assertFalse(settings.isRuleEnabled("Rule3"))
  }


  @Test
  fun testResetRuleToDefault_removesPerRuleSetting() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("TestRule", false)
    assertFalse(settings.isRuleEnabled("TestRule"))

    settings.resetRuleToDefault("TestRule")
    assertTrue(settings.isRuleEnabled("TestRule"))
  }

  @Test
  fun testResetRuleToDefault_restoresDefaultBehavior() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("PreviewVisibility", false)
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    settings.resetRuleToDefault("PreviewVisibility")

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }


  @Test
  fun testResetAllRulesToDefault_clearsAllPerRuleSettings() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("Rule1", false)
    settings.setRuleEnabled("Rule2", false)
    settings.setRuleEnabled("Rule3", false)

    settings.resetAllRulesToDefault()

    assertTrue(settings.isRuleEnabled("Rule1"))
    assertTrue(settings.isRuleEnabled("Rule2"))
    assertTrue(settings.isRuleEnabled("Rule3"))
    assertTrue(settings.ruleEnabledStates.isEmpty())
  }

  @Test
  fun testResetAllRules_clearsAllCustomSettings() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("PreviewVisibility", false)
    settings.setRuleEnabled("LazyListContentType", false)
    settings.setRuleEnabled("LazyListMissingKey", false)

    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    settings.resetAllRulesToDefault()

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }


  @Test
  fun testToggleSettingsCanBeModified() {
    val settings = ComposeGuardSettingsState()

    settings.showRuleGutterIcons = false
    settings.showRuleInlayHints = false
    settings.showRuleViolationsInToolWindow = false

    assertFalse(settings.showRuleGutterIcons)
    assertFalse(settings.showRuleInlayHints)
    assertFalse(settings.showRuleViolationsInToolWindow)
  }

  @Test
  fun testSuppressBuiltInInspections_defaultTrue() {
    val settings = ComposeGuardSettingsState()
    assertTrue(settings.suppressBuiltInInspections)
  }

  @Test
  fun testSuppressBuiltInInspections_canBeToggled() {
    val settings = ComposeGuardSettingsState()

    settings.suppressBuiltInInspections = false
    assertFalse(settings.suppressBuiltInInspections)

    settings.suppressBuiltInInspections = true
    assertTrue(settings.suppressBuiltInInspections)
  }


  @Test
  fun testPreviewVisibilityRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    settings.setRuleEnabled("PreviewVisibility", false)
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    settings.setRuleEnabled("PreviewVisibility", true)
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  @Test
  fun testLazyListContentTypeRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))

    settings.setRuleEnabled("LazyListContentType", false)
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))

    settings.setRuleEnabled("LazyListContentType", true)
    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
  }

  @Test
  fun testLazyListMissingKeyRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    settings.setRuleEnabled("LazyListMissingKey", false)
    assertFalse(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    settings.setRuleEnabled("LazyListMissingKey", true)
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }

  @Test
  fun testMultipleRulesCanBeIndividuallyConfigured() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("PreviewVisibility", true)
    settings.setRuleEnabled("LazyListContentType", false)
    settings.setRuleEnabled("LazyListMissingKey", true)

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }


  @Test
  fun testRuleEnabledStates_persistsCorrectly() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("Rule1", true)
    settings.setRuleEnabled("Rule2", false)
    settings.setRuleEnabled("Rule3", true)

    assertEquals(3, settings.ruleEnabledStates.size)
    assertEquals(true, settings.ruleEnabledStates["Rule1"])
    assertEquals(false, settings.ruleEnabledStates["Rule2"])
    assertEquals(true, settings.ruleEnabledStates["Rule3"])
  }


  @Test
  fun testDisablingOneRule_doesNotDisableOtherRules() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("ComposableNaming", false)

    assertFalse(settings.isRuleEnabled("ComposableNaming"))

    assertTrue(settings.isRuleEnabled("ModifierRequired"))
    assertTrue(settings.isRuleEnabled("RememberState"))
    assertTrue(settings.isRuleEnabled("PreviewVisibility"))
    assertTrue(settings.isRuleEnabled("LazyListContentType"))
  }

  @Test
  fun testDisablingMultipleRules_doesNotDisableAllRules() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("ComposableNaming", false)
    settings.setRuleEnabled("ModifierRequired", false)
    settings.setRuleEnabled("RememberState", false)

    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertFalse(settings.isRuleEnabled("ModifierRequired"))
    assertFalse(settings.isRuleEnabled("RememberState"))

    assertTrue(settings.isRuleEnabled("PreviewVisibility"))
    assertTrue(settings.isRuleEnabled("LazyListContentType"))
    assertTrue(settings.isRuleEnabled("ParameterOrdering"))
  }

  @Test
  fun testIndependentRuleConfiguration() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("ComposableNaming", false)
    settings.setRuleEnabled("CompositionLocalNaming", true)

    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertTrue(settings.isRuleEnabled("CompositionLocalNaming"))
  }
}
