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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds contentType parameter to LazyList item/items calls.
 */
public class AddContentTypeFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add contentType parameter"

  override fun getName(): String = "Add contentType to items"

  private val lazyItemFunctions = setOf("item", "items", "itemsIndexed", "stickyHeader")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val lazyListCall = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val factory = KtPsiFactory(project)

    // Find the lambda body of the LazyList
    val lambdaArgument = lazyListCall.lambdaArguments.firstOrNull()
      ?: lazyListCall.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
      ?: return

    val lambdaBody = when (lambdaArgument) {
      is KtLambdaExpression -> lambdaArgument.bodyExpression
      else -> (lambdaArgument as? com.intellij.psi.PsiElement)
        ?.let { PsiTreeUtil.findChildOfType(it, KtLambdaExpression::class.java) }
        ?.bodyExpression
    } ?: return

    // Find all item/items calls
    val itemCalls = PsiTreeUtil.findChildrenOfType(lambdaBody, KtCallExpression::class.java)
      .filter { it.calleeExpression?.text in lazyItemFunctions }

    var typeIndex = 0
    for (itemCall in itemCalls) {
      if (!hasContentTypeParameter(itemCall)) {
        addContentTypeToCall(factory, itemCall, typeIndex)
        typeIndex++
      }
    }
  }

  private fun hasContentTypeParameter(callExpression: KtCallExpression): Boolean {
    return callExpression.valueArguments.any { arg ->
      arg.getArgumentName()?.asName?.asString() == "contentType"
    }
  }

  private fun addContentTypeToCall(factory: KtPsiFactory, call: KtCallExpression, typeIndex: Int) {
    val calleeName = call.calleeExpression?.text ?: return
    val valueArguments = call.valueArguments
    val lambdaArguments = call.lambdaArguments

    val contentTypeName = when (typeIndex) {
      0 -> "header"
      1 -> "content"
      2 -> "footer"
      else -> "type$typeIndex"
    }

    when (calleeName) {
      "item", "stickyHeader" -> {
        // item { content } -> item(contentType = "header") { content }
        val trailingLambda = lambdaArguments.firstOrNull()?.text ?: "{}"
        val newCallText = "$calleeName(contentType = \"$contentTypeName\") $trailingLambda"
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
      "items", "itemsIndexed" -> {
        // items(list, key = {...}) { } -> items(list, key = {...}, contentType = { "type" }) { }
        val existingArgs = valueArguments.joinToString(", ") { it.text }
        val trailingLambda = lambdaArguments.firstOrNull()?.text ?: "{}"

        val contentTypeArg = if (calleeName == "itemsIndexed") {
          "contentType = { _, _ -> \"$contentTypeName\" }"
        } else {
          "contentType = { \"$contentTypeName\" }"
        }

        val newCallText = if (existingArgs.isNotEmpty()) {
          "$calleeName($existingArgs, $contentTypeArg) $trailingLambda"
        } else {
          "$calleeName($contentTypeArg) $trailingLambda"
        }

        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
    }
  }
}
