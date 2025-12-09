/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that replaces Material 2 import with Material 3 equivalent.
 */
public class UseMaterial3Fix : LocalQuickFix {

  override fun getFamilyName(): String = "Migrate to Material 3"

  override fun getName(): String = "Replace with Material 3 import"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val import = descriptor.psiElement as? KtImportDirective ?: return
    val importPath = import.importedFqName?.asString() ?: return

    val newPath = convertToMaterial3(importPath) ?: return

    val factory = KtPsiFactory(project)
    val fqName = org.jetbrains.kotlin.name.FqName(newPath)
    val importPathNew = org.jetbrains.kotlin.resolve.ImportPath(fqName, import.isAllUnder)
    val newImport = factory.createImportDirective(importPathNew)

    import.replace(newImport)
  }

  private fun convertToMaterial3(importPath: String): String? {
    // Replace material with material3
    return when {
      importPath.startsWith("androidx.compose.material.icons.") -> {
        // Icons are shared, keep as is or update if needed
        importPath
      }
      importPath.startsWith("androidx.compose.material.") -> {
        importPath.replace("androidx.compose.material.", "androidx.compose.material3.")
      }
      else -> null
    }
  }
}
