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

@Service
@State(
  name = "ComposeRulesSettings",
  storages = [Storage("ComposeRulesSettings.xml")],
)
public class ComposeGuardSettingsState : PersistentStateComponent<ComposeGuardSettingsState> {

  public var showRuleGutterIcons: Boolean = true

  public var showRuleInlayHints: Boolean = true

  public var showRuleViolationsInToolWindow: Boolean = true

  public var suppressBuiltInInspections: Boolean = true

  public var ruleEnabledStates: MutableMap<String, Boolean> = mutableMapOf()

  public fun isRuleEnabled(ruleId: String, defaultEnabled: Boolean = true): Boolean {
    return ruleEnabledStates.getOrDefault(ruleId, defaultEnabled)
  }

  public fun setRuleEnabled(ruleId: String, enabled: Boolean) {
    ruleEnabledStates[ruleId] = enabled
  }

  public fun enableRules(ruleIds: List<String>) {
    for (ruleId in ruleIds) {
      ruleEnabledStates[ruleId] = true
    }
  }

  public fun disableRules(ruleIds: List<String>) {
    for (ruleId in ruleIds) {
      ruleEnabledStates[ruleId] = false
    }
  }

  public fun resetRuleToDefault(ruleId: String) {
    ruleEnabledStates.remove(ruleId)
  }

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
