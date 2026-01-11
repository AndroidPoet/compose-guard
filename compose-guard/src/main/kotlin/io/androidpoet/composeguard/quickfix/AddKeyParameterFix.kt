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

    val itemParamName = detectItemParameterName(callExpr)

    val keyLambda = buildKeyLambda(calleeName, itemParamName)

    val newCallText = buildCallWithKey(callExpr, calleeName, keyLambda)

    val newCall = factory.createExpression(newCallText) as KtCallExpression
    callExpr.replace(newCall)
  }

  private fun detectItemParameterName(callExpr: KtCallExpression): String {
    val lambdaExpr = callExpr.lambdaArguments.firstOrNull()?.getLambdaExpression()
      ?: return "it"

    val params = lambdaExpr.valueParameters
    return when {
      params.isEmpty() -> "it"
      params.size == 1 -> params[0].name ?: "it"
      params.size == 2 -> params[1].name ?: "item"
      else -> "it"
    }
  }

  private fun buildKeyLambda(calleeName: String, itemParamName: String): String {
    return when (calleeName) {
      "itemsIndexed" -> {
        "key = { _, $itemParamName -> $itemParamName.id }"
      }
      else -> {
        if (itemParamName == "it") {
          "key = { it.id }"
        } else {
          "key = { $itemParamName -> $itemParamName.id }"
        }
      }
    }
  }

  private fun buildCallWithKey(
    callExpr: KtCallExpression,
    calleeName: String,
    keyLambda: String,
  ): String {
    var itemsArg: String? = null
    var contentTypeArg: String? = null
    val otherArgs = mutableListOf<String>()

    for (arg in callExpr.valueArguments) {
      val argName = arg.getArgumentName()?.asName?.asString()
      val argExpr = arg.getArgumentExpression() ?: continue
      val argValue = argExpr.text
      val isLambda = argExpr is KtLambdaExpression

      when {
        argName == "key" -> continue

        argName == "contentType" -> {
          contentTypeArg = "contentType = $argValue"
        }

        argName == "itemContent" -> continue

        argName == "items" && !isLambda -> {
          if (itemsArg == null) {
            itemsArg = "items = $argValue"
          }
        }

        argName == null && itemsArg == null && !isLambda -> {
          itemsArg = "items = $argValue"
        }

        argName != null && !isLambda -> {
          otherArgs.add("$argName = $argValue")
        }

      }
    }

    val finalArgs = mutableListOf<String>()

    if (itemsArg != null) {
      finalArgs.add(itemsArg)
    }

    finalArgs.add(keyLambda)

    if (contentTypeArg != null) {
      finalArgs.add(contentTypeArg)
    }

    finalArgs.addAll(otherArgs)

    val trailingLambda = callExpr.lambdaArguments.firstOrNull()?.text ?: "{}"

    return "$calleeName(${finalArgs.joinToString(", ")}) $trailingLambda"
  }
}
