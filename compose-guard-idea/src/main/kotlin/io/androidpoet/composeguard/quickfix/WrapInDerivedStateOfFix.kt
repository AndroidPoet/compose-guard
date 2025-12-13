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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Quick fix that wraps an expression in remember { derivedStateOf { } }.
 *
 * Use derivedStateOf when:
 * - The input state changes MORE frequently than the derived output
 * - Classic example: scrollState.firstVisibleItemIndex > 0
 *   - Scroll offset changes on every frame
 *   - But "show button" only changes when crossing the threshold (true ↔ false)
 *
 * derivedStateOf caches the result and only triggers recomposition when
 * the DERIVED value actually changes, not when the input changes.
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/side-effects#derivedstateof">derivedStateOf</a>
 */
public class WrapInDerivedStateOfFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Wrap in derivedStateOf"

  override fun getName(): String = "Wrap in remember { derivedStateOf { } }"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement

    // Find the expression to wrap
    val expression: KtExpression = when (element) {
      is KtExpression -> element
      is KtProperty -> element.initializer
      else -> {
        element.parent as? KtExpression
          ?: (element.parent as? KtProperty)?.initializer
      }
    } ?: return

    // Get the file BEFORE replacing (element becomes invalid after replace)
    val file = expression.containingKtFile

    val factory = KtPsiFactory(project)
    val exprText = expression.text

    // Wrap in remember { derivedStateOf { } }
    val wrappedExpr = factory.createExpression("remember { derivedStateOf { $exprText } }")
    expression.replace(wrappedExpr)

    // Add imports
    addImportIfNeeded(project, file, "androidx.compose.runtime.remember")
    addImportIfNeeded(project, file, "androidx.compose.runtime.derivedStateOf")
  }

  private fun addImportIfNeeded(
    project: Project,
    file: org.jetbrains.kotlin.psi.KtFile,
    importFqName: String,
  ) {
    val imports = file.importDirectives

    val hasImport = imports.any {
      it.importedFqName?.asString() == importFqName
    }

    if (!hasImport) {
      val factory = KtPsiFactory(project)
      val fqName = org.jetbrains.kotlin.name.FqName(importFqName)
      val importPath = org.jetbrains.kotlin.resolve.ImportPath(fqName, false)
      val importDirective = factory.createImportDirective(importPath)
      file.importList?.add(importDirective)
    }
  }
}
