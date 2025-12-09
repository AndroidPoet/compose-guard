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
}
