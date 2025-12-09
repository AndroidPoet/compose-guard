/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.RefactoringFactory
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Quick fix that adds "Local" prefix to a CompositionLocal property.
 */
public class AddLocalPrefixFix(
  private val currentName: String,
) : LocalQuickFix {

  override fun getFamilyName(): String = "Add Local prefix"

  override fun getName(): String = "Rename to 'Local$currentName'"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val property = descriptor.psiElement as? KtProperty
      ?: descriptor.psiElement.parent as? KtProperty
      ?: return

    val nameIdentifier = property.nameIdentifier ?: return
    val newName = "Local$currentName"

    val factory = RefactoringFactory.getInstance(project)
    val rename = factory.createRename(nameIdentifier, newName)
    rename.run()
  }
}
