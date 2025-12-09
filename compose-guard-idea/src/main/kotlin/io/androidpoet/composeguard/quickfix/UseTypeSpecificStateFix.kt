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
 * Quick fix that replaces mutableStateOf with type-specific variant.
 */
public class UseTypeSpecificStateFix(
  private val targetFunction: String,
) : LocalQuickFix {

  override fun getFamilyName(): String = "Use type-specific mutableStateOf"

  override fun getName(): String = "Replace with $targetFunction()"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val callExpr = descriptor.psiElement as? KtCallExpression
      ?: descriptor.psiElement.parent as? KtCallExpression
      ?: return

    val args = callExpr.valueArgumentList?.arguments?.firstOrNull()?.text ?: return
    val factory = KtPsiFactory(project)

    val newCall = factory.createExpression("$targetFunction($args)")
    callExpr.replace(newCall)

    // Add import if needed
    addImportIfNeeded(project, callExpr)
  }

  private fun addImportIfNeeded(project: Project, element: org.jetbrains.kotlin.psi.KtElement) {
    val file = element.containingKtFile
    val imports = file.importDirectives

    val fqNameStr = "androidx.compose.runtime.$targetFunction"
    val hasImport = imports.any {
      it.importedFqName?.asString() == fqNameStr
    }

    if (!hasImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName(fqNameStr)
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }

  public companion object {
    public const val MUTABLE_INT_STATE_OF: String = "mutableIntStateOf"
    public const val MUTABLE_LONG_STATE_OF: String = "mutableLongStateOf"
    public const val MUTABLE_FLOAT_STATE_OF: String = "mutableFloatStateOf"
    public const val MUTABLE_DOUBLE_STATE_OF: String = "mutableDoubleStateOf"
  }
}
