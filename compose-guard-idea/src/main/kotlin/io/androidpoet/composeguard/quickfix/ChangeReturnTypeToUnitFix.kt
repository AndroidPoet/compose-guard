/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Quick fix that removes the return type from a composable function
 * (making it return Unit implicitly).
 */
public class ChangeReturnTypeToUnitFix : LocalQuickFix {

  override fun getFamilyName(): String = "Change return type"

  override fun getName(): String = "Remove return type (return Unit)"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val function = when (element) {
      is KtNamedFunction -> element
      else -> generateSequence(element) { it.parent }
        .filterIsInstance<KtNamedFunction>()
        .firstOrNull()
    } ?: return

    // Remove return type
    function.typeReference?.delete()
    // Also remove the colon if present
    function.colon?.delete()
  }
}
