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

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a @Suppress annotation for a compose rule violation.
 * This is marked as low priority so the actual fix actions appear first.
 */
public class SuppressComposeRuleFix(
  private val ruleId: String,
) : LocalQuickFix, LowPriorityAction {

  override fun getFamilyName(): String = "Suppress compose rule"

  override fun getName(): String = "Suppress '$ruleId'"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val psiElement = descriptor.psiElement ?: return
    val element = findAnnotatableElement(psiElement) ?: return

    val existingSuppress = when (element) {
      is KtNamedFunction -> element.annotationEntries.find {
        it.shortName?.asString() == "Suppress"
      }
      is KtProperty -> element.annotationEntries.find {
        it.shortName?.asString() == "Suppress"
      }
      else -> null
    }

    val factory = KtPsiFactory(project)

    if (existingSuppress != null) {
      addToExistingSuppress(factory, existingSuppress)
    } else {
      addNewSuppressAnnotation(factory, element as KtAnnotated)
    }
  }

  private fun findAnnotatableElement(element: PsiElement): KtElement? {
    var current: PsiElement? = element
    while (current != null) {
      when (current) {
        is KtNamedFunction -> return current
        is KtProperty -> return current
      }
      current = current.parent
    }
    return null
  }

  private fun addNewSuppressAnnotation(factory: KtPsiFactory, element: KtAnnotated) {
    val annotation = factory.createAnnotationEntry("@Suppress(\"$ruleId\")")

    when (element) {
      is KtNamedFunction -> {
        val modifierList = element.modifierList
        if (modifierList != null) {
          modifierList.addBefore(annotation, modifierList.firstChild)
        } else {
          val newModifierList = factory.createModifierList("@Suppress(\"$ruleId\")")
          element.addBefore(newModifierList, element.funKeyword)
        }
      }
      is KtProperty -> {
        val modifierList = element.modifierList
        if (modifierList != null) {
          modifierList.addBefore(annotation, modifierList.firstChild)
        } else {
          element.addBefore(annotation, element.firstChild)
        }
      }
    }
  }

  private fun addToExistingSuppress(
    factory: KtPsiFactory,
    suppress: org.jetbrains.kotlin.psi.KtAnnotationEntry,
  ) {
    val valueArguments = suppress.valueArgumentList?.arguments ?: emptyList()
    val alreadyExists = valueArguments.any { arg ->
      val text = arg.getArgumentExpression()?.text?.trim('"') ?: ""
      text == ruleId
    }

    if (alreadyExists) return

    val existingArgs = valueArguments.joinToString(", ") {
      it.getArgumentExpression()?.text ?: ""
    }

    val newArgs = if (existingArgs.isEmpty()) {
      "\"$ruleId\""
    } else {
      "$existingArgs, \"$ruleId\""
    }

    val newAnnotation = factory.createAnnotationEntry("@Suppress($newArgs)")
    suppress.replace(newAnnotation)
  }
}
