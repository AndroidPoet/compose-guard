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
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.parents

public class MoveModifierToRootFix(
  private val modifierName: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Move modifier to root"

  override fun getName(): String = "Move '$modifierName' to root layout"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement

    val modifierArg = when (element) {
      is KtValueArgument -> element
      else -> element.parent as? KtValueArgument
    } ?: return

    val nestedCall = modifierArg.parent?.parent as? KtCallExpression ?: return

    val modifierExpr = modifierArg.getArgumentExpression()?.text ?: return

    val rootCall = findRootContentEmitter(nestedCall) ?: return

    val factory = KtPsiFactory(project)

    addModifierToCall(rootCall, modifierExpr, factory)

    removeModifierFromCall(nestedCall, modifierArg, factory)
  }

  private fun findRootContentEmitter(nestedCall: KtCallExpression): KtCallExpression? {
    for (parent in nestedCall.parents) {
      if (parent is KtLambdaArgument) {
        val parentCall = parent.parent as? KtCallExpression ?: continue
        val callName = parentCall.calleeExpression?.text ?: continue

        if (callName.first().isUpperCase()) {
          return parentCall
        }
      }
    }
    return null
  }

  private fun addModifierToCall(call: KtCallExpression, modifierExpr: String, factory: KtPsiFactory) {
    val callName = call.calleeExpression?.text ?: return
    val existingArgs = call.valueArgumentList
    val lambdaArg = call.lambdaArguments.firstOrNull()

    val hasModifier = existingArgs?.arguments?.any {
      it.getArgumentName()?.asName?.asString() == "modifier"
    } == true

    if (hasModifier) {
      return
    }

    val newArgsText = if (existingArgs != null && existingArgs.arguments.isNotEmpty()) {
      val existingArgsText = existingArgs.arguments.joinToString(", ") { it.text }
      "$existingArgsText, modifier = $modifierExpr"
    } else {
      "modifier = $modifierExpr"
    }

    val lambdaText = lambdaArg?.getLambdaExpression()?.text ?: ""
    val newCallText = if (lambdaText.isNotEmpty()) {
      "$callName($newArgsText) $lambdaText"
    } else {
      "$callName($newArgsText)"
    }

    val newCall = factory.createExpression(newCallText)
    call.replace(newCall)
  }

  private fun removeModifierFromCall(call: KtCallExpression, modifierArg: KtValueArgument, factory: KtPsiFactory) {
    val callName = call.calleeExpression?.text ?: return
    val existingArgs = call.valueArgumentList
    val lambdaArg = call.lambdaArguments.firstOrNull()

    val remainingArgs = existingArgs?.arguments
      ?.filter { it != modifierArg }
      ?.map { it.text }
      ?: emptyList()

    val lambdaText = lambdaArg?.getLambdaExpression()?.text ?: ""

    val newCallText = when {
      remainingArgs.isEmpty() && lambdaText.isNotEmpty() -> "$callName $lambdaText"
      remainingArgs.isEmpty() -> "$callName()"
      lambdaText.isNotEmpty() -> "$callName(${remainingArgs.joinToString(", ")}) $lambdaText"
      else -> "$callName(${remainingArgs.joinToString(", ")})"
    }

    val newCall = factory.createExpression(newCallText)
    call.replace(newCall)
  }
}
