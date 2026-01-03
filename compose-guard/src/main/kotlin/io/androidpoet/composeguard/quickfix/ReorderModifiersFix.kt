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
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that reorders modifier chain to put clickable/selectable before padding/offset.
 * This ensures proper touch target sizing.
 *
 * Example transformation:
 * ❌ modifier.padding(16.dp).clickable { }
 * ✅ modifier.clickable { }.padding(16.dp)
 */
public class ReorderModifiersFix(
  private val boundReducingModifier: String,
  private val interactionModifier: String,
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Reorder modifiers"

  override fun getName(): String = "Move '$interactionModifier' before '$boundReducingModifier'"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as? KtCallExpression ?: return

    val modifierChain = findModifierChain(element) ?: return

    val modifierCalls = extractModifierCallsWithText(modifierChain)
    if (modifierCalls.size < 2) return

    val base = findChainBase(modifierChain) ?: return

    val reorderedCalls = reorderModifiers(modifierCalls)

    val newChainText = buildChainText(base, reorderedCalls)

    val factory = KtPsiFactory(project)
    val newChain = factory.createExpression(newChainText)
    modifierChain.replace(newChain)
  }

  private fun findModifierChain(element: KtCallExpression): KtDotQualifiedExpression? {
    var current = element.parent
    var lastDotExpr: KtDotQualifiedExpression? = null

    while (current is KtDotQualifiedExpression) {
      lastDotExpr = current
      current = current.parent
    }

    return lastDotExpr
  }

  private fun findChainBase(chain: KtDotQualifiedExpression): String? {
    var current: Any = chain
    while (current is KtDotQualifiedExpression) {
      val receiver = current.receiverExpression
      if (receiver is KtDotQualifiedExpression) {
        current = receiver
      } else {
        return receiver.text
      }
    }
    return null
  }

  private fun extractModifierCallsWithText(chain: KtDotQualifiedExpression): List<ModifierCallInfo> {
    val calls = mutableListOf<ModifierCallInfo>()

    fun traverse(expr: KtDotQualifiedExpression) {
      val receiver = expr.receiverExpression
      val selector = expr.selectorExpression

      if (receiver is KtDotQualifiedExpression) {
        traverse(receiver)
      }

      if (selector is KtCallExpression) {
        val callName = selector.calleeExpression?.text ?: return
        calls.add(ModifierCallInfo(callName, selector.text))
      }
    }

    traverse(chain)
    return calls
  }

  private fun reorderModifiers(calls: List<ModifierCallInfo>): List<ModifierCallInfo> {
    val interactionModifiers = setOf("clickable", "selectable", "toggleable", "combinedClickable")
    val boundReducingModifiers = setOf("padding", "offset")

    val result = calls.toMutableList()

    var modified = true
    while (modified) {
      modified = false
      for (i in 0 until result.size - 1) {
        val current = result[i]
        val next = result[i + 1]

        if (current.name in boundReducingModifiers && next.name in interactionModifiers) {
          result[i] = next
          result[i + 1] = current
          modified = true
          break
        }
      }
    }

    return result
  }

  private fun buildChainText(base: String, calls: List<ModifierCallInfo>): String {
    val builder = StringBuilder(base)
    for (call in calls) {
      builder.append(".").append(call.fullText)
    }
    return builder.toString()
  }

  private data class ModifierCallInfo(
    val name: String,
    val fullText: String,
  )
}
