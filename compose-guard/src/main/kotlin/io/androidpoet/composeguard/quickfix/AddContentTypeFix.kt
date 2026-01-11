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
import org.jetbrains.kotlin.psi.KtValueArgument

public class AddContentTypeFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add contentType parameter"

  override fun getName(): String = "Add contentType to items"

  private val lazyListFunctions = setOf(
    "LazyColumn",
    "LazyRow",
    "LazyVerticalGrid",
    "LazyHorizontalGrid",
    "LazyVerticalStaggeredGrid",
    "LazyHorizontalStaggeredGrid",
  )

  private val lazyItemFunctions = setOf("item", "items", "itemsIndexed", "stickyHeader")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val factory = KtPsiFactory(project)

    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: PsiTreeUtil.getParentOfType(element, KtCallExpression::class.java)
      ?: return

    val calleeName = callExpr.calleeExpression?.text ?: return

    if (calleeName in lazyItemFunctions) {
      addContentTypeToSingleCall(factory, callExpr)
      return
    }

    if (calleeName in lazyListFunctions) {
      addContentTypeToAllItems(factory, callExpr)
      return
    }
  }

  private fun addContentTypeToSingleCall(factory: KtPsiFactory, call: KtCallExpression) {
    if (hasContentTypeParameter(call)) return

    val calleeName = call.calleeExpression?.text ?: return

    val parentLambda = PsiTreeUtil.getParentOfType(call, KtLambdaExpression::class.java)
    val siblingCalls = parentLambda?.bodyExpression?.let { body ->
      PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
        .filter { it.calleeExpression?.text in lazyItemFunctions }
    } ?: listOf(call)

    val index = siblingCalls.indexOf(call).coerceAtLeast(0)
    val totalCount = siblingCalls.size

    val contentTypeName = generateContentTypeName(calleeName, index, totalCount)
    replaceCallWithContentType(factory, call, calleeName, contentTypeName)
  }

  private fun addContentTypeToAllItems(factory: KtPsiFactory, lazyListCall: KtCallExpression) {
    val lambdaArgument = lazyListCall.lambdaArguments.firstOrNull()
      ?: lazyListCall.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
      ?: return

    val lambdaBody = when (lambdaArgument) {
      is KtLambdaExpression -> lambdaArgument.bodyExpression
      else -> (lambdaArgument as? com.intellij.psi.PsiElement)
        ?.let { PsiTreeUtil.findChildOfType(it, KtLambdaExpression::class.java) }
        ?.bodyExpression
    } ?: return

    val itemCalls = PsiTreeUtil.findChildrenOfType(lambdaBody, KtCallExpression::class.java)
      .filter { it.calleeExpression?.text in lazyItemFunctions }

    val callsNeedingContentType = itemCalls.filter { !hasContentTypeParameter(it) }

    for ((index, itemCall) in callsNeedingContentType.withIndex()) {
      val calleeName = itemCall.calleeExpression?.text ?: continue
      val contentTypeName = generateContentTypeName(calleeName, index, callsNeedingContentType.size)
      replaceCallWithContentType(factory, itemCall, calleeName, contentTypeName)
    }
  }

  private fun replaceCallWithContentType(
    factory: KtPsiFactory,
    call: KtCallExpression,
    calleeName: String,
    contentTypeName: String,
  ) {
    val valueArguments = call.valueArguments
    val lambdaArguments = call.lambdaArguments
    val trailingLambda = lambdaArguments.firstOrNull()?.text ?: "{}"

    when (calleeName) {
      "item", "stickyHeader" -> {
        val newCallText = buildItemCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
      "items" -> {
        val newCallText = buildItemsCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
      "itemsIndexed" -> {
        val newCallText = buildItemsIndexedCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
    }
  }

  private fun buildItemCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda

    for ((index, arg) in valueArguments.withIndex()) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        if (argName == "content" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        if (isLambdaArg) {
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          continue
        }
        when (index) {
          0 -> namedArgs.add("key = $argValue")
          else -> namedArgs.add(argValue)
        }
      }
    }

    namedArgs.add("contentType = \"$contentTypeName\"")

    return "$calleeName(${namedArgs.joinToString(", ")}) $effectiveTrailingLambda"
  }

  private fun buildItemsCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda
    var positionalIndex = 0

    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        if (argName == "itemContent" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        if (isLambdaArg && positionalIndex > 0) {
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          positionalIndex++
          continue
        }
        when (positionalIndex) {
          0 -> namedArgs.add("items = $argValue")
          else -> namedArgs.add(argValue)
        }
        positionalIndex++
      }
    }

    namedArgs.add("contentType = { _ -> \"$contentTypeName\" }")

    return "$calleeName(${namedArgs.joinToString(", ")}) $effectiveTrailingLambda"
  }

  private fun buildItemsIndexedCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda
    var positionalIndex = 0

    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        if (argName == "itemContent" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        if (isLambdaArg && positionalIndex > 0) {
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          positionalIndex++
          continue
        }
        when (positionalIndex) {
          0 -> namedArgs.add("items = $argValue")
          else -> namedArgs.add(argValue)
        }
        positionalIndex++
      }
    }

    namedArgs.add("contentType = { _, _ -> \"$contentTypeName\" }")

    return "$calleeName(${namedArgs.joinToString(", ")}) $effectiveTrailingLambda"
  }

  private fun hasContentTypeParameter(callExpression: KtCallExpression): Boolean {
    return callExpression.valueArguments.any { arg ->
      arg.getArgumentName()?.asName?.asString() == "contentType"
    }
  }

  private fun generateContentTypeName(
    calleeName: String,
    index: Int,
    totalCount: Int,
  ): String {
    return "contentType${index + 1}"
  }
}
