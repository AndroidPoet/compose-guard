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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Quick fix that makes a @Preview function private.
 */
public class MakePreviewPrivateFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Make private"

  override fun getName(): String = "Make preview function private"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val function = descriptor.psiElement as? KtNamedFunction
      ?: descriptor.psiElement.parent as? KtNamedFunction
      ?: return

    function.modifierList?.getModifier(KtTokens.PUBLIC_KEYWORD)?.delete()
    function.modifierList?.getModifier(KtTokens.INTERNAL_KEYWORD)?.delete()

    function.addModifier(KtTokens.PRIVATE_KEYWORD)
  }
}
