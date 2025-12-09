/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.RefactoringFactory
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Quick fix that renames a parameter.
 */
public class RenameParameterFix(
  private val suggestedName: String,
) : LocalQuickFix {

  override fun getFamilyName(): String = "Rename parameter"

  override fun getName(): String = "Rename to '$suggestedName'"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val parameter = descriptor.psiElement as? KtParameter
      ?: descriptor.psiElement.parent as? KtParameter
      ?: return

    val nameIdentifier = parameter.nameIdentifier ?: return

    val factory = RefactoringFactory.getInstance(project)
    val rename = factory.createRename(nameIdentifier, suggestedName)
    rename.run()
  }
}
