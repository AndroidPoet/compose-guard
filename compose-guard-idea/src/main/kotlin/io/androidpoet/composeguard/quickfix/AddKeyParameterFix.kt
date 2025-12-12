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
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a key parameter to LazyList items() or itemsIndexed() calls.
 */
public class AddKeyParameterFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add key parameter"

  override fun getName(): String = "Add key = { it.id }"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val factory = KtPsiFactory(project)
    val calleeName = callExpr.calleeExpression?.text ?: return
    val valueArguments = callExpr.valueArguments
    val lambdaArguments = callExpr.lambdaArguments

    // Get the first argument (the items list)
    val firstArg = valueArguments.firstOrNull()?.text ?: return

    // Build the new call with key parameter
    val keyLambda = if (calleeName == "itemsIndexed") {
      "key = { index, item -> item.id }"
    } else {
      "key = { it.id }"
    }

    val trailingLambda = lambdaArguments.firstOrNull()?.text ?: "{}"

    val newCallText = "$calleeName($firstArg, $keyLambda) $trailingLambda"
    val newCall = factory.createExpression(newCallText) as KtCallExpression

    callExpr.replace(newCall)
  }
}
