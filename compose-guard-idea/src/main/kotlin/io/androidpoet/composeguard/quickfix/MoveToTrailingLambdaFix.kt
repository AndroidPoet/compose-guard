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
 * Quick fix that moves a content lambda parameter to the trailing position.
 */
public class MoveToTrailingLambdaFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Move to trailing position"

  override fun getName(): String = "Move content lambda to trailing position"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val param = when (element) {
      is KtParameter -> element
      else -> element.parent as? KtParameter
    } ?: return

    val function = generateSequence<com.intellij.psi.PsiElement>(param) { it.parent }
      .filterIsInstance<KtNamedFunction>()
      .firstOrNull() ?: return

    val parameterList = function.valueParameterList ?: return
    val params = parameterList.parameters.toMutableList()

    val paramIndex = params.indexOf(param)
    if (paramIndex < 0 || paramIndex == params.lastIndex) return

    // Move to end
    params.removeAt(paramIndex)
    params.add(param)

    val factory = KtPsiFactory(project)
    val newParamTexts = params.map { it.text }
    val newParamListText = newParamTexts.joinToString(", ", "(", ")")
    val newParamList = factory.createParameterList(newParamListText)

    parameterList.replace(newParamList)
  }
}
