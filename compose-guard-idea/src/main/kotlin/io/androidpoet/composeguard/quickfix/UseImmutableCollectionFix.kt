/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference

/**
 * Quick fix that replaces standard collection with immutable variant.
 */
public class UseImmutableCollectionFix(
  private val currentType: String,
  private val suggestedType: String,
) : LocalQuickFix {

  override fun getFamilyName(): String = "Use immutable collection"

  override fun getName(): String = "Replace with $suggestedType"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val typeRef = descriptor.psiElement as? KtTypeReference
      ?: descriptor.psiElement.parent as? KtTypeReference
      ?: return

    val factory = KtPsiFactory(project)
    val currentText = typeRef.text

    // Replace the collection type while preserving type parameters
    val newText = currentText.replaceFirst(currentType, suggestedType)
    val newTypeRef = factory.createType(newText)

    typeRef.replace(newTypeRef.typeElement!!)

    // Add import if needed
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
