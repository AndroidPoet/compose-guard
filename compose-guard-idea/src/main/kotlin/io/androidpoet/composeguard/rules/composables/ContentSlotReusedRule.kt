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
package io.androidpoet.composeguard.rules.composables

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposableLambda
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression

/**
 * Rule: Content slots should not be reused across different code paths.
 *
 * Invoking the same content slot lambda multiple times in different branches
 * or scopes of a composable function can cause the slot's internal state to
 * be lost or behave unexpectedly.
 *
 * Solution: Wrap the slot in `remember { movableContentOf { slot() } }` to
 * preserve internal state across different invocation sites.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#content-slots-should-not-be-reused">Content Slots Should Not Be Reused</a>
 */
public class ContentSlotReusedRule : ComposableFunctionRule() {
  override val id: String = "ContentSlotReused"
  override val name: String = "Content Slots Should Not Be Reused"
  override val description: String = "Content slot lambdas should not be invoked multiple times."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#content-slots-should-not-be-reused"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    // Find all @Composable lambda parameters (content slots)
    val contentSlots = function.valueParameters
      .filter { it.isComposableLambda() }
      .mapNotNull { it.name }
      .toSet()

    if (contentSlots.isEmpty()) return emptyList()

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Count invocations of each content slot
    val invocationCounts = mutableMapOf<String, MutableList<com.intellij.psi.PsiElement>>()

    // Find direct invocations: content()
    val callExpressions = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    for (call in callExpressions) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName in contentSlots) {
        invocationCounts.getOrPut(calleeName) { mutableListOf() }.add(call)
      }
    }

    // Find safe call invocations: content?.invoke() or content?.let { it() }
    val safeQualifiedExpressions = PsiTreeUtil.findChildrenOfType(body, KtSafeQualifiedExpression::class.java)
    for (safeExpr in safeQualifiedExpressions) {
      val receiverText = safeExpr.receiverExpression.text
      if (receiverText in contentSlots) {
        val selectorText = safeExpr.selectorExpression?.text ?: continue
        if (selectorText.startsWith("invoke") || selectorText.contains("{ it()")) {
          invocationCounts.getOrPut(receiverText) { mutableListOf() }.add(safeExpr)
        }
      }
    }

    // Find reference expressions that might be invocations in let/run/also blocks
    val nameReferences = PsiTreeUtil.findChildrenOfType(body, KtNameReferenceExpression::class.java)
    for (ref in nameReferences) {
      val refName = ref.getReferencedName()
      if (refName in contentSlots) {
        // Check if this is a direct call (parent is KtCallExpression with this as callee)
        val parent = ref.parent
        if (parent is KtCallExpression && parent.calleeExpression == ref) {
          // Already counted above
          continue
        }
        // Check for invoke() pattern
        if (parent?.text?.contains(".invoke()") == true) {
          invocationCounts.getOrPut(refName) { mutableListOf() }.add(ref)
        }
      }
    }

    // Report violations for slots invoked more than once
    for ((slotName, invocations) in invocationCounts) {
      if (invocations.size > 1) {
        // Report on the second and subsequent invocations
        for (i in 1 until invocations.size) {
          violations.add(
            createViolation(
              element = invocations[i],
              message = "Content slot '$slotName' is invoked multiple times",
              tooltip = """
                The content slot '$slotName' is being invoked ${invocations.size} times.
                This can cause the slot's internal state to be lost or behave unexpectedly.

                To preserve internal state, wrap the slot in movableContentOf:

                @Composable
                fun MyComposable($slotName: @Composable () -> Unit) {
                    val movableContent = remember {
                        movableContentOf { $slotName() }
                    }

                    if (condition) {
                        movableContent()
                    } else {
                        movableContent()  // State is preserved!
                    }
                }

                This ensures the content's state is maintained regardless of
                which code path invokes it.
              """.trimIndent(),
              quickFixes = listOf(SuppressComposeRuleFix(id)),
            ),
          )
        }
      }
    }

    return violations
  }
}
