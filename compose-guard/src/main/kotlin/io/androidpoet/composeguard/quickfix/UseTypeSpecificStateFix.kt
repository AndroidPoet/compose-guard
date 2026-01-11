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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

public class UseTypeSpecificStateFix(
  private val targetFunction: String,
) : LocalQuickFix, HighPriorityAction {

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
