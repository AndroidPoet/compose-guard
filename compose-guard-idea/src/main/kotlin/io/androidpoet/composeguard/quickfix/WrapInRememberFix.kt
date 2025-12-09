/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Quick fix that wraps a state creation call in remember {}.
 */
public class WrapInRememberFix : LocalQuickFix {

  override fun getFamilyName(): String = "Wrap in remember"

  override fun getName(): String = "Wrap in remember { }"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement

    // Find the call expression or property to wrap
    val callExpr = when (element) {
      is KtCallExpression -> element
      is KtProperty -> element.initializer as? KtCallExpression
      else -> element.parent as? KtCallExpression
        ?: (element.parent as? KtProperty)?.initializer as? KtCallExpression
    } ?: return

    val factory = KtPsiFactory(project)
    val callText = callExpr.text

    // Create the remember { } wrapper
    val rememberCall = factory.createExpression("remember { $callText }")

    callExpr.replace(rememberCall)

    // Add import for remember if not present
    addRememberImportIfNeeded(project, callExpr)
  }

  private fun addRememberImportIfNeeded(
    project: Project,
    element: org.jetbrains.kotlin.psi.KtElement,
  ) {
    val file = element.containingKtFile
    val imports = file.importDirectives

    val hasRememberImport = imports.any {
      it.importedFqName?.asString() == "androidx.compose.runtime.remember"
    }

    if (!hasRememberImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName("androidx.compose.runtime.remember")
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }
}
