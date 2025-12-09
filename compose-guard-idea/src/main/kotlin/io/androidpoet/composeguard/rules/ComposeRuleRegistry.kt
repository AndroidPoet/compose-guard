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
package io.androidpoet.composeguard.rules

import io.androidpoet.composeguard.rules.composables.ContentEmissionRule
import io.androidpoet.composeguard.rules.composables.EffectKeysRule
import io.androidpoet.composeguard.rules.composables.MovableContentRule
import io.androidpoet.composeguard.rules.composables.MultipleContentRule
import io.androidpoet.composeguard.rules.composables.PreviewVisibilityRule
import io.androidpoet.composeguard.rules.modifiers.AvoidComposedRule
import io.androidpoet.composeguard.rules.modifiers.ModifierDefaultValueRule
import io.androidpoet.composeguard.rules.modifiers.ModifierNamingRule
import io.androidpoet.composeguard.rules.modifiers.ModifierOrderRule
import io.androidpoet.composeguard.rules.modifiers.ModifierRequiredRule
import io.androidpoet.composeguard.rules.modifiers.ModifierReuseRule
import io.androidpoet.composeguard.rules.modifiers.ModifierTopMostRule
import io.androidpoet.composeguard.rules.naming.ComposableAnnotationNamingRule
import io.androidpoet.composeguard.rules.naming.ComposableNamingRule
import io.androidpoet.composeguard.rules.naming.CompositionLocalNamingRule
import io.androidpoet.composeguard.rules.naming.EventParameterNamingRule
import io.androidpoet.composeguard.rules.naming.MultipreviewNamingRule
import io.androidpoet.composeguard.rules.naming.PreviewNamingRule
import io.androidpoet.composeguard.rules.parameters.ExplicitDependenciesRule
import io.androidpoet.composeguard.rules.parameters.MutableParameterRule
import io.androidpoet.composeguard.rules.parameters.ParameterOrderingRule
import io.androidpoet.composeguard.rules.parameters.TrailingLambdaRule
import io.androidpoet.composeguard.rules.state.HoistStateRule
import io.androidpoet.composeguard.rules.state.MutableStateParameterRule
import io.androidpoet.composeguard.rules.state.RememberStateRule
import io.androidpoet.composeguard.rules.state.TypeSpecificStateRule
import io.androidpoet.composeguard.rules.stricter.Material2Rule
import io.androidpoet.composeguard.rules.stricter.UnstableCollectionsRule
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState

/**
 * Central registry for all compose rules.
 * Provides access to all registered rules and filtering capabilities.
 */
public object ComposeRuleRegistry {

  private val rules = mutableListOf<ComposeRule>()

  init {
    // Phase 1: Core Rules (Naming)
    register(ComposableNamingRule())

    // Phase 1: Core Rules (Modifiers)
    register(ModifierRequiredRule())
    register(ModifierDefaultValueRule())
    register(ModifierNamingRule())
    register(ModifierTopMostRule())
    register(ModifierReuseRule())

    // Phase 1: Core Rules (State)
    register(RememberStateRule())
    register(TypeSpecificStateRule())

    // Phase 2: Parameter & Slot Rules
    register(ParameterOrderingRule())
    register(TrailingLambdaRule())
    register(MutableParameterRule())
    register(MutableStateParameterRule())
    register(ExplicitDependenciesRule())

    // Phase 3: Naming Convention Rules
    register(CompositionLocalNamingRule())
    register(MultipreviewNamingRule())
    register(PreviewNamingRule())
    register(ComposableAnnotationNamingRule())
    register(EventParameterNamingRule())

    // Phase 4: Composable Structure Rules
    register(ContentEmissionRule())
    register(MultipleContentRule())
    register(EffectKeysRule())
    register(MovableContentRule())
    register(PreviewVisibilityRule())

    // Phase 4: Advanced Rules
    register(ModifierOrderRule())
    register(AvoidComposedRule())
    register(HoistStateRule())

    // Phase 5: Stricter Rules (enabled by default per user requirement)
    register(Material2Rule())
    register(UnstableCollectionsRule())
  }

  /**
   * Register a new rule.
   */
  public fun register(rule: ComposeRule) {
    rules.add(rule)
  }

  /**
   * Get all registered rules.
   */
  public fun getAllRules(): List<ComposeRule> {
    return rules.toList()
  }

  /**
   * Get all enabled rules based on current settings.
   */
  public fun getEnabledRules(): List<ComposeRule> {
    val settings = ComposeGuardSettingsState.getInstance()
    return rules.filter { rule ->
      settings.isRuleEnabled(rule.id) && isCategoryEnabled(rule.category, settings)
    }
  }

  /**
   * Get rules by category.
   */
  public fun getRulesByCategory(category: RuleCategory): List<ComposeRule> {
    return rules.filter { it.category == category }
  }

  /**
   * Get a rule by its ID.
   */
  public fun getRuleById(id: String): ComposeRule? {
    return rules.find { it.id == id }
  }

  /**
   * Get all rule categories with their rules.
   */
  public fun getRulesByCategories(): Map<RuleCategory, List<ComposeRule>> {
    return rules.groupBy { it.category }
  }

  /**
   * Get the total count of rules.
   */
  public fun getRuleCount(): Int = rules.size

  /**
   * Get the count of enabled rules.
   */
  public fun getEnabledRuleCount(): Int = getEnabledRules().size

  /**
   * Check if a category is enabled in settings.
   */
  private fun isCategoryEnabled(category: RuleCategory, settings: ComposeGuardSettingsState): Boolean {
    return when (category) {
      RuleCategory.NAMING -> settings.enableNamingRules
      RuleCategory.MODIFIER -> settings.enableModifierRules
      RuleCategory.STATE -> settings.enableStateRules
      RuleCategory.PARAMETER -> settings.enableParameterRules
      RuleCategory.COMPOSABLE -> settings.enableComposableRules
      RuleCategory.STRICTER -> settings.enableStricterRules
    }
  }
}
