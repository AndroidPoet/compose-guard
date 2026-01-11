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
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

public class AddModifierDefaultValueFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add default value"

  override fun getName(): String = "Add '= Modifier' default value"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val parameter = descriptor.psiElement as? KtParameter
      ?: descriptor.psiElement.parent as? KtParameter
      ?: return

    if (parameter.hasDefaultValue()) return

    val factory = KtPsiFactory(project)
    val typeRef = parameter.typeReference?.text ?: "Modifier"
    val newParam = factory.createParameter("${parameter.name}: $typeRef = Modifier")

    parameter.replace(newParam)
  }
}
