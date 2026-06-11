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
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

public class RenameParameterFix(
  private val suggestedName: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Rename parameter"

  override fun getName(): String = "Rename to '$suggestedName'"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val parameter = descriptor.psiElement as? KtParameter
      ?: descriptor.psiElement.parent as? KtParameter
      ?: return

    val nameIdentifier = parameter.nameIdentifier ?: return
    val oldName = parameter.name

    val factory = KtPsiFactory(project)

    // Rename the parameter's usages in the function body too. Renaming only the declaration
    // would leave dangling references to the old name, producing non-compiling code.
    val function = PsiTreeUtil.getParentOfType(parameter, KtNamedFunction::class.java)
    val body = function?.bodyBlockExpression ?: function?.bodyExpression
    if (oldName != null && oldName != suggestedName && body != null) {
      val references = PsiTreeUtil.findChildrenOfType(body, KtNameReferenceExpression::class.java)
        .filter { it.getReferencedName() == oldName }
      for (ref in references) {
        ref.replace(factory.createExpression(suggestedName))
      }
    }

    val newIdentifier = factory.createIdentifier(suggestedName)
    nameIdentifier.replace(newIdentifier)
  }
}
