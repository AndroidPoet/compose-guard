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
 *
 * Simple design:
 * - Each rule has its own enabled/disabled state stored in ruleEnabledStates map
 * - If a rule is not in the map, it uses its default enabled state
 * - Master switch and category checkboxes in UI are just convenience toggles
 */
@Service
@State(
  name = "ComposeRulesSettings",
  storages = [Storage("ComposeRulesSettings.xml")],
)
public class ComposeGuardSettingsState : PersistentStateComponent<ComposeGuardSettingsState> {

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

  /**
   * Map of rule IDs to their enabled/disabled state.
   * Rules not in this map use their default enabled state.
   *
   * This is the single source of truth for rule enable/disable states.
   */
  public var ruleEnabledStates: MutableMap<String, Boolean> = mutableMapOf()

  /**
   * Check if a specific rule is enabled.
   * Returns the stored state, or the rule's default if not explicitly set.
   *
   * @param ruleId The ID of the rule to check
   * @param defaultEnabled The rule's default enabled state (from rule.enabledByDefault)
   */
  public fun isRuleEnabled(ruleId: String, defaultEnabled: Boolean = true): Boolean {
    return ruleEnabledStates.getOrDefault(ruleId, defaultEnabled)
  }

  /**
   * Set the enabled state for a specific rule.
   */
  public fun setRuleEnabled(ruleId: String, enabled: Boolean) {
    ruleEnabledStates[ruleId] = enabled
  }

  /**
   * Enable all rules in a list.
   */
  public fun enableRules(ruleIds: List<String>) {
    for (ruleId in ruleIds) {
      ruleEnabledStates[ruleId] = true
    }
  }

  /**
   * Disable all rules in a list.
   */
  public fun disableRules(ruleIds: List<String>) {
    for (ruleId in ruleIds) {
      ruleEnabledStates[ruleId] = false
    }
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
