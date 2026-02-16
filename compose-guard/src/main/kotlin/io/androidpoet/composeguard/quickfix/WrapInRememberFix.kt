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
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

public class WrapInRememberFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Wrap in remember"

  override fun getName(): String = "Wrap in remember { }"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement

    val expression: KtExpression = when (element) {
      is KtExpression -> element
      is KtProperty -> element.initializer
      else -> {
        element.parent as? KtExpression
          ?: (element.parent as? KtProperty)?.initializer
      }
    } ?: return

    // Prevent endless wrapping: check if already inside remember
    if (isAlreadyInsideRemember(expression)) {
      return
    }

    val file = expression.containingKtFile

    val containingFunction = PsiTreeUtil.getParentOfType(expression, KtNamedFunction::class.java)
    val usedParameters = findUsedParameters(expression, containingFunction)

    val factory = KtPsiFactory(project)
    val exprText = expression.text

    // Simple wrapping: just wrap in remember, optionally with parameter keys
    val rememberCall = if (usedParameters.isNotEmpty()) {
      val keys = usedParameters.joinToString(", ")
      factory.createExpression("remember($keys) { $exprText }")
    } else {
      factory.createExpression("remember { $exprText }")
    }

    expression.replace(rememberCall)

    addRememberImportIfNeeded(project, file)
  }

  private fun isAlreadyInsideRemember(expression: KtExpression): Boolean {
    var parent = expression.parent
    while (parent != null) {
      if (parent is KtCallExpression) {
        val calleeName = parent.calleeExpression?.text
        if (calleeName != null && REMEMBER_FUNCTION_NAMES.contains(calleeName)) {
          return true
        }
      }
      // Stop searching at function boundaries
      if (parent is KtNamedFunction) {
        break
      }
      parent = parent.parent
    }
    return false
  }

  private companion object {
    private val REMEMBER_FUNCTION_NAMES = setOf(
      "remember",
      "rememberSaveable",
      "rememberCoroutineScope",
      "rememberUpdatedState",
    )
  }

  private fun findUsedParameters(
    expression: KtExpression,
    containingFunction: KtNamedFunction?,
  ): List<String> {
    if (containingFunction == null) return emptyList()

    val parameterNames = containingFunction.valueParameters
      .mapNotNull { it.name }
      .filter { it != "modifier" }
      .toSet()

    if (parameterNames.isEmpty()) return emptyList()

    val nameReferences = PsiTreeUtil.findChildrenOfType(expression, KtNameReferenceExpression::class.java)

    val usedParams = mutableSetOf<String>()
    for (ref in nameReferences) {
      val name = ref.getReferencedName()
      if (name in parameterNames) {
        usedParams.add(name)
      }
    }

    return usedParams.toList()
  }

  private fun addRememberImportIfNeeded(
    project: Project,
    file: org.jetbrains.kotlin.psi.KtFile,
  ) {
    val imports = file.importDirectives

    val hasRememberImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.runtime.remember"
    }

    if (!hasRememberImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName("androidx.compose.runtime.remember")
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }

}
