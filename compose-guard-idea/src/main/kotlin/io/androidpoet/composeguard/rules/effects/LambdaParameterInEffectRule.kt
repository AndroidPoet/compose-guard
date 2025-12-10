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
package io.androidpoet.composeguard.rules.effects

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.AddLambdaAsEffectKeyFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseRememberUpdatedStateFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isSuppressed
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Lambda parameters should not be directly used in restartable effects.
 *
 * When a lambda parameter is used directly inside LaunchedEffect, DisposableEffect,
 * or other restartable effects without being included as a key, the effect may
 * capture a stale reference to the lambda.
 *
 * Solutions:
 * 1. Add the lambda as a key to the effect
 * 2. Wrap with rememberUpdatedState for effects that shouldn't restart
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#lambdas-used-in-restartable-effects-should-be-checked">Lambda Parameters in Effects</a>
 */
public class LambdaParameterInEffectRule : ComposableFunctionRule() {
  override val id: String = "LambdaParameterInEffect"
  override val name: String = "Lambda Parameters in Restartable Effects"
  override val description: String = "Lambda parameters in effects should be keyed or wrapped in rememberUpdatedState."
  override val category: RuleCategory = RuleCategory.STATE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#lambdas-used-in-restartable-effects-should-be-checked"

  // Effects that restart based on keys
  private val restartableEffects = setOf(
    "LaunchedEffect",
    "DisposableEffect",
    "produceState",
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    // Check if suppressed at function level
    if (isSuppressed(function, id)) {
      return emptyList()
    }

    val violations = mutableListOf<ComposeRuleViolation>()

    // Find all lambda parameters
    val lambdaParams = function.valueParameters.filter { param ->
      val typeText = param.typeReference?.text ?: return@filter false
      typeText.contains("->") || typeText.contains("Function")
    }.mapNotNull { it.name }

    if (lambdaParams.isEmpty()) return emptyList()

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Find all effect calls
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName !in restartableEffects) continue

      // Get the effect keys (first N arguments before the trailing lambda)
      val effectKeys = call.valueArguments
        .filter { it.getArgumentExpression() !is KtLambdaExpression }
        .mapNotNull { it.getArgumentExpression()?.text }
        .toSet()

      // Get the effect body (trailing lambda)
      val effectLambda = call.lambdaArguments.firstOrNull()?.getLambdaExpression()
        ?: call.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
        ?: continue

      // Find references to lambda parameters inside the effect
      val references = PsiTreeUtil.findChildrenOfType(
        effectLambda.bodyExpression,
        KtNameReferenceExpression::class.java,
      )

      for (ref in references) {
        val refName = ref.getReferencedName()
        if (refName in lambdaParams && refName !in effectKeys) {
          // Check if it's wrapped in rememberUpdatedState
          if (!isWrappedInRememberUpdatedState(ref, body)) {
            violations.add(
              createViolation(
                element = ref,
                message = "Lambda parameter '$refName' used in $calleeName without being keyed",
                tooltip = """
                  The lambda parameter '$refName' is used inside $calleeName but is not
                  included as a key. This can lead to capturing stale references.

                  Option 1 - Add as effect key (effect restarts when lambda changes):
                  $calleeName($refName) {
                      $refName()
                  }

                  Option 2 - Use rememberUpdatedState (effect doesn't restart):
                  val current${refName.replaceFirstChar { it.uppercase() }} by rememberUpdatedState($refName)
                  $calleeName(Unit) {
                      current${refName.replaceFirstChar { it.uppercase() }}()
                  }

                  Choose based on whether you want the effect to restart when
                  the lambda parameter changes.
                """.trimIndent(),
                quickFixes = listOf(
                  AddLambdaAsEffectKeyFix(refName, calleeName),
                  UseRememberUpdatedStateFix(refName),
                  SuppressComposeRuleFix(id),
                ),
              ),
            )
          }
        }
      }
    }

    return violations
  }

  private fun isWrappedInRememberUpdatedState(
    reference: KtNameReferenceExpression,
    functionBody: com.intellij.psi.PsiElement,
  ): Boolean {
    val refName = reference.getReferencedName()

    // Look for: val currentX by rememberUpdatedState(x) or similar
    val properties = PsiTreeUtil.findChildrenOfType(functionBody, org.jetbrains.kotlin.psi.KtProperty::class.java)

    for (prop in properties) {
      val initializer = prop.initializer ?: prop.delegateExpression ?: continue
      val initText = initializer.text

      if (initText.contains("rememberUpdatedState") && initText.contains(refName)) {
        // Check if the reference is actually using the remembered version
        val propName = prop.name ?: continue
        val refParent = reference.parent?.text ?: ""
        if (refParent.contains(propName)) {
          return true
        }
      }
    }

    return false
  }
}
