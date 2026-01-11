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

    val originalNames = params.map { it.name }
    val sortedNames = sortedParams.map { it.name }
    if (originalNames == sortedNames) return

    val factory = KtPsiFactory(project)
    val newParamTexts = sortedParams.map { it.text }
    val dummyFunction = factory.createFunction("fun dummy(${newParamTexts.joinToString(", ")}) {}")
    val newParamList = dummyFunction.valueParameterList ?: return

    parameterList.replace(newParamList)
  }

  private fun sortParameters(params: List<KtParameter>): List<KtParameter> {
    val required = mutableListOf<KtParameter>()
    val optional = mutableListOf<KtParameter>()
    val modifier = mutableListOf<KtParameter>()
    val contentLambdas = mutableListOf<KtParameter>()

    for (param in params) {
      when {
        isModifierParam(param) -> modifier.add(param)

        isContentLambda(param) -> contentLambdas.add(param)

        param.hasDefaultValue() -> optional.add(param)
        else -> required.add(param)
      }
    }

    val pairedRequired = pairStateCallbacks(required)
    val pairedOptional = pairStateCallbacks(optional)

    val sortedContentLambdas = contentLambdas.sortedWith(
      compareBy(
        { !it.hasDefaultValue() },
        { it.name == "content" },
      ),
    )

    return pairedRequired + modifier + pairedOptional + sortedContentLambdas
  }

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

      val callbackName = stateCallbackPairs[param.name]
      if (callbackName != null) {
        val callbackIndex = params.indexOfFirst { it.name == callbackName }
        if (callbackIndex >= 0 && callbackIndex !in usedIndices) {
          result.add(params[callbackIndex])
          usedIndices.add(callbackIndex)
        }
      }
    }

    return result
  }

  private fun isContentLambda(param: KtParameter): Boolean {
    val typeText = param.typeReference?.text ?: return false
    val name = param.name ?: return false

    if (name.startsWith("on") && name.length > 2 && name[2].isUpperCase()) {
      return false
    }

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
