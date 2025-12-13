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
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a key parameter to LazyList items() or itemsIndexed() calls.
 *
 * Per Android's official documentation:
 * - Keys should be stable, unique, and Bundle-compatible
 * - Keys enable efficient recomposition and proper state preservation
 * - Keys allow animations to work correctly with animateItem()
 *
 * Function signatures:
 * - items(items: List<T>, key: ((T) -> Any)? = null, contentType: (T) -> Any? = null, itemContent: (T) -> Unit)
 * - itemsIndexed(items: List<T>, key: ((Int, T) -> Any)? = null, contentType: (Int, T) -> Any? = null, itemContent: (Int, T) -> Unit)
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/lists#item-keys">Item Keys</a>
 */
public class AddKeyParameterFix : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Add key parameter"

  override fun getName(): String = "Add key parameter for stable item identity"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val factory = KtPsiFactory(project)
    val calleeName = callExpr.calleeExpression?.text ?: return

    // Detect the item parameter name from the trailing lambda
    val itemParamName = detectItemParameterName(callExpr)

    // Build the key lambda based on function type
    val keyLambda = buildKeyLambda(calleeName, itemParamName)

    // Reconstruct the call with key parameter inserted in correct position
    val newCallText = buildCallWithKey(callExpr, calleeName, keyLambda)

    val newCall = factory.createExpression(newCallText) as KtCallExpression
    callExpr.replace(newCall)
  }

  /**
   * Detects the item parameter name from the trailing lambda.
   * e.g., `{ user -> UserItem(user) }` returns "user"
   * e.g., `{ index, item -> ... }` returns "item" (for itemsIndexed)
   */
  private fun detectItemParameterName(callExpr: KtCallExpression): String {
    val lambdaExpr = callExpr.lambdaArguments.firstOrNull()?.getLambdaExpression()
      ?: return "it"

    val params = lambdaExpr.valueParameters
    return when {
      params.isEmpty() -> "it"
      params.size == 1 -> params[0].name ?: "it"
      params.size == 2 -> params[1].name ?: "item" // For itemsIndexed: (index, item)
      else -> "it"
    }
  }

  /**
   * Builds the key lambda based on function type.
   */
  private fun buildKeyLambda(calleeName: String, itemParamName: String): String {
    return when (calleeName) {
      "itemsIndexed" -> {
        // itemsIndexed key signature: (Int, T) -> Any
        "key = { _, $itemParamName -> $itemParamName.id }"
      }
      else -> {
        // items key signature: (T) -> Any
        if (itemParamName == "it") {
          "key = { it.id }"
        } else {
          "key = { $itemParamName -> $itemParamName.id }"
        }
      }
    }
  }

  /**
   * Builds the new call expression with key parameter inserted in the correct position.
   *
   * Parameter order: items(items, key, contentType) { itemContent }
   */
  private fun buildCallWithKey(
    callExpr: KtCallExpression,
    calleeName: String,
    keyLambda: String,
  ): String {
    var itemsArg: String? = null
    var contentTypeArg: String? = null
    val otherArgs = mutableListOf<String>()

    // Process existing value arguments (excluding trailing lambda)
    for (arg in callExpr.valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argExpr = arg.getArgumentExpression() ?: continue
      val argValue = argExpr.text
      val isLambda = argExpr is KtLambdaExpression

      when {
        // Skip key if it exists (shouldn't happen but be safe)
        argName == "key" -> continue

        // Capture contentType to add after key
        argName == "contentType" -> {
          contentTypeArg = "contentType = $argValue"
        }

        // Skip itemContent - it should be the trailing lambda
        argName == "itemContent" -> continue

        // Named items argument
        argName == "items" && !isLambda -> {
          if (itemsArg == null) {
            itemsArg = "items = $argValue"
          }
        }

        // Positional first argument (must be items, and must NOT be a lambda)
        argName == null && itemsArg == null && !isLambda -> {
          itemsArg = "items = $argValue"
        }

        // Any other named argument (excluding lambdas which are likely itemContent)
        argName != null && !isLambda -> {
          otherArgs.add("$argName = $argValue")
        }

        // Skip any other lambdas - they're likely misplaced itemContent
      }
    }

    // Build the final arguments list in correct order: items, key, contentType, others
    val finalArgs = mutableListOf<String>()

    // 1. Items (required)
    if (itemsArg != null) {
      finalArgs.add(itemsArg)
    }

    // 2. Key (what we're adding)
    finalArgs.add(keyLambda)

    // 3. ContentType (if present)
    if (contentTypeArg != null) {
      finalArgs.add(contentTypeArg)
    }

    // 4. Any other args
    finalArgs.addAll(otherArgs)

    // Get trailing lambda
    val trailingLambda = callExpr.lambdaArguments.firstOrNull()?.text ?: "{}"

    return "$calleeName(${finalArgs.joinToString(", ")}) $trailingLambda"
  }
}
