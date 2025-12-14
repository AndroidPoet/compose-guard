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
 */
public class ComposeBuiltInInspectionSuppressor : InspectionSuppressor {

  /**
   * Map of built-in Compose inspection IDs to the Compose Guard category that covers them.
   */
  private val suppressedInspections = mapOf(
    // Modifier-related inspections -> enabled when modifier rules are enabled
    "ModifierParameter" to CategoryCheck.MODIFIER,
    "ComposableModifierFactory" to CategoryCheck.MODIFIER,

    // Naming-related inspections -> enabled when naming rules are enabled
    "ComposableNaming" to CategoryCheck.NAMING,

    // Parameter-related inspections -> enabled when parameter rules are enabled
    "LambdaParameterInRestartableEffect" to CategoryCheck.PARAMETER,
  )

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    val categoryCheck = suppressedInspections[toolId] ?: return false

    val settings = ComposeGuardSettingsState.getInstance()

    // Only suppress if Compose Guard is enabled
    if (!settings.isComposeRulesEnabled) {
      return false
    }

    // Only suppress if the category that handles this inspection is enabled
    if (!settings.suppressBuiltInInspections) {
      return false
    }

    return when (categoryCheck) {
      CategoryCheck.MODIFIER -> settings.enableModifierRules
      CategoryCheck.NAMING -> settings.enableNamingRules
      CategoryCheck.PARAMETER -> settings.enableParameterRules
    }
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    return SuppressQuickFix.EMPTY_ARRAY
  }

  private enum class CategoryCheck {
    MODIFIER,
    NAMING,
    PARAMETER,
  }
}
