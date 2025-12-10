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
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that reorders composable parameters to follow the recommended order:
 * 1. Required parameters (no defaults)
 * 2. Modifier parameter (with default)
 * 3. Optional parameters (with defaults)
 * 4. Content lambda (trailing, with default)
 */
public class ReorderParametersFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Reorder parameters"

  override fun getName(): String = "Reorder parameters to recommended order"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val function = when (element) {
      is KtNamedFunction -> element
      else -> generateSequence(element) { it.parent }
        .filterIsInstance<KtNamedFunction>()
        .firstOrNull()
    } ?: return

    val parameterList = function.valueParameterList ?: return
    val params = parameterList.parameters.toList()
    if (params.size <= 1) return

    val sortedParams = sortParameters(params)
    if (sortedParams == params) return

    val factory = KtPsiFactory(project)
    val newParamTexts = sortedParams.map { it.text }
    val newParamListText = newParamTexts.joinToString(", ", "(", ")")
    val newParamList = factory.createParameterList(newParamListText)

    parameterList.replace(newParamList)
  }

  private fun sortParameters(params: List<KtParameter>): List<KtParameter> {
    val required = mutableListOf<KtParameter>()
    val modifier = mutableListOf<KtParameter>()
    val optional = mutableListOf<KtParameter>()
    val trailing = mutableListOf<KtParameter>()

    for (param in params) {
      when {
        isModifierParam(param) -> modifier.add(param)
        // Only content lambdas with defaults should be trailing
        isContentLambda(param) && param.hasDefaultValue() -> trailing.add(param)
        // Required lambdas (no default) are required parameters
        param.hasDefaultValue() -> optional.add(param)
        else -> required.add(param)
      }
    }

    return required + modifier + optional + trailing
  }

  /**
   * Checks if a parameter is a content slot lambda (typically @Composable () -> Unit).
   * Event handlers like onClick should NOT be treated as content lambdas.
   */
  private fun isContentLambda(param: KtParameter): Boolean {
    val typeText = param.typeReference?.text ?: return false
    val name = param.name ?: return false
    // Content slots are typically @Composable lambdas named "content" or similar
    // Event handlers (onClick, onEdit, etc.) should NOT be trailing
    if (name.startsWith("on") && name.length > 2 && name[2].isUpperCase()) {
      return false // This is an event handler, not a content slot
    }
    return typeText.contains("@Composable") && typeText.contains("->")
  }

  private fun isModifierParam(param: KtParameter): Boolean {
    val typeName = param.typeReference?.text ?: return false
    return typeName == "Modifier" || typeName.endsWith(".Modifier")
  }
}
