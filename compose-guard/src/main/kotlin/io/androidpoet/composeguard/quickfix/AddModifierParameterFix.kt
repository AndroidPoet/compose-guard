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
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a modifier parameter to a composable function.
 */
public class AddModifierParameterFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add modifier parameter"

  override fun getName(): String = "Add 'modifier: Modifier = Modifier' parameter"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val function = when (element) {
      is KtNamedFunction -> element
      else -> element.parent as? KtNamedFunction
    } ?: return

    val factory = KtPsiFactory(project)
    val parameterList = function.valueParameterList ?: return

    val parameters = parameterList.parameters
    val insertIndex = findModifierInsertIndex(parameters)

    val modifierParam = factory.createParameter("modifier: Modifier = Modifier")

    if (parameters.isEmpty()) {
      parameterList.addParameter(modifierParam)
    } else if (insertIndex >= parameters.size) {
      parameterList.addParameterAfter(modifierParam, parameters.last())
    } else if (insertIndex == 0) {
      parameterList.addParameterBefore(modifierParam, parameters.first())
    } else {
      parameterList.addParameterAfter(modifierParam, parameters[insertIndex - 1])
    }

    addModifierImportIfNeeded(project, function)
  }

  private fun findModifierInsertIndex(parameters: List<org.jetbrains.kotlin.psi.KtParameter>): Int {
    for ((index, param) in parameters.withIndex()) {
      val hasDefault = param.hasDefaultValue()
      val isLambda = param.typeReference?.text?.contains("->") == true ||
        param.typeReference?.text?.startsWith("@Composable") == true

      if (hasDefault && !isLambda) {
        return index
      }
    }

    for ((index, param) in parameters.withIndex()) {
      val isLambda = param.typeReference?.text?.contains("->") == true ||
        param.typeReference?.text?.startsWith("@Composable") == true
      if (isLambda) {
        return index
      }
    }

    return parameters.size
  }

  private fun addModifierImportIfNeeded(project: Project, function: KtNamedFunction) {
    val file = function.containingKtFile
    val imports = file.importDirectives

    val hasModifierImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.ui.Modifier"
    }

    if (!hasModifierImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName("androidx.compose.ui.Modifier")
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }
}
