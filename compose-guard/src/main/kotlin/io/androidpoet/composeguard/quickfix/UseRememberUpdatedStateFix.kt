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
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * Quick fix that wraps a lambda parameter with rememberUpdatedState.
 *
 * Transforms:
 * @Composable
 * fun MyComposable(onComplete: () -> Unit) {
 *     LaunchedEffect(Unit) {
 *         onComplete()
 *     }
 * }
 *
 * Into:
 * @Composable
 * fun MyComposable(onComplete: () -> Unit) {
 *     val currentOnComplete by rememberUpdatedState(onComplete)
 *     LaunchedEffect(Unit) {
 *         currentOnComplete()
 *     }
 * }
 */
public class UseRememberUpdatedStateFix(
  private val lambdaName: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Use rememberUpdatedState"

  override fun getName(): String = "Wrap '$lambdaName' with rememberUpdatedState"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as? KtNameReferenceExpression ?: return

    val function = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java) ?: return
    val functionBody = function.bodyBlockExpression ?: return

    val factory = KtPsiFactory(project)

    val updatedStateName = "current${lambdaName.replaceFirstChar { it.uppercase() }}"

    if (hasExistingRememberUpdatedState(functionBody, lambdaName)) {
      replaceAllReferencesInEffects(functionBody, lambdaName, updatedStateName, factory)
      return
    }

    val effectCall = findParentEffectCall(element)

    val rememberUpdatedStateProperty = factory.createProperty(
      "val $updatedStateName by rememberUpdatedState($lambdaName)",
    )

    if (effectCall != null) {
      var statementToInsertBefore: PsiElement? = effectCall
      while (statementToInsertBefore?.parent != functionBody && statementToInsertBefore != null) {
        statementToInsertBefore = statementToInsertBefore.parent
      }

      if (statementToInsertBefore != null) {
        functionBody.addBefore(rememberUpdatedStateProperty, statementToInsertBefore)
        functionBody.addBefore(factory.createNewLine(), statementToInsertBefore)
      }
    } else {
      val firstStatement = functionBody.statements.firstOrNull()
      if (firstStatement != null) {
        functionBody.addBefore(rememberUpdatedStateProperty, firstStatement)
        functionBody.addBefore(factory.createNewLine(), firstStatement)
      } else {
        functionBody.add(rememberUpdatedStateProperty)
      }
    }

    replaceAllReferencesInEffects(functionBody, lambdaName, updatedStateName, factory)

    addImportsIfNeeded(project, element)
  }

  private fun hasExistingRememberUpdatedState(body: KtBlockExpression, lambdaName: String): Boolean {
    val properties = PsiTreeUtil.findChildrenOfType(body, org.jetbrains.kotlin.psi.KtProperty::class.java)
    return properties.any { prop ->
      val initializer = prop.delegateExpression?.text ?: prop.initializer?.text ?: ""
      initializer.contains("rememberUpdatedState") && initializer.contains(lambdaName)
    }
  }

  private fun findParentEffectCall(element: PsiElement): KtCallExpression? {
    var current: PsiElement? = element
    while (current != null) {
      if (current is KtCallExpression) {
        val calleeName = current.calleeExpression?.text
        if (calleeName in RESTARTABLE_EFFECTS) {
          return current
        }
      }
      current = current.parent
    }
    return null
  }

  private fun replaceAllReferencesInEffects(
    functionBody: KtBlockExpression,
    originalName: String,
    newName: String,
    factory: KtPsiFactory,
  ) {
    val effectCalls = PsiTreeUtil.findChildrenOfType(functionBody, KtCallExpression::class.java)
      .filter { it.calleeExpression?.text in RESTARTABLE_EFFECTS }

    for (effectCall in effectCalls) {
      val effectLambda = effectCall.lambdaArguments.firstOrNull()?.getLambdaExpression()
        ?: effectCall.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
        ?: continue

      val references = PsiTreeUtil.findChildrenOfType(
        effectLambda.bodyExpression,
        KtNameReferenceExpression::class.java,
      ).filter { it.getReferencedName() == originalName }

      for (ref in references) {
        val newRef = factory.createExpression(newName)
        ref.replace(newRef)
      }
    }
  }

  private fun addImportsIfNeeded(project: Project, element: org.jetbrains.kotlin.psi.KtElement) {
    val file = element.containingKtFile
    val imports = file.importDirectives

    val hasRememberUpdatedStateImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.runtime.rememberUpdatedState"
    }

    if (!hasRememberUpdatedStateImport) {
      val factory = KtPsiFactory(project)
      val fqName = FqName("androidx.compose.runtime.rememberUpdatedState")
      val importPath = ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }

    val hasGetValueImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.runtime.getValue"
    }

    if (!hasGetValueImport) {
      val factory = KtPsiFactory(project)
      val fqName = FqName("androidx.compose.runtime.getValue")
      val importPath = ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }

  private companion object {
    val RESTARTABLE_EFFECTS = setOf(
      "LaunchedEffect",
      "DisposableEffect",
      "produceState",
    )
  }
}
