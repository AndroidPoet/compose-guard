/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPsiFactory

public class UseMaterial3Fix : LocalQuickFix, HighPriorityAction {

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
    return when {
      importPath.startsWith("androidx.compose.material.icons.") -> {
        importPath
      }
      importPath.startsWith("androidx.compose.material.") -> {
        importPath.replace("androidx.compose.material.", "androidx.compose.material3.")
      }
      else -> null
    }
  }
}
