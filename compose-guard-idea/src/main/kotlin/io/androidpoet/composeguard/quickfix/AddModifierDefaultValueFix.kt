/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a default value to a modifier parameter.
 */
public class AddModifierDefaultValueFix : LocalQuickFix {

  override fun getFamilyName(): String = "Add default value"

  override fun getName(): String = "Add '= Modifier' default value"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val parameter = descriptor.psiElement as? KtParameter
      ?: descriptor.psiElement.parent as? KtParameter
      ?: return

    if (parameter.hasDefaultValue()) return

    val factory = KtPsiFactory(project)
    val typeRef = parameter.typeReference?.text ?: "Modifier"
    val newParam = factory.createParameter("${parameter.name}: $typeRef = Modifier")

    parameter.replace(newParam)
  }
}
