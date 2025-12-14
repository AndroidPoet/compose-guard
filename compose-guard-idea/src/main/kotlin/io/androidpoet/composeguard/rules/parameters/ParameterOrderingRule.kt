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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.quickfix.ReorderParametersFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.hasDefaultValue
import io.androidpoet.composeguard.rules.isComposableLambda
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Rule: Order composable parameters following Compose API guidelines.
 *
 * Official Compose API parameter order:
 * 1. Required parameters (no defaults) - data first, then metadata
 * 2. Modifier parameter (FIRST optional - easily accessible at call site)
 * 3. Other optional parameters (with defaults)
 * 4. Trailing @Composable lambda (if any)
 *
 * From the official Compose Component API Guidelines:
 * > "Since the modifier is recommended for any component and is used often,
 * > placing it first ensures that it can be set without a named parameter
 * > and provides a consistent place for this parameter in any component."
 *
 * Key conventions:
 * - Keep state and its callback paired: (value: T, onValueChange: (T) -> Unit)
 * - Modifier is always `modifier: Modifier = Modifier`
 * - Content lambda enables trailing lambda syntax
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/components">Compose Components</a>
 */
public class ParameterOrderingRule : ComposableFunctionRule() {

  override val id: String = "ParameterOrdering"

  override val name: String = "Parameter Ordering"

  override val description: String = """
    Composable parameters should follow Compose API guidelines.
    Order: required → modifier (first optional) → optionals → content lambda.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.PARAMETER

  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING

  override val documentationUrl: String =
    "https://developer.android.com/develop/ui/compose/components"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val params = function.valueParameters
    if (params.size <= 1) return emptyList()

    val violations = mutableListOf<ComposeRuleViolation>()

    // Rule 1: Required parameters must come before optional ones
    val hasRequiredAfterOptional = checkRequiredBeforeOptional(params, violations)

    // Rule 2: Modifier should be the first optional parameter
    // Only check this when required params are correctly placed (not after optionals)
    // Otherwise the modifier warning is misleading - the whole order is broken
    if (!hasRequiredAfterOptional) {
      checkModifierPosition(params, violations)
    }

    // Rule 3: Content lambdas should be at the end (trailing lambda syntax)
    checkContentLambdaTrailing(params, violations)

    // Rule 4: State and callback should be paired together
    checkStateCallbackPairing(params, violations)

    return violations
  }

  /**
   * Rule 1: Required parameters (no defaults) must come before optional ones.
   *
   * According to official Compose API guidelines:
   * 1. Required parameters (no defaults)
   * 2. Modifier parameter (FIRST optional)
   * 3. Other optional parameters (with defaults)
   * 4. Content lambda (trailing, if any)
   *
   * @return true if any required params were found after optional params
   */
  private fun checkRequiredBeforeOptional(
    params: List<KtParameter>,
    violations: MutableList<ComposeRuleViolation>,
  ): Boolean {
    var foundOptional = false
    var hasViolation = false
    for (param in params) {
      val hasDefault = param.hasDefaultValue()
      val isContentLambda = isContentLambda(param)

      // Skip content lambdas for this check (they have their own ordering rule)
      if (isContentLambda) continue

      // Check if this is a required param after we've seen an optional param
      // This applies to ALL optional params including modifier (which has default value)
      if (!hasDefault && foundOptional) {
        hasViolation = true
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Required parameter '${param.name}' should come before optional parameters",
            tooltip = """
              Compose API guidelines require this parameter order:
              1. Required parameters (no defaults)
              2. Modifier parameter (first optional)
              3. Optional parameters (with defaults)
              4. Content lambda (trailing, if any)

              '${param.name}' has no default value but appears after optional parameters.
            """.trimIndent(),
            quickFixes = listOf(
              ReorderParametersFix("Move '${param.name}' before optional parameters"),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }

      // Track when we've seen any optional parameter (including modifier with default)
      if (hasDefault) {
        foundOptional = true
      }
    }
    return hasViolation
  }

  /**
   * Rule 2: Modifier must be the FIRST optional parameter.
   *
   * From the official Compose Component API Guidelines:
   * "Since the modifier is recommended for any component and is used often,
   * placing it first ensures that it can be set without a named parameter
   * and provides a consistent place for this parameter in any component."
   *
   * Correct order: required → modifier (FIRST optional) → other optionals → content lambda
   */
  private fun checkModifierPosition(
    params: List<KtParameter>,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    val modifierIndex = params.indexOfFirst { isModifierParam(it) }
    if (modifierIndex < 0) return

    val modifierParam = params[modifierIndex]

    // Only check if modifier has a default value (is optional)
    // If modifier is required (no default), it belongs in the required params section
    if (!modifierParam.hasDefaultValue()) return

    // Check for optional parameters BEFORE modifier (they should come AFTER)
    // This includes content lambdas with defaults (optional slots) since modifier
    // should be the FIRST optional parameter
    val paramsBeforeModifier = params.take(modifierIndex)
    val optionalBeforeModifier = paramsBeforeModifier.filter { param ->
      param.hasDefaultValue()
    }

    if (optionalBeforeModifier.isNotEmpty()) {
      val wrongParams = optionalBeforeModifier.mapNotNull { it.name }.joinToString(", ")
      violations.add(
        createViolation(
          element = modifierParam.nameIdentifier ?: modifierParam,
          message = "Modifier parameter should be the first optional parameter",
          tooltip = """
            Modifier must be the FIRST optional parameter (right after required params).

            These optional parameters appear before modifier but should come after:
            $wrongParams

            Correct order:
            fun FormField(
                label: String,                      // 1. Required params
                value: String,                      // 1. Required params
                onValueChange: (String) -> Unit,    // 1. Required callback
                modifier: Modifier = Modifier,      // 2. Modifier (FIRST optional)
                isError: Boolean = false,           // 3. Other optional params
                enabled: Boolean = true,
                placeholder: String = ""
            )

            From official guidelines: "Since the modifier is recommended for any
            component and is used often, placing it first ensures that it can be
            set without a named parameter and provides a consistent place for
            this parameter in any component."
          """.trimIndent(),
          quickFixes = listOf(
            ReorderParametersFix("Move '$wrongParams' after modifier"),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }
  }

  /**
   * Rule 3: Content lambdas should be at the end for trailing lambda syntax.
   */
  private fun checkContentLambdaTrailing(
    params: List<KtParameter>,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    for ((index, param) in params.withIndex()) {
      if (!isContentLambda(param)) continue

      // Check if there are non-content-lambda, non-modifier params after this
      val paramsAfter = params.drop(index + 1)
      val wrongParamsAfter = paramsAfter.filter { !isContentLambda(it) && !isModifierParam(it) }

      if (wrongParamsAfter.isNotEmpty()) {
        violations.add(
          createViolation(
            element = param.nameIdentifier ?: param,
            message = "Content lambda '${param.name}' should be at the end",
            tooltip = """
              Content slot lambdas (@Composable () -> Unit) should be at the
              end of the parameter list for trailing lambda syntax.

              Example:
              fun Card(
                  title: String,
                  modifier: Modifier = Modifier,
                  content: @Composable () -> Unit  // Last for trailing lambda
              )

              Call site: Card("Title") { Text("Content") }
            """.trimIndent(),
            quickFixes = listOf(
              ReorderParametersFix("Move '${param.name}' to trailing position"),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
        break
      }
    }
  }

  /**
   * Rule 4: State values and their callbacks should be paired together.
   *
   * Pattern: (value: T, onValueChange: (T) -> Unit) should be adjacent.
   */
  private fun checkStateCallbackPairing(
    params: List<KtParameter>,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    val stateCallbackPairs = mapOf(
      "value" to "onValueChange",
      "checked" to "onCheckedChange",
      "selected" to "onSelectedChange",
      "expanded" to "onExpandedChange",
      "text" to "onTextChange",
      "query" to "onQueryChange",
    )

    for ((stateName, callbackName) in stateCallbackPairs) {
      val stateIndex = params.indexOfFirst { it.name == stateName }
      val callbackIndex = params.indexOfFirst { it.name == callbackName }

      if (stateIndex >= 0 && callbackIndex >= 0) {
        // They should be adjacent (callback right after state)
        if (callbackIndex != stateIndex + 1) {
          violations.add(
            createViolation(
              element = params[callbackIndex].nameIdentifier ?: params[callbackIndex],
              message = "'$callbackName' should immediately follow '$stateName'",
              tooltip = """
                State and callback parameters should be paired together:

                ✅ Correct:
                fun TextField(
                    value: String,
                    onValueChange: (String) -> Unit,  // Right after value
                    ...
                )

                ❌ Wrong:
                fun TextField(
                    value: String,
                    label: String,
                    onValueChange: (String) -> Unit,  // Separated from value
                    ...
                )
              """.trimIndent(),
              quickFixes = listOf(
                ReorderParametersFix("Move '$callbackName' next to '$stateName'"),
                SuppressComposeRuleFix(id),
              ),
            ),
          )
        }
      }
    }
  }

  /**
   * Checks if a parameter is a content slot lambda (typically @Composable () -> Unit).
   * Event handlers like onClick should NOT be treated as content lambdas.
   */
  private fun isContentLambda(param: KtParameter): Boolean {
    val typeText = param.typeReference?.text ?: return false
    val name = param.name ?: return false

    // Event handlers (onClick, onEdit, etc.) are NOT content slots
    if (name.startsWith("on") && name.length > 2 && name[2].isUpperCase()) {
      return false
    }

    // Content slots are @Composable lambdas
    return typeText.contains("@Composable") && typeText.contains("->")
  }

  private fun isModifierParam(param: KtParameter): Boolean {
    val typeName = param.typeReference?.text ?: return false
    return typeName == "Modifier" || typeName.endsWith(".Modifier")
  }
}
