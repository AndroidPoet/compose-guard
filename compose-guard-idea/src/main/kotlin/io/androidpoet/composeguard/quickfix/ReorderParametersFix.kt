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
 * Quick fix that reorders composable parameters to follow the recommended order:
 * 1. Required parameters (no defaults)
 * 2. Modifier parameter (with default)
 * 3. Optional parameters (with defaults)
 * 4. Content lambda (trailing, with default)
 */
public class ReorderParametersFix : LocalQuickFix {

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
        isComposableLambda(param) -> trailing.add(param)
        param.hasDefaultValue() -> optional.add(param)
        else -> required.add(param)
      }
    }

    return required + modifier + optional + trailing
  }

  private fun isModifierParam(param: KtParameter): Boolean {
    val typeName = param.typeReference?.text ?: return false
    return typeName == "Modifier" || typeName.endsWith(".Modifier")
  }

  private fun isComposableLambda(param: KtParameter): Boolean {
    val typeText = param.typeReference?.text ?: return false
    return typeText.contains("@Composable") && typeText.contains("->")
  }
}
