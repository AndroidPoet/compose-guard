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

/**
 * Quick fix that adds contentType parameter to LazyList item/items calls.
 *
 * Parameter signatures:
 * - item(key: Any? = null, contentType: Any? = null, content: () -> Unit)
 * - items(items: List<T>, key: ((T) -> Any)? = null, contentType: (T) -> Any? = null, itemContent: (T) -> Unit)
 * - itemsIndexed(items: List<T>, key: ((Int, T) -> Any)? = null, contentType: (Int, T) -> Any? = null, itemContent: (Int, T) -> Unit)
 */
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

    // Try to find the call expression
    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: PsiTreeUtil.getParentOfType(element, KtCallExpression::class.java)
      ?: return

    val calleeName = callExpr.calleeExpression?.text ?: return

    // Case 1: Clicked on a single item/items call - fix just this one
    if (calleeName in lazyItemFunctions) {
      addContentTypeToSingleCall(factory, callExpr)
      return
    }

    // Case 2: Clicked on LazyColumn/LazyRow - fix all items inside
    if (calleeName in lazyListFunctions) {
      addContentTypeToAllItems(factory, callExpr)
      return
    }
  }

  /**
   * Add contentType to a single item/items call.
   */
  private fun addContentTypeToSingleCall(factory: KtPsiFactory, call: KtCallExpression) {
    if (hasContentTypeParameter(call)) return

    val calleeName = call.calleeExpression?.text ?: return

    // Try to determine position in parent for smart naming
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

  /**
   * Add contentType to all items inside a LazyList.
   */
  private fun addContentTypeToAllItems(factory: KtPsiFactory, lazyListCall: KtCallExpression) {
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

    // Collect items that need contentType
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
        // item(key = x) { } -> item(key = x, contentType = "type") { }
        // contentType for item is Any?, not a lambda
        val newCallText = buildItemCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
      "items" -> {
        // items(list, key = { }) { } -> items(items = list, key = { }, contentType = { _ -> "type" }) { }
        // contentType for items is (T) -> Any?, a single-param lambda
        val newCallText = buildItemsCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
      "itemsIndexed" -> {
        // itemsIndexed(list) { } -> itemsIndexed(items = list, contentType = { _, _ -> "type" }) { }
        // contentType for itemsIndexed is (Int, T) -> Any?, a two-param lambda
        val newCallText = buildItemsIndexedCall(calleeName, valueArguments, contentTypeName, trailingLambda)
        val newCall = factory.createExpression(newCallText) as KtCallExpression
        call.replace(newCall)
      }
    }
  }

  /**
   * Build call for item/stickyHeader: contentType is Any?, not a lambda
   * Signature: item(key: Any? = null, contentType: Any? = null, content: () -> Unit)
   */
  private fun buildItemCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda

    // Process existing arguments, converting to named if needed
    for ((index, arg) in valueArguments.withIndex()) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        // Skip 'content' named argument - it should be trailing lambda
        if (argName == "content" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        // For positional args: check if it's a lambda (content, not key)
        // Lambda as first positional arg is likely content passed incorrectly, not key
        if (isLambdaArg) {
          // This is likely the content lambda passed as positional arg
          // Move it to trailing lambda position if we don't have one
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          // Skip adding it as a named arg - it's content, not key
          continue
        }
        // First positional arg for item is 'key'
        when (index) {
          0 -> namedArgs.add("key = $argValue")
          else -> namedArgs.add(argValue) // shouldn't happen
        }
      }
    }

    // Add contentType
    namedArgs.add("contentType = \"$contentTypeName\"")

    return "$calleeName(${namedArgs.joinToString(", ")}) $effectiveTrailingLambda"
  }

  /**
   * Build call for items: contentType is (T) -> Any?, a single-param lambda
   * Signature: items(items: List<T>, key: ((T) -> Any)? = null, contentType: (T) -> Any? = null, itemContent: (T) -> Unit)
   */
  private fun buildItemsCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda
    var positionalIndex = 0

    // Process existing arguments
    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        // Skip 'itemContent' named argument - it should be trailing lambda
        if (argName == "itemContent" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        // For items(), positional order is: items, key, contentType, itemContent
        // If we see a lambda without a name at position 1+, it's likely itemContent (misplaced)
        // Don't assume it's a key - that would create invalid code like key = { Text(...) }
        if (isLambdaArg && positionalIndex > 0) {
          // Lambda at position 1+ without a name is likely itemContent, not key
          // Move it to trailing lambda position
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          // Skip - don't add as a named arg
          positionalIndex++
          continue
        }
        // Positional args: only treat first non-lambda as 'items'
        when (positionalIndex) {
          0 -> namedArgs.add("items = $argValue")
          else -> namedArgs.add(argValue) // preserve other positional args as-is
        }
        positionalIndex++
      }
    }

    // Add contentType as lambda with single parameter
    namedArgs.add("contentType = { _ -> \"$contentTypeName\" }")

    return "$calleeName(${namedArgs.joinToString(", ")}) $effectiveTrailingLambda"
  }

  /**
   * Build call for itemsIndexed: contentType is (Int, T) -> Any?, a two-param lambda
   * Signature: itemsIndexed(items: List<T>, key: ((Int, T) -> Any)? = null, contentType: (Int, T) -> Any? = null, itemContent: (Int, T) -> Unit)
   */
  private fun buildItemsIndexedCall(
    calleeName: String,
    valueArguments: List<KtValueArgument>,
    contentTypeName: String,
    trailingLambda: String,
  ): String {
    val namedArgs = mutableListOf<String>()
    var effectiveTrailingLambda = trailingLambda
    var positionalIndex = 0

    // Process existing arguments
    for (arg in valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: continue
      val isLambdaArg = arg.getArgumentExpression() is KtLambdaExpression

      if (argName != null) {
        // Skip 'itemContent' named argument - it should be trailing lambda
        if (argName == "itemContent" && isLambdaArg) {
          effectiveTrailingLambda = argValue
          continue
        }
        namedArgs.add("$argName = $argValue")
      } else {
        // For itemsIndexed(), positional order is: items, key, contentType, itemContent
        // If we see a lambda without a name at position 1+, it's likely itemContent (misplaced)
        // Don't assume it's a key - that would create invalid code like key = { Text(...) }
        if (isLambdaArg && positionalIndex > 0) {
          // Lambda at position 1+ without a name is likely itemContent, not key
          // Move it to trailing lambda position
          if (trailingLambda == "{}") {
            effectiveTrailingLambda = argValue
          }
          // Skip - don't add as a named arg
          positionalIndex++
          continue
        }
        // Positional args: only treat first non-lambda as 'items'
        when (positionalIndex) {
          0 -> namedArgs.add("items = $argValue")
          else -> namedArgs.add(argValue) // preserve other positional args as-is
        }
        positionalIndex++
      }
    }

    // Add contentType as lambda with two parameters (index, item)
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
    // Use simple numbered content types: contentType1, contentType2, etc.
    // User can rename to meaningful names after applying the fix
    return "contentType${index + 1}"
  }
}
