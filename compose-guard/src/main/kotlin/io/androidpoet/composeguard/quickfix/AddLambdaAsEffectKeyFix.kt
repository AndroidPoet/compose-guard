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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a lambda parameter as a key to a restartable effect.
 *
 * Transforms:
 * LaunchedEffect(Unit) { onComplete() }
 *
 * Into:
 * LaunchedEffect(onComplete) { onComplete() }
 */
public class AddLambdaAsEffectKeyFix(
  private val lambdaName: String,
  private val effectName: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add lambda as effect key"

  override fun getName(): String = "Add '$lambdaName' as $effectName key"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as? KtNameReferenceExpression ?: return

    val effectCall = findParentEffectCall(element) ?: return

    val factory = KtPsiFactory(project)

    val existingArgs = effectCall.valueArguments
      .filter { it.getArgumentExpression() !is KtLambdaExpression }

    val alreadyHasKey = existingArgs.any {
      it.getArgumentExpression()?.text == lambdaName
    }
    if (alreadyHasKey) return

    val trailingLambda = effectCall.lambdaArguments.firstOrNull()?.getLambdaExpression()
      ?: effectCall.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression

    if (existingArgs.isEmpty()) {
      val newArgument = factory.createArgument(factory.createExpression(lambdaName))
      val argList = effectCall.valueArgumentList
      if (argList != null) {
        argList.addArgument(newArgument)
      }
    } else if (existingArgs.size == 1 && existingArgs[0].getArgumentExpression()?.text == "Unit") {
      val unitArg = existingArgs[0]
      val newExpression = factory.createExpression(lambdaName)
      unitArg.getArgumentExpression()?.replace(newExpression)
    } else {
      val newArgument = factory.createArgument(factory.createExpression(lambdaName))
      val argList = effectCall.valueArgumentList
      if (argList != null) {
        val lastNonLambdaArg = existingArgs.lastOrNull()
        if (lastNonLambdaArg != null) {
          argList.addArgumentAfter(newArgument, lastNonLambdaArg)
        } else {
          argList.addArgument(newArgument)
        }
      }
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

  private companion object {
    val RESTARTABLE_EFFECTS = setOf(
      "LaunchedEffect",
      "DisposableEffect",
      "produceState",
    )
  }
}
