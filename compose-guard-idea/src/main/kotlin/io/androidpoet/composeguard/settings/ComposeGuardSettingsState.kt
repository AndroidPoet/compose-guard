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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for Compose Rules plugin.
 * Stores per-rule enable/disable states and category-level settings.
 */
@Service
@State(
  name = "ComposeRulesSettings",
  storages = [Storage("ComposeRulesSettings.xml")],
)
public class ComposeGuardSettingsState : PersistentStateComponent<ComposeGuardSettingsState> {

  /**
   * Master switch to enable/disable all compose rules.
   */
  public var isComposeRulesEnabled: Boolean = true

  /**
   * Show gutter icons for rule violations.
   */
  public var showRuleGutterIcons: Boolean = true

  /**
   * Show inlay hints for rule violations.
   */
  public var showRuleInlayHints: Boolean = true

  /**
   * Show rule violations in the tool window.
   */
  public var showRuleViolationsInToolWindow: Boolean = true

  /**
   * Suppress Android Studio's built-in Compose lint inspections when
   * Compose Guard's corresponding rules are enabled.
   * This prevents duplicate warnings from appearing.
   */
  public var suppressBuiltInInspections: Boolean = true

  // ===== Category-level settings =====

  /**
   * Enable naming convention rules.
   */
  public var enableNamingRules: Boolean = true

  /**
   * Enable modifier-related rules.
   */
  public var enableModifierRules: Boolean = true

  /**
   * Enable state management rules.
   */
  public var enableStateRules: Boolean = true

  /**
   * Enable parameter-related rules.
   */
  public var enableParameterRules: Boolean = true

  /**
   * Enable composable structure rules.
   */
  public var enableComposableRules: Boolean = true

  /**
   * Enable stricter rules (Material 2, unstable collections).
   * Enabled by default per user requirement for stricter enforcement.
   */
  public var enableStricterRules: Boolean = true

  /**
   * Enable experimental rules (LazyList optimizations, etc.).
   * Disabled by default as these rules are still being refined.
   */
  public var enableExperimentalRules: Boolean = false

  // ===== Per-rule settings =====

  /**
   * Map of rule IDs to their enabled/disabled state.
   * Rules not in this map use their default enabled state.
   */
  public var ruleEnabledStates: MutableMap<String, Boolean> = mutableMapOf()

  /**
   * Check if a specific rule is enabled.
   * Returns the rule's default state if not explicitly set.
   *
   * @param ruleId The ID of the rule to check
   * @param defaultEnabled The rule's default enabled state (from rule.enabledByDefault)
   */
  public fun isRuleEnabled(ruleId: String, defaultEnabled: Boolean = true): Boolean {
    // First check if master switch is enabled
    if (!isComposeRulesEnabled) {
      return false
    }
    // Then check per-rule setting, using the rule's default if not explicitly set
    return ruleEnabledStates.getOrDefault(ruleId, defaultEnabled)
  }

  /**
   * Set the enabled state for a specific rule.
   */
  public fun setRuleEnabled(ruleId: String, enabled: Boolean) {
    ruleEnabledStates[ruleId] = enabled
  }

  /**
   * Reset a rule to its default enabled state.
   */
  public fun resetRuleToDefault(ruleId: String) {
    ruleEnabledStates.remove(ruleId)
  }

  /**
   * Reset all rules to their default states.
   */
  public fun resetAllRulesToDefault() {
    ruleEnabledStates.clear()
  }

  /**
   * Enable all rules in a category.
   */
  public fun enableCategory(category: String) {
    when (category) {
      "NAMING" -> enableNamingRules = true
      "MODIFIER" -> enableModifierRules = true
      "STATE" -> enableStateRules = true
      "PARAMETER" -> enableParameterRules = true
      "COMPOSABLE" -> enableComposableRules = true
      "STRICTER" -> enableStricterRules = true
      "EXPERIMENTAL" -> enableExperimentalRules = true
    }
  }

  /**
   * Disable all rules in a category.
   */
  public fun disableCategory(category: String) {
    when (category) {
      "NAMING" -> enableNamingRules = false
      "MODIFIER" -> enableModifierRules = false
      "STATE" -> enableStateRules = false
      "PARAMETER" -> enableParameterRules = false
      "COMPOSABLE" -> enableComposableRules = false
      "STRICTER" -> enableStricterRules = false
      "EXPERIMENTAL" -> enableExperimentalRules = false
    }
  }

  public override fun getState(): ComposeGuardSettingsState = this

  public override fun loadState(state: ComposeGuardSettingsState) {
    XmlSerializerUtil.copyBean(state, this)
  }

  public companion object {
    public fun getInstance(): ComposeGuardSettingsState {
      return service()
    }
  }
}
