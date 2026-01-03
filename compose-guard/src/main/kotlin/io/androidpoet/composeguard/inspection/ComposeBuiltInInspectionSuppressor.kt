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
package io.androidpoet.composeguard.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState

/**
 * Suppresses Android Studio's built-in Compose lint inspections when
 * Compose Guard's corresponding rules are enabled.
 *
 * This prevents duplicate warnings from appearing when both Compose Guard
 * and Android Studio's built-in inspections are active.
 *
 * Built-in inspections suppressed:
 * - ModifierParameter: Covered by ParameterOrderingRule, ModifierNamingRule, ModifierDefaultValueRule
 * - ComposableNaming: Covered by ComposableNamingRule
 * - ComposableModifierFactory: Covered by ModifierNamingRule
 * - LambdaParameterInRestartableEffect: Covered by LambdaParameterInEffect
 */
public class ComposeBuiltInInspectionSuppressor : InspectionSuppressor {

  /**
   * Map of built-in Compose inspection IDs to the Compose Guard rule IDs that cover them.
   */
  private val suppressedInspections = mapOf(
    "ModifierParameter" to listOf("ModifierRequired", "ModifierDefaultValue", "ModifierNaming"),
    "ComposableModifierFactory" to listOf("ModifierNaming"),

    "ComposableNaming" to listOf("ComposableNaming"),

    "LambdaParameterInRestartableEffect" to listOf("LambdaParameterInEffect"),
  )

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    val ruleIds = suppressedInspections[toolId] ?: return false

    val settings = ComposeGuardSettingsState.getInstance()

    if (!settings.suppressBuiltInInspections) {
      return false
    }

    return ruleIds.any { ruleId ->
      settings.isRuleEnabled(ruleId, defaultEnabled = true)
    }
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    return SuppressQuickFix.EMPTY_ARRAY
  }
}
