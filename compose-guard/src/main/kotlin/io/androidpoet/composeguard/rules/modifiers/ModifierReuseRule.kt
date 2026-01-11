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
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.getModifierParameter
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtValueArgument

public class ModifierReuseRule : ComposableFunctionRule() {

  override val id: String = "ModifierReuse"

  override val name: String = "Don't Re-use Modifiers"

  override val description: String = """
    The same modifier should not be passed to multiple layout nodes.
    Each layout should have its own modifier instance.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.MODIFIER

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#dont-re-use-modifiers"

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val modifierParam = function.getModifierParameter() ?: return emptyList()
    val modifierName = modifierParam.name ?: return emptyList()

    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    val modifierNames = mutableSetOf(modifierName)

    val properties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)
    for (prop in properties) {
      val initializerText = prop.initializer?.text ?: continue
      if (modifierNames.any { initializerText.startsWith(it) }) {
        prop.name?.let { modifierNames.add(it) }
      }
    }

    val assignments = PsiTreeUtil.findChildrenOfType(body, KtBinaryExpression::class.java)
    for (assignment in assignments) {
      if (assignment.operationReference.text == "=") {
        val rightText = assignment.right?.text ?: continue
        if (modifierNames.any { rightText.startsWith(it) }) {
          val leftText = assignment.left?.text
          if (leftText != null && !leftText.contains(".")) {
            modifierNames.add(leftText)
          }
        }
      }
    }

    val valueArgs = PsiTreeUtil.findChildrenOfType(body, KtValueArgument::class.java)
    val modifierUsages = valueArgs.filter { arg ->
      val argName = arg.getArgumentName()?.asName?.asString()
      val argValue = arg.getArgumentExpression()?.text ?: return@filter false

      val normalizedArgValue = argValue.replace(Regex("\\s+"), "")

      (argName == "modifier" || argName == null) &&
        modifierNames.any { name ->
          normalizedArgValue == name || normalizedArgValue.startsWith("$name.")
        }
    }

    if (modifierUsages.size > 1) {
      return listOf(
        createViolation(
          element = modifierParam.nameIdentifier ?: modifierParam,
          message = "Modifier '$modifierName' is passed to ${modifierUsages.size} different layouts",
          tooltip = """
            The modifier parameter is being used in ${modifierUsages.size} different places.
            Modifiers should only be applied to the root layout of a composable.

            Reusing modifiers can cause unexpected behavior because:
            - Modifier effects are designed for a single node
            - State inside modifiers may conflict

            Solution: Only pass the modifier to the root layout, and use
            Modifier (with capital M) to create new chains for other layouts.

            ❌ Bad:
            Column(modifier = modifier) {
                Text("A", modifier = modifier)  // Reused!
            }

            ✅ Good:
            Column(modifier = modifier) {
                Text("A", modifier = Modifier.padding(8.dp))
            }
          """.trimIndent(),
          quickFixes = listOf(
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    return emptyList()
  }
}
