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

import io.androidpoet.composeguard.rules.RuleCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ComposeGuardSettingsState configuration.
 */
class ComposeGuardSettingsStateTest {

  @Test
  fun testDefaultSettings() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isComposeRulesEnabled)
    assertTrue(settings.showRuleGutterIcons)
    assertTrue(settings.showRuleInlayHints)
    assertTrue(settings.showRuleViolationsInToolWindow)
  }

  @Test
  fun testDefaultCategorySettings() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.enableNamingRules)
    assertTrue(settings.enableModifierRules)
    assertTrue(settings.enableStateRules)
    assertTrue(settings.enableParameterRules)
    assertTrue(settings.enableComposableRules)
    assertTrue(settings.enableStricterRules)
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
  fun testIsRuleEnabled_returnsFalseWhenMasterSwitchDisabled() {
    val settings = ComposeGuardSettingsState()
    settings.isComposeRulesEnabled = false

    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertFalse(settings.isRuleEnabled("ModifierRequired"))
  }

  @Test
  fun testIsRuleEnabled_respectsPerRuleSetting() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("ComposableNaming", false)

    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertTrue(settings.isRuleEnabled("ModifierRequired"))
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
  fun testResetRuleToDefault_removesPerRuleSetting() {
    val settings = ComposeGuardSettingsState()

    settings.setRuleEnabled("TestRule", false)
    assertFalse(settings.isRuleEnabled("TestRule"))

    settings.resetRuleToDefault("TestRule")
    assertTrue(settings.isRuleEnabled("TestRule"))
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
  fun testEnableCategory_enablesNaming() {
    val settings = ComposeGuardSettingsState()
    settings.enableNamingRules = false

    settings.enableCategory("NAMING")

    assertTrue(settings.enableNamingRules)
  }

  @Test
  fun testEnableCategory_enablesModifier() {
    val settings = ComposeGuardSettingsState()
    settings.enableModifierRules = false

    settings.enableCategory("MODIFIER")

    assertTrue(settings.enableModifierRules)
  }

  @Test
  fun testEnableCategory_enablesState() {
    val settings = ComposeGuardSettingsState()
    settings.enableStateRules = false

    settings.enableCategory("STATE")

    assertTrue(settings.enableStateRules)
  }

  @Test
  fun testEnableCategory_enablesParameter() {
    val settings = ComposeGuardSettingsState()
    settings.enableParameterRules = false

    settings.enableCategory("PARAMETER")

    assertTrue(settings.enableParameterRules)
  }

  @Test
  fun testEnableCategory_enablesComposable() {
    val settings = ComposeGuardSettingsState()
    settings.enableComposableRules = false

    settings.enableCategory("COMPOSABLE")

    assertTrue(settings.enableComposableRules)
  }

  @Test
  fun testEnableCategory_enablesStricter() {
    val settings = ComposeGuardSettingsState()
    settings.enableStricterRules = false

    settings.enableCategory("STRICTER")

    assertTrue(settings.enableStricterRules)
  }

  @Test
  fun testDisableCategory_disablesNaming() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("NAMING")

    assertFalse(settings.enableNamingRules)
  }

  @Test
  fun testDisableCategory_disablesModifier() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("MODIFIER")

    assertFalse(settings.enableModifierRules)
  }

  @Test
  fun testDisableCategory_disablesState() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("STATE")

    assertFalse(settings.enableStateRules)
  }

  @Test
  fun testDisableCategory_disablesParameter() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("PARAMETER")

    assertFalse(settings.enableParameterRules)
  }

  @Test
  fun testDisableCategory_disablesComposable() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("COMPOSABLE")

    assertFalse(settings.enableComposableRules)
  }

  @Test
  fun testDisableCategory_disablesStricter() {
    val settings = ComposeGuardSettingsState()

    settings.disableCategory("STRICTER")

    assertFalse(settings.enableStricterRules)
  }

  @Test
  fun testEnableCategory_unknownCategory_noOp() {
    val settings = ComposeGuardSettingsState()
    val originalNaming = settings.enableNamingRules

    settings.enableCategory("UNKNOWN")

    assertEquals(originalNaming, settings.enableNamingRules)
  }

  @Test
  fun testDisableCategory_unknownCategory_noOp() {
    val settings = ComposeGuardSettingsState()
    val originalNaming = settings.enableNamingRules

    settings.disableCategory("UNKNOWN")

    assertEquals(originalNaming, settings.enableNamingRules)
  }

  @Test
  fun testToggleSettingsCanBeModified() {
    val settings = ComposeGuardSettingsState()

    settings.isComposeRulesEnabled = false
    settings.showRuleGutterIcons = false
    settings.showRuleInlayHints = false
    settings.showRuleViolationsInToolWindow = false

    assertFalse(settings.isComposeRulesEnabled)
    assertFalse(settings.showRuleGutterIcons)
    assertFalse(settings.showRuleInlayHints)
    assertFalse(settings.showRuleViolationsInToolWindow)
  }

  @Test
  fun testCategorySettingsCanBeModified() {
    val settings = ComposeGuardSettingsState()

    settings.enableNamingRules = false
    settings.enableModifierRules = false
    settings.enableStateRules = false
    settings.enableParameterRules = false
    settings.enableComposableRules = false
    settings.enableStricterRules = false

    assertFalse(settings.enableNamingRules)
    assertFalse(settings.enableModifierRules)
    assertFalse(settings.enableStateRules)
    assertFalse(settings.enableParameterRules)
    assertFalse(settings.enableComposableRules)
    assertFalse(settings.enableStricterRules)
  }

  // ===== Individual Rule Enable/Disable Tests =====

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

  // ===== Specific Rule Tests (PreviewVisibility, LazyList rules) =====

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

  // ===== Category and Rule Interaction Tests =====

  @Test
  fun testComposableCategory_enableDisable() {
    val settings = ComposeGuardSettingsState()

    // Default should be enabled
    assertTrue(settings.enableComposableRules)

    // Disable the category
    settings.disableCategory("COMPOSABLE")
    assertFalse(settings.enableComposableRules)

    // Re-enable the category
    settings.enableCategory("COMPOSABLE")
    assertTrue(settings.enableComposableRules)
  }

  @Test
  fun testRuleEnabled_butMasterSwitchDisabled_returnsFalse() {
    val settings = ComposeGuardSettingsState()

    // Enable a specific rule
    settings.setRuleEnabled("PreviewVisibility", true)

    // Disable master switch
    settings.isComposeRulesEnabled = false

    // Rule should return false even though it's explicitly enabled
    assertFalse(settings.isRuleEnabled("PreviewVisibility", defaultEnabled = true))
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

  // ===== All Categories Enable/Disable Tests =====

  @Test
  fun testAllCategories_canBeDisabledAndReEnabled() {
    val settings = ComposeGuardSettingsState()

    // Disable all categories
    settings.disableCategory("NAMING")
    settings.disableCategory("MODIFIER")
    settings.disableCategory("STATE")
    settings.disableCategory("PARAMETER")
    settings.disableCategory("COMPOSABLE")
    settings.disableCategory("STRICTER")
    settings.disableCategory("EXPERIMENTAL")

    assertFalse(settings.enableNamingRules)
    assertFalse(settings.enableModifierRules)
    assertFalse(settings.enableStateRules)
    assertFalse(settings.enableParameterRules)
    assertFalse(settings.enableComposableRules)
    assertFalse(settings.enableStricterRules)
    assertFalse(settings.enableExperimentalRules)

    // Re-enable all categories
    settings.enableCategory("NAMING")
    settings.enableCategory("MODIFIER")
    settings.enableCategory("STATE")
    settings.enableCategory("PARAMETER")
    settings.enableCategory("COMPOSABLE")
    settings.enableCategory("STRICTER")
    settings.enableCategory("EXPERIMENTAL")

    assertTrue(settings.enableNamingRules)
    assertTrue(settings.enableModifierRules)
    assertTrue(settings.enableStateRules)
    assertTrue(settings.enableParameterRules)
    assertTrue(settings.enableComposableRules)
    assertTrue(settings.enableStricterRules)
    assertTrue(settings.enableExperimentalRules)
  }

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

  // ===== isCategoryEnabled Tests =====

  @Test
  fun testIsCategoryEnabled_namingCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.NAMING))

    settings.enableNamingRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.NAMING))
  }

  @Test
  fun testIsCategoryEnabled_modifierCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.MODIFIER))

    settings.enableModifierRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.MODIFIER))
  }

  @Test
  fun testIsCategoryEnabled_stateCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.STATE))

    settings.enableStateRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.STATE))
  }

  @Test
  fun testIsCategoryEnabled_parameterCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.PARAMETER))

    settings.enableParameterRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.PARAMETER))
  }

  @Test
  fun testIsCategoryEnabled_composableCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.COMPOSABLE))

    settings.enableComposableRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.COMPOSABLE))
  }

  @Test
  fun testIsCategoryEnabled_stricterCategory() {
    val settings = ComposeGuardSettingsState()

    assertTrue(settings.isCategoryEnabled(RuleCategory.STRICTER))

    settings.enableStricterRules = false
    assertFalse(settings.isCategoryEnabled(RuleCategory.STRICTER))
  }

  // ===== isRuleEffectivelyEnabled Tests =====

  @Test
  fun testIsRuleEffectivelyEnabled_allEnabled() {
    val settings = ComposeGuardSettingsState()

    // All enabled: master=true, category=true, rule=true (default)
    assertTrue(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_masterDisabled() {
    val settings = ComposeGuardSettingsState()
    settings.isComposeRulesEnabled = false

    // Master disabled -> rule is disabled even if category and rule are enabled
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_categoryDisabled() {
    val settings = ComposeGuardSettingsState()
    settings.enableNamingRules = false

    // Category disabled -> rule is disabled even if master and rule are enabled
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_ruleDisabled() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("ComposableNaming", false)

    // Rule disabled -> rule is disabled even if master and category are enabled
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_ruleEnabled_categoryDisabled() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("ComposableNaming", true) // Explicitly enable rule
    settings.enableNamingRules = false // Disable category

    // Category disabled takes precedence over individual rule setting
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_hierarchy_masterTakesPrecedence() {
    val settings = ComposeGuardSettingsState()
    settings.isComposeRulesEnabled = false
    settings.enableNamingRules = true
    settings.setRuleEnabled("ComposableNaming", true)

    // Master switch takes precedence over everything
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_hierarchy_categoryTakesPrecedence() {
    val settings = ComposeGuardSettingsState()
    settings.isComposeRulesEnabled = true
    settings.enableNamingRules = false
    settings.setRuleEnabled("ComposableNaming", true)

    // Category takes precedence over individual rule
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_differentCategories() {
    val settings = ComposeGuardSettingsState()
    settings.enableNamingRules = false
    settings.enableModifierRules = true

    // Naming category disabled, but Modifier category enabled
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
    assertTrue(settings.isRuleEffectivelyEnabled("ModifierRequired", RuleCategory.MODIFIER))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_individualRulesInSameCategory() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("ComposableNaming", false)
    settings.setRuleEnabled("CompositionLocalNaming", true)

    // Category enabled, individual rules can have different states
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
    assertTrue(settings.isRuleEffectivelyEnabled("CompositionLocalNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_reEnablingCategory_preservesRuleState() {
    val settings = ComposeGuardSettingsState()

    // Disable a specific rule
    settings.setRuleEnabled("ComposableNaming", false)
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))

    // Disable then re-enable category
    settings.enableNamingRules = false
    settings.enableNamingRules = true

    // Individual rule state should be preserved (still disabled)
    assertFalse(settings.isRuleEffectivelyEnabled("ComposableNaming", RuleCategory.NAMING))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_withDefaultEnabledFalse() {
    val settings = ComposeGuardSettingsState()

    // Rule with default=false should be disabled by default
    assertFalse(settings.isRuleEffectivelyEnabled("SomeRule", RuleCategory.NAMING, defaultEnabled = false))
  }

  @Test
  fun testIsRuleEffectivelyEnabled_explicitlyEnableRuleWithDefaultFalse() {
    val settings = ComposeGuardSettingsState()
    settings.setRuleEnabled("SomeRule", true)

    // Explicitly enabled rule should be enabled even if default is false
    assertTrue(settings.isRuleEffectivelyEnabled("SomeRule", RuleCategory.NAMING, defaultEnabled = false))
  }

  // ===== Parent-Child Hierarchy Behavior Tests =====

  @Test
  fun testCategoryDisabled_allRulesInCategoryDisabled() {
    val settings = ComposeGuardSettingsState()

    // Enable all rules individually
    settings.setRuleEnabled("PreviewVisibility", true)
    settings.setRuleEnabled("LazyListContentType", true)
    settings.setRuleEnabled("LazyListMissingKey", true)

    // Disable the category
    settings.enableComposableRules = false

    // All rules in category should be effectively disabled
    assertFalse(settings.isRuleEffectivelyEnabled("PreviewVisibility", RuleCategory.COMPOSABLE))
    assertFalse(settings.isRuleEffectivelyEnabled("LazyListContentType", RuleCategory.COMPOSABLE))
    assertFalse(settings.isRuleEffectivelyEnabled("LazyListMissingKey", RuleCategory.COMPOSABLE))
  }

  @Test
  fun testCategoryEnabled_individualRulesCanBeConfigured() {
    val settings = ComposeGuardSettingsState()

    // Category enabled
    assertTrue(settings.enableComposableRules)

    // Configure individual rules
    settings.setRuleEnabled("PreviewVisibility", true)
    settings.setRuleEnabled("LazyListContentType", false)
    settings.setRuleEnabled("LazyListMissingKey", true)

    // Individual settings are respected
    assertTrue(settings.isRuleEffectivelyEnabled("PreviewVisibility", RuleCategory.COMPOSABLE))
    assertFalse(settings.isRuleEffectivelyEnabled("LazyListContentType", RuleCategory.COMPOSABLE))
    assertTrue(settings.isRuleEffectivelyEnabled("LazyListMissingKey", RuleCategory.COMPOSABLE))
  }

  @Test
  fun testReEnableCategory_preservesIndividualRuleSettings() {
    val settings = ComposeGuardSettingsState()

    // Configure individual rules
    settings.setRuleEnabled("PreviewVisibility", false)
    settings.setRuleEnabled("LazyListContentType", true)

    // Disable category
    settings.enableComposableRules = false

    // Re-enable category
    settings.enableComposableRules = true

    // Individual settings should be preserved
    assertFalse(settings.isRuleEffectivelyEnabled("PreviewVisibility", RuleCategory.COMPOSABLE))
    assertTrue(settings.isRuleEffectivelyEnabled("LazyListContentType", RuleCategory.COMPOSABLE))
  }

  // ===== Regression Test: Disabling one rule should NOT disable all rules =====

  @Test
  fun testDisablingOneRule_doesNotDisableOtherRules() {
    val settings = ComposeGuardSettingsState()

    // Master switch should stay enabled
    assertTrue(settings.isComposeRulesEnabled)

    // Disable just ONE rule
    settings.setRuleEnabled("ComposableNaming", false)

    // Master switch should STILL be enabled (this was the bug - it was being set to false)
    assertTrue(settings.isComposeRulesEnabled)

    // The disabled rule should be disabled
    assertFalse(settings.isRuleEnabled("ComposableNaming"))

    // But OTHER rules should still be enabled
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

    // Master switch should STILL be enabled
    assertTrue(settings.isComposeRulesEnabled)

    // Disabled rules should be disabled
    assertFalse(settings.isRuleEnabled("ComposableNaming"))
    assertFalse(settings.isRuleEnabled("ModifierRequired"))
    assertFalse(settings.isRuleEnabled("RememberState"))

    // Other rules should still be enabled
    assertTrue(settings.isRuleEnabled("PreviewVisibility"))
    assertTrue(settings.isRuleEnabled("LazyListContentType"))
    assertTrue(settings.isRuleEnabled("ParameterOrdering"))
  }
}
