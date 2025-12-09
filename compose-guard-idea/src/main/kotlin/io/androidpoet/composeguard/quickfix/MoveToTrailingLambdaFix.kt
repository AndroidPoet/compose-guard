/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that moves a content lambda parameter to the trailing position.
 */
public class MoveToTrailingLambdaFix : LocalQuickFix {

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
