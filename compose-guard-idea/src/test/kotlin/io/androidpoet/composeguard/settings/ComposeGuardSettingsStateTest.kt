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

  // ===== Default Settings Tests =====

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

  // ===== isRuleEnabled Tests =====

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

    // Rule not in map, default is true -> should be enabled
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  @Test
  fun testIsRuleEnabled_withDefaultEnabledFalse() {
    val settings = ComposeGuardSettingsState()

    // Rule not in map, default is false -> should be disabled
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

    // Rule explicitly enabled, overrides default of false
    assertTrue(settings.isRuleEnabled("SomeRule", defaultEnabled = false))
  }

  @Test
  fun testIsRuleEnabled_explicitlyDisabled_overridesDefaultTrue() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("PreviewVisibility", false)

    // Rule explicitly disabled, overrides default of true
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  // ===== setRuleEnabled Tests =====

  @Test
  fun testSetRuleEnabled_storesState() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("TestRule", false)
    assertFalse(settings.isRuleEnabled("TestRule"))

    settings.setRuleEnabled("TestRule", true)
    assertTrue(settings.isRuleEnabled("TestRule"))
  }

  // ===== enableRules / disableRules Tests =====

  @Test
  fun testEnableRules_enablesMultipleRules() {
    val settings = ComposeGuardSettingsState()

    // First disable some rules
    settings.setRuleEnabled("Rule1", false)
    settings.setRuleEnabled("Rule2", false)
    settings.setRuleEnabled("Rule3", false)

    // Enable them using enableRules
    settings.enableRules(listOf("Rule1", "Rule2", "Rule3"))

    assertTrue(settings.isRuleEnabled("Rule1"))
    assertTrue(settings.isRuleEnabled("Rule2"))
    assertTrue(settings.isRuleEnabled("Rule3"))
  }

  @Test
  fun testDisableRules_disablesMultipleRules() {
    val settings = ComposeGuardSettingsState()

    // Disable rules using disableRules
    settings.disableRules(listOf("Rule1", "Rule2", "Rule3"))

    assertFalse(settings.isRuleEnabled("Rule1"))
    assertFalse(settings.isRuleEnabled("Rule2"))
    assertFalse(settings.isRuleEnabled("Rule3"))
  }

  // ===== resetRuleToDefault Tests =====

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

    // Explicitly disable a rule
    settings.setRuleEnabled("PreviewVisibility", false)
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    // Reset to default
    settings.resetRuleToDefault("PreviewVisibility")

    // Should now use the default (true)
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  // ===== resetAllRulesToDefault Tests =====

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

    // Configure multiple rules
    settings.setRuleEnabled("PreviewVisibility", false)
    settings.setRuleEnabled("LazyListContentType", false)
    settings.setRuleEnabled("LazyListMissingKey", false)

    // Verify they are disabled
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    // Reset all
    settings.resetAllRulesToDefault()

    // Should now use defaults (all true)
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }

  // ===== Display Settings Tests =====

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

  // ===== Specific Rule Tests =====

  @Test
  fun testPreviewVisibilityRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    // Default should be enabled
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    // Disable it
    settings.setRuleEnabled("PreviewVisibility", false)
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))

    // Re-enable it
    settings.setRuleEnabled("PreviewVisibility", true)
    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
  }

  @Test
  fun testLazyListContentTypeRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    // Default should be enabled
    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))

    // Disable it
    settings.setRuleEnabled("LazyListContentType", false)
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))

    // Re-enable it
    settings.setRuleEnabled("LazyListContentType", true)
    assertTrue(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
  }

  @Test
  fun testLazyListMissingKeyRule_enableDisable() {
    val settings = ComposeGuardSettingsState()

    // Default should be enabled
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    // Disable it
    settings.setRuleEnabled("LazyListMissingKey", false)
    assertFalse(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))

    // Re-enable it
    settings.setRuleEnabled("LazyListMissingKey", true)
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }

  @Test
  fun testMultipleRulesCanBeIndividuallyConfigured() {
    val settings = ComposeGuardSettingsState()

    // Configure multiple rules differently
    settings.setRuleEnabled("PreviewVisibility", true)
    settings.setRuleEnabled("LazyListContentType", false)
    settings.setRuleEnabled("LazyListMissingKey", true)

    assertTrue(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
    assertFalse(settings.isRuleEnabled("LazyListContentType", defaultEnabled = true))
    assertTrue(settings.isRuleEnabled("LazyListMissingKey", defaultEnabled = true))
  }

  // ===== ruleEnabledStates Map Tests =====

  @Test
  fun testRuleEnabledStates_persistsCorrectly() {
    val settings = ComposeGuardSettingsState()

    // Set various rules
    settings.setRuleEnabled("Rule1", true)
    settings.setRuleEnabled("Rule2", false)
    settings.setRuleEnabled("Rule3", true)

    // Verify the map contains correct values
    assertEquals(3, settings.ruleEnabledStates.size)
    assertEquals(true, settings.ruleEnabledStates["Rule1"])
    assertEquals(false, settings.ruleEnabledStates["Rule2"])
    assertEquals(true, settings.ruleEnabledStates["Rule3"])
  }

  // ===== Regression Test: Disabling one rule should NOT disable other rules =====

  @Test
  fun testDisablingOneRule_doesNotDisableOtherRules() {
    val settings = ComposeGuardSettingsState()

    // Disable just ONE rule
    settings.setRuleEnabled("ComposableNaming", false)

    // The disabled rule should be disabled
    assertFalse(settings.isRuleEnabled("ComposableNaming"))

    // But OTHER rules should still be enabled (using default)
    assertTrue(settings.isRuleEnabled("ModifierRequired"))
    assertTrue(settings.isRuleEnabled("RememberState"))
    assertTrue(settings.isRuleEnabled("PreviewVisibility"))
    assertTrue(settings.isRuleEnabled("LazyListContentType"))
  }

  @Test
  fun testDisablingMultipleRules_doesNotDisableAllRules() {
    val settings = ComposeGuardSettingsState()

    // Disable multiple rules
    settings.setRuleEnabled("ComposableNaming", false)
    settings.setRuleEnabled("ModifierRequired", false)
    settings.setRuleEnabled("RememberState", false)

    // Disabled rules should be disabled
    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertFalse(settings.isRuleEnabled("ModifierRequired"))
    assertFalse(settings.isRuleEnabled("RememberState"))

    // Other rules should still be enabled
    assertTrue(settings.isRuleEnabled("PreviewVisibility"))
    assertTrue(settings.isRuleEnabled("LazyListContentType"))
    assertTrue(settings.isRuleEnabled("ParameterOrdering"))
  }

  @Test
  fun testIndependentRuleConfiguration() {
    val settings = ComposeGuardSettingsState()

    // Configure rules in same "category" differently
    settings.setRuleEnabled("ComposableNaming", false)
    settings.setRuleEnabled("CompositionLocalNaming", true)

    // Rules should have independent states
    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertTrue(settings.isRuleEnabled("CompositionLocalNaming"))
  }
}
