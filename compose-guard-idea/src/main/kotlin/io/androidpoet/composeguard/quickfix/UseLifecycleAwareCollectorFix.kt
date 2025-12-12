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
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that replaces collectAsState() with collectAsStateWithLifecycle().
 */
public class UseLifecycleAwareCollectorFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Use lifecycle-aware collector"

  override fun getName(): String = "Replace with collectAsStateWithLifecycle()"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement

    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val factory = KtPsiFactory(project)

    // Get the parent dot qualified expression (e.g., flow.collectAsState())
    val dotExpr = callExpr.parent as? KtDotQualifiedExpression ?: return

    // Get the receiver (e.g., flow)
    val receiver = dotExpr.receiverExpression.text

    // Get the arguments
    val args = callExpr.valueArguments.joinToString(", ") { it.text }

    // Build the new call
    val newCallText = if (args.isNotEmpty()) {
      "$receiver.collectAsStateWithLifecycle($args)"
    } else {
      "$receiver.collectAsStateWithLifecycle()"
    }

    val newExpr = factory.createExpression(newCallText)
    dotExpr.replace(newExpr)

    // Add import
    addImportIfNeeded(project, callExpr)
  }

  private fun addImportIfNeeded(
    project: Project,
    element: org.jetbrains.kotlin.psi.KtElement,
  ) {
    val file = element.containingKtFile
    val imports = file.importDirectives

    val fqNameStr = "androidx.lifecycle.compose.collectAsStateWithLifecycle"

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
}
