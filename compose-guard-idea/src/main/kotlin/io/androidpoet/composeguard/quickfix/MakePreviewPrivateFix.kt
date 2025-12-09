/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that makes a @Preview function private.
 */
public class MakePreviewPrivateFix : LocalQuickFix {

  override fun getFamilyName(): String = "Make private"

  override fun getName(): String = "Make preview function private"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val function = descriptor.psiElement as? KtNamedFunction
      ?: descriptor.psiElement.parent as? KtNamedFunction
      ?: return

    val factory = KtPsiFactory(project)

    // Remove public/internal modifier if present
    function.modifierList?.getModifier(KtTokens.PUBLIC_KEYWORD)?.delete()
    function.modifierList?.getModifier(KtTokens.INTERNAL_KEYWORD)?.delete()

    // Add private modifier
    val modifierList = function.modifierList
    if (modifierList != null) {
      val privateModifier = factory.createModifier(KtTokens.PRIVATE_KEYWORD)
      modifierList.addBefore(privateModifier, modifierList.firstChild)
    } else {
      function.addModifier(KtTokens.PRIVATE_KEYWORD)
    }
  }
}
