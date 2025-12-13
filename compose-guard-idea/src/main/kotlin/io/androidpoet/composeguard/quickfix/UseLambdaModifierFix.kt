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
 * Quick fix that converts a non-lambda modifier to its lambda-based alternative
 * for deferred state reads.
 *
 * Transforms:
 * ```kotlin
 * // offset(x, y) -> offset { IntOffset(x, y) }
 * Modifier.offset(x = scrollOffset.dp, y = 0.dp)
 * ```
 *
 * To:
 * ```kotlin
 * Modifier.offset { IntOffset(scrollOffset.roundToInt(), 0) }
 * ```
 *
 * Per Android's official guidance: "Defer state reads as long as possible
 * by wrapping them in lambda functions."
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/performance#defer-reads">Defer Reads</a>
 */
public class UseLambdaModifierFix(
  private val modifierName: String,
  private val lambdaVersion: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Use lambda-based modifier"

  override fun getName(): String = "Convert to $lambdaVersion"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val callExpr = element as? KtCallExpression
      ?: element.parent as? KtCallExpression
      ?: return

    val factory = KtPsiFactory(project)
    val args = callExpr.valueArguments

    when (modifierName) {
      "offset" -> convertOffset(factory, callExpr, args)
      "alpha" -> convertAlpha(factory, callExpr, args)
      "rotate" -> convertRotate(factory, callExpr, args)
      "scale" -> convertScale(factory, callExpr, args)
      else -> {
        // Generic conversion: wrap args in lambda
        val argsText = args.joinToString(", ") { it.getArgumentExpression()?.text ?: "" }
        val newCallText = "$modifierName { $argsText }"
        val newCall = factory.createExpression(newCallText)
        callExpr.replace(newCall)
      }
    }
  }

  /**
   * Converts offset(x, y) to offset { IntOffset(x, y) }
   */
  private fun convertOffset(factory: KtPsiFactory, callExpr: KtCallExpression, args: List<org.jetbrains.kotlin.psi.KtValueArgument>) {
    val xArg = args.find { it.getArgumentName()?.asName?.asString() == "x" }?.getArgumentExpression()?.text
      ?: args.getOrNull(0)?.getArgumentExpression()?.text
      ?: "0"
    val yArg = args.find { it.getArgumentName()?.asName?.asString() == "y" }?.getArgumentExpression()?.text
      ?: args.getOrNull(1)?.getArgumentExpression()?.text
      ?: "0"

    // Convert .dp values to pixels (user will need to adjust)
    val xPixels = convertDpToPixels(xArg)
    val yPixels = convertDpToPixels(yArg)

    val newCallText = "offset { IntOffset($xPixels, $yPixels) }"
    val newCall = factory.createExpression(newCallText)
    callExpr.replace(newCall)
  }

  /**
   * Converts alpha(value) to graphicsLayer { alpha = value }
   */
  private fun convertAlpha(factory: KtPsiFactory, callExpr: KtCallExpression, args: List<org.jetbrains.kotlin.psi.KtValueArgument>) {
    val alphaValue = args.firstOrNull()?.getArgumentExpression()?.text ?: "1f"
    val newCallText = "graphicsLayer { alpha = $alphaValue }"
    val newCall = factory.createExpression(newCallText)
    callExpr.replace(newCall)
  }

  /**
   * Converts rotate(degrees) to graphicsLayer { rotationZ = degrees }
   */
  private fun convertRotate(factory: KtPsiFactory, callExpr: KtCallExpression, args: List<org.jetbrains.kotlin.psi.KtValueArgument>) {
    val degrees = args.firstOrNull()?.getArgumentExpression()?.text ?: "0f"
    val newCallText = "graphicsLayer { rotationZ = $degrees }"
    val newCall = factory.createExpression(newCallText)
    callExpr.replace(newCall)
  }

  /**
   * Converts scale(value) to graphicsLayer { scaleX = value; scaleY = value }
   */
  private fun convertScale(factory: KtPsiFactory, callExpr: KtCallExpression, args: List<org.jetbrains.kotlin.psi.KtValueArgument>) {
    val scaleX = args.find { it.getArgumentName()?.asName?.asString() == "scaleX" }?.getArgumentExpression()?.text
    val scaleY = args.find { it.getArgumentName()?.asName?.asString() == "scaleY" }?.getArgumentExpression()?.text
    val uniformScale = args.firstOrNull()?.getArgumentExpression()?.text

    val newCallText = if (scaleX != null || scaleY != null) {
      "graphicsLayer { scaleX = ${scaleX ?: "1f"}; scaleY = ${scaleY ?: "1f"} }"
    } else {
      val scale = uniformScale ?: "1f"
      "graphicsLayer { scaleX = $scale; scaleY = $scale }"
    }

    val newCall = factory.createExpression(newCallText)
    callExpr.replace(newCall)
  }

  /**
   * Converts a Dp expression to pixels representation for IntOffset.
   * User may need to adjust with proper density conversion.
   */
  private fun convertDpToPixels(dpValue: String): String {
    return when {
      dpValue.endsWith(".dp") -> {
        val value = dpValue.removeSuffix(".dp")
        "$value.dp.roundToPx()"
      }
      dpValue.contains(".dp") -> {
        // Complex expression like (offset).dp
        dpValue.replace(".dp", ".dp.roundToPx()")
      }
      else -> {
        // Already a numeric value, assume pixels or Int
        "$dpValue.toInt()"
      }
    }
  }
}
