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
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference

public class UseImmutableCollectionFix(
  private val currentType: String,
  private val suggestedType: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Use immutable collection"

  override fun getName(): String = "Replace with $suggestedType"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val typeRef = descriptor.psiElement as? KtTypeReference
      ?: descriptor.psiElement.parent as? KtTypeReference
      ?: return

    val factory = KtPsiFactory(project)
    val currentText = typeRef.text

    val newText = currentText.replaceFirst(currentType, suggestedType)
    val newTypeRef = factory.createType(newText)

    typeRef.replace(newTypeRef.typeElement!!)

    addImportIfNeeded(project, typeRef)
  }

  private fun addImportIfNeeded(project: Project, element: org.jetbrains.kotlin.psi.KtElement) {
    val file = element.containingKtFile
    val imports = file.importDirectives

    val fqNameStr = when (suggestedType) {
      "ImmutableList" -> "kotlinx.collections.immutable.ImmutableList"
      "ImmutableSet" -> "kotlinx.collections.immutable.ImmutableSet"
      "ImmutableMap" -> "kotlinx.collections.immutable.ImmutableMap"
      "PersistentList" -> "kotlinx.collections.immutable.PersistentList"
      "PersistentSet" -> "kotlinx.collections.immutable.PersistentSet"
      "PersistentMap" -> "kotlinx.collections.immutable.PersistentMap"
      else -> return
    }

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
    public fun forList(): UseImmutableCollectionFix =
      UseImmutableCollectionFix("List", "ImmutableList")

    public fun forSet(): UseImmutableCollectionFix =
      UseImmutableCollectionFix("Set", "ImmutableSet")

    public fun forMap(): UseImmutableCollectionFix =
      UseImmutableCollectionFix("Map", "ImmutableMap")
  }
}
