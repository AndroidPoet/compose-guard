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
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

public class WrapInRememberFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Wrap in remember"

  override fun getName(): String = "Wrap in remember (smart)"

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

    val file = expression.containingKtFile

    val containingFunction = PsiTreeUtil.getParentOfType(expression, KtNamedFunction::class.java)
    val usedParameters = findUsedParameters(expression, containingFunction)
    val readsStateValue = containsStateValueRead(expression)

    val factory = KtPsiFactory(project)
    val exprText = expression.text

    val rememberCall = when {
      readsStateValue -> {
        factory.createExpression("remember { derivedStateOf { $exprText } }")
      }
      usedParameters.isNotEmpty() -> {
        val keys = usedParameters.joinToString(", ")
        factory.createExpression("remember($keys) { $exprText }")
      }
      else -> {
        factory.createExpression("remember { $exprText }")
      }
    }

    expression.replace(rememberCall)

    addRememberImportIfNeeded(project, file)
    if (readsStateValue) {
      addDerivedStateOfImportIfNeeded(project, file)
    }
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

  private fun containsStateValueRead(expression: KtExpression): Boolean {
    val dotExpressions = PsiTreeUtil.findChildrenOfType(expression, KtDotQualifiedExpression::class.java)

    for (dotExpr in dotExpressions) {
      val selector = dotExpr.selectorExpression
      if (selector is KtNameReferenceExpression && selector.getReferencedName() == "value") {
        return true
      }
    }

    return false
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

  private fun addDerivedStateOfImportIfNeeded(
    project: Project,
    file: org.jetbrains.kotlin.psi.KtFile,
  ) {
    val imports = file.importDirectives

    val hasDerivedStateOfImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.runtime.derivedStateOf"
    }

    if (!hasDerivedStateOfImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName("androidx.compose.runtime.derivedStateOf")
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }
}
