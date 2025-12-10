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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds an implicit dependency as an explicit parameter.
 */
public class AddExplicitParameterFix(
  private val dependencyName: String,
  private val parameterType: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add explicit parameter"

  override fun getName(): String = "Add '$dependencyName' as parameter"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val call = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val function = generateSequence<com.intellij.psi.PsiElement>(call) { it.parent }
      .filterIsInstance<KtNamedFunction>()
      .firstOrNull() ?: return

    val factory = KtPsiFactory(project)
    val parameterList = function.valueParameterList ?: return

    // Generate parameter name from dependency
    val paramName = generateParamName(dependencyName)
    val newParam = factory.createParameter("$paramName: $parameterType")

    // Find insertion point (before optional params and trailing lambdas)
    val params = parameterList.parameters
    val insertIndex = findInsertIndex(params)

    if (params.isEmpty()) {
      parameterList.addParameter(newParam)
    } else if (insertIndex >= params.size) {
      parameterList.addParameterAfter(newParam, params.last())
    } else if (insertIndex == 0) {
      parameterList.addParameterBefore(newParam, params.first())
    } else {
      parameterList.addParameterAfter(newParam, params[insertIndex - 1])
    }
  }

  private fun generateParamName(dependency: String): String {
    return when {
      dependency.startsWith("Local") -> dependency.removePrefix("Local").replaceFirstChar { it.lowercase() }
      dependency.endsWith("ViewModel") -> "viewModel"
      dependency.contains("ViewModel") -> dependency.replaceFirstChar { it.lowercase() }
      else -> dependency.replaceFirstChar { it.lowercase() }
    }
  }

  private fun findInsertIndex(params: List<org.jetbrains.kotlin.psi.KtParameter>): Int {
    for ((index, param) in params.withIndex()) {
      val hasDefault = param.hasDefaultValue()
      val isLambda = param.typeReference?.text?.contains("->") == true
      if (hasDefault || isLambda) {
        return index
      }
    }
    return params.size
  }
}
