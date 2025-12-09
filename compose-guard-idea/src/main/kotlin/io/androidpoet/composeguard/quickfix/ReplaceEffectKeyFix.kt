/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that suggests replacing a constant effect key with a TODO placeholder.
 */
public class ReplaceEffectKeyFix(
  private val suggestedKey: String = "/* TODO: Add appropriate key */",
) : LocalQuickFix {

  override fun getFamilyName(): String = "Replace effect key"

  override fun getName(): String = "Replace constant key with TODO"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val call = element as? KtCallExpression ?: return

    val args = call.valueArguments
    if (args.isEmpty()) return

    val firstArg = args.first()
    val factory = KtPsiFactory(project)

    // Replace the first argument with the suggested key
    val newArg = factory.createArgument(suggestedKey)
    firstArg.replace(newArg)
  }
}
