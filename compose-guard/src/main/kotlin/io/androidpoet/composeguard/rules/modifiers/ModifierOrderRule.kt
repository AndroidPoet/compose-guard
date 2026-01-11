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
package io.androidpoet.composeguard.rules.modifiers

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.ReorderModifiersFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

public class ModifierOrderRule : ComposableFunctionRule() {
  override val id: String = "ModifierOrder"
  override val name: String = "Modifier Order Matters"
  override val description: String = "Modifiers are applied in order - clickable before padding ensures proper touch targets."
  override val category: RuleCategory = RuleCategory.MODIFIER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#modifier-order-matters"

  private val interactionModifiers = setOf(
    "clickable",
    "selectable",
    "toggleable",
    "combinedClickable",
  )

  private val boundReducingModifiers = setOf(
    "padding",
    "offset",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val dotExpressions = PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java)

    val processedChains = mutableSetOf<KtDotQualifiedExpression>()

    for (dotExpr in dotExpressions) {
      if (processedChains.any { PsiTreeUtil.isAncestor(it, dotExpr, false) }) continue

      if (!isModifierChain(dotExpr)) continue

      processedChains.add(dotExpr)

      val modifierCalls = extractModifierCalls(dotExpr)
      if (modifierCalls.isEmpty()) continue

      checkPaddingBeforeClickable(modifierCalls, dotExpr, violations)
    }

    return violations
  }

  private fun isModifierChain(dotExpr: KtDotQualifiedExpression): Boolean {
    var current: Any = dotExpr
    while (current is KtDotQualifiedExpression) {
      val receiver = current.receiverExpression
      if (receiver is KtDotQualifiedExpression) {
        current = receiver
      } else {
        val baseText = receiver.text
        return baseText == "modifier" || baseText == "Modifier"
      }
    }
    return false
  }

  private fun extractModifierCalls(dotExpr: KtDotQualifiedExpression): List<ModifierCall> {
    val calls = mutableListOf<ModifierCall>()

    fun traverse(expr: KtDotQualifiedExpression) {
      val receiver = expr.receiverExpression
      val selector = expr.selectorExpression

      if (receiver is KtDotQualifiedExpression) {
        traverse(receiver)
      } else if (receiver is KtCallExpression) {
        val callName = receiver.calleeExpression?.text
        if (callName != null) {
          calls.add(ModifierCall(callName, receiver))
        }
      }

      if (selector is KtCallExpression) {
        val callName = selector.calleeExpression?.text ?: return
        calls.add(ModifierCall(callName, selector))
      }
    }

    traverse(dotExpr)
    return calls
  }

  private fun checkPaddingBeforeClickable(
    calls: List<ModifierCall>,
    chainExpr: KtDotQualifiedExpression,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    for ((index, call) in calls.withIndex()) {
      if (call.name in interactionModifiers) {
        for (i in 0 until index) {
          if (calls[i].name in boundReducingModifiers) {
            violations.add(
              createViolation(
                element = calls[i].element,
                message = "'${calls[i].name}' before '${call.name}' reduces touch target area",
                tooltip = """
                  Modifier order matters! When '${calls[i].name}' comes before '${call.name}':
                  - The touch target is reduced by the padding amount
                  - Users may have difficulty tapping the element

                  Recommended order:
                  1. clickable/selectable (interaction handlers)
                  2. padding (visual spacing)

                  Example fix:
                  ❌ modifier.padding(16.dp).clickable { }
                  ✅ modifier.clickable { }.padding(16.dp)
                """.trimIndent(),
                quickFixes = listOf(
                  ReorderModifiersFix(calls[i].name, call.name),
                  SuppressComposeRuleFix(id),
                ),
              ),
            )
            break
          }
        }
      }
    }
  }

  private data class ModifierCall(
    val name: String,
    val element: KtCallExpression,
  )
}
