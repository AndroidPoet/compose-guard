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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Changes a `<Component>Defaults` object's visibility to match its composable, resolving a
 * [io.androidpoet.composeguard.rules.composables.ComponentDefaultsVisibilityRule] violation.
 */
public class MatchDefaultsVisibilityFix(
  private val targetVisibility: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Match composable visibility"

  override fun getName(): String = "Make defaults object $targetVisibility"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val obj = element as? KtObjectDeclaration
      ?: element.parent as? KtObjectDeclaration
      ?: PsiTreeUtil.getParentOfType(element, KtObjectDeclaration::class.java)
      ?: return

    // Drop any explicit visibility modifier, then add the target one (public is the default and
    // needs no modifier).
    obj.modifierList?.getModifier(KtTokens.PRIVATE_KEYWORD)?.delete()
    obj.modifierList?.getModifier(KtTokens.INTERNAL_KEYWORD)?.delete()
    obj.modifierList?.getModifier(KtTokens.PROTECTED_KEYWORD)?.delete()
    obj.modifierList?.getModifier(KtTokens.PUBLIC_KEYWORD)?.delete()

    val token = when (targetVisibility) {
      "internal" -> KtTokens.INTERNAL_KEYWORD
      "private" -> KtTokens.PRIVATE_KEYWORD
      "protected" -> KtTokens.PROTECTED_KEYWORD
      else -> null
    }
    if (token != null) {
      obj.addModifier(token)
    }
  }
}
