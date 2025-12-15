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
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that reorders composable parameters to follow Compose API guidelines.
 *
 * Official Compose API parameter order:
 * 1. Required parameters (no defaults) - data first, then metadata
 * 2. Modifier parameter (FIRST optional - easily accessible at call site)
 * 3. Other optional parameters (with defaults)
 * 4. Trailing @Composable lambda (if any)
 *
 * From the official Compose Component API Guidelines:
 * "Since the modifier is recommended for any component and is used often,
 * placing it first ensures that it can be set without a named parameter
 * and provides a consistent place for this parameter in any component."
 *
 * Examples:
 * ```kotlin
 * @Composable
 * fun Card(
 *     title: String,                              // 1. required
 *     modifier: Modifier = Modifier,              // 2. modifier (FIRST optional)
 *     elevation: Dp = 4.dp,                       // 3. other optional
 *     content: @Composable () -> Unit             // 4. content (trailing)
 * )
 *
 * @Composable
 * fun FormField(
 *     label: String,                              // 1. required
 *     value: String,                              // 1. required
 *     onValueChange: (String) -> Unit,            // 1. required
 *     modifier: Modifier = Modifier,              // 2. modifier (FIRST optional)
 *     isError: Boolean = false,                   // 3. optional
 *     enabled: Boolean = true,                    // 3. optional
 *     placeholder: String = ""                    // 3. optional
 * )
 * ```
 *
 * @param specificAction Optional specific action description (e.g., "Move 'title, text' after modifier")
 */
public class ReorderParametersFix(
  private val specificAction: String? = null,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Reorder parameters"

  override fun getName(): String = specificAction ?: "Reorder parameters (Compose API guidelines)"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement ?: return
    val function = findParentFunction(element) ?: return

    val parameterList = function.valueParameterList ?: return
    val params = parameterList.parameters.toList()
    if (params.size <= 1) return

    val sortedParams = sortParameters(params)

    // Compare by name to check if order actually changed
    val originalNames = params.map { it.name }
    val sortedNames = sortedParams.map { it.name }
    if (originalNames == sortedNames) return

    val factory = KtPsiFactory(project)
    val newParamTexts = sortedParams.map { it.text }
    // Create a dummy function to get a properly formed parameter list
    val dummyFunction = factory.createFunction("fun dummy(${newParamTexts.joinToString(", ")}) {}")
    val newParamList = dummyFunction.valueParameterList ?: return

    parameterList.replace(newParamList)
  }

  /**
   * Sorts parameters according to Compose API guidelines.
   *
   * Order: required → modifier (FIRST optional) → optionals → content lambda
   *
   * Also ensures state/callback pairs are kept together (e.g., value/onValueChange).
   */
  private fun sortParameters(params: List<KtParameter>): List<KtParameter> {
    val required = mutableListOf<KtParameter>()
    val optional = mutableListOf<KtParameter>()
    val modifier = mutableListOf<KtParameter>()
    val contentLambdas = mutableListOf<KtParameter>()

    for (param in params) {
      when {
        // Modifier is always in its own category
        isModifierParam(param) -> modifier.add(param)

        // Content lambdas go at the end (trailing)
        isContentLambda(param) -> contentLambdas.add(param)

        // Regular params: required vs optional
        param.hasDefaultValue() -> optional.add(param)
        else -> required.add(param)
      }
    }

    // Ensure state/callback pairs are adjacent within each category
    val pairedRequired = pairStateCallbacks(required)
    val pairedOptional = pairStateCallbacks(optional)

    // Sort content lambdas: optional slots first, primary content last
    val sortedContentLambdas = contentLambdas.sortedWith(
      compareBy(
        // Content with defaults (optional slots) come before content without defaults
        { !it.hasDefaultValue() },
        // "content" named lambda should be last among content lambdas
        { it.name == "content" },
      ),
    )

    // Official order: required → modifier (FIRST optional) → optionals → content
    return pairedRequired + modifier + pairedOptional + sortedContentLambdas
  }

  /**
   * Reorders parameters to ensure state/callback pairs are adjacent.
   *
   * Pattern: (value, onValueChange), (checked, onCheckedChange), etc.
   * The callback should immediately follow its state parameter.
   */
  private fun pairStateCallbacks(params: List<KtParameter>): List<KtParameter> {
    if (params.size <= 1) return params

    val stateCallbackPairs = mapOf(
      "value" to "onValueChange",
      "checked" to "onCheckedChange",
      "selected" to "onSelectedChange",
      "expanded" to "onExpandedChange",
      "text" to "onTextChange",
      "query" to "onQueryChange",
    )

    val result = mutableListOf<KtParameter>()
    val usedIndices = mutableSetOf<Int>()

    for ((index, param) in params.withIndex()) {
      if (index in usedIndices) continue

      result.add(param)
      usedIndices.add(index)

      // Check if this is a state param with a corresponding callback
      val callbackName = stateCallbackPairs[param.name]
      if (callbackName != null) {
        // Find the callback in remaining params
        val callbackIndex = params.indexOfFirst { it.name == callbackName }
        if (callbackIndex >= 0 && callbackIndex !in usedIndices) {
          result.add(params[callbackIndex])
          usedIndices.add(callbackIndex)
        }
      }
    }

    return result
  }

  /**
   * Checks if a parameter is a content slot lambda (typically @Composable () -> Unit).
   * Event handlers like onClick should NOT be treated as content lambdas.
   */
  private fun isContentLambda(param: KtParameter): Boolean {
    val typeText = param.typeReference?.text ?: return false
    val name = param.name ?: return false

    // Event handlers (onClick, onEdit, etc.) should NOT be trailing
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

  private fun findParentFunction(element: PsiElement): KtNamedFunction? {
    if (element is KtNamedFunction) return element
    return PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
  }
}
