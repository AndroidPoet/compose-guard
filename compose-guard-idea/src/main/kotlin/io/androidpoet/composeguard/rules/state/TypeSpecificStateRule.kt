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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseTypeSpecificStateFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeArgumentList

/**
 * Rule: Use type-specific mutableStateOf variants when possible.
 *
 * For primitive types like Int, Long, Float, Double, using type-specific
 * variants (mutableIntStateOf, mutableLongStateOf, etc.) eliminates
 * autoboxing on JVM and improves memory efficiency.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#use-mutablestateof-type-specific-variants-when-possible">Type-Specific mutableStateOf Variants</a>
 */
public class TypeSpecificStateRule : ComposableFunctionRule() {

  override val id: String = "TypeSpecificState"

  override val name: String = "Use Type-Specific State Variants"

  override val description: String = """
    Use type-specific mutableStateOf variants (mutableIntStateOf, etc.)
    for primitive types to avoid autoboxing overhead.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.WARNING

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#use-mutablestateof-type-specific-variants-when-possible"

  private val typeSpecificVariants = mapOf(
    "Int" to "mutableIntStateOf",
    "Long" to "mutableLongStateOf",
    "Float" to "mutableFloatStateOf",
    "Double" to "mutableDoubleStateOf",
  )

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Find all mutableStateOf calls
    val callExpressions = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    for (call in callExpressions) {
      val calleeName = call.calleeExpression?.text ?: continue

      if (calleeName == "mutableStateOf") {
        // Check for explicit type argument
        val typeArgumentList = call.typeArgumentList
        if (typeArgumentList != null) {
          val typeText = typeArgumentList.arguments.firstOrNull()?.text
          typeSpecificVariants[typeText]?.let { suggestedVariant ->
            violations.add(
              createViolation(
                element = call,
                message = "Use '$suggestedVariant' instead of 'mutableStateOf<$typeText>'",
                tooltip = """
                  For primitive type $typeText, use $suggestedVariant instead of mutableStateOf<$typeText>.
                  This avoids autoboxing overhead on JVM and improves memory efficiency.

                  Change:
                    mutableStateOf<$typeText>(initialValue)

                  To:
                    $suggestedVariant(initialValue)
                """.trimIndent(),
                quickFixes = listOf(
                  UseTypeSpecificStateFix(suggestedVariant),
                  SuppressComposeRuleFix(id),
                ),
              ),
            )
          }
        } else {
          // Try to infer type from the argument
          val argument = call.valueArguments.firstOrNull()?.getArgumentExpression()
          val argText = argument?.text
          if (argText != null) {
            val inferredType = inferPrimitiveType(argText)
            typeSpecificVariants[inferredType]?.let { suggestedVariant ->
              violations.add(
                createViolation(
                  element = call,
                  message = "Consider using '$suggestedVariant' for $inferredType values",
                  tooltip = """
                    For primitive type $inferredType, consider using $suggestedVariant
                    instead of mutableStateOf. This avoids autoboxing overhead.

                    Change:
                      mutableStateOf($argText)

                    To:
                      $suggestedVariant($argText)
                  """.trimIndent(),
                  quickFixes = listOf(
                    UseTypeSpecificStateFix(suggestedVariant),
                    SuppressComposeRuleFix(id),
                  ),
                ),
              )
            }
          }
        }
      }
    }

    return violations
  }

  private fun inferPrimitiveType(argText: String): String? {
    return when {
      argText.matches(Regex("""\d+L""")) -> "Long"
      argText.matches(Regex("""\d+\.\d*[fF]""")) -> "Float"
      argText.matches(Regex("""\d+\.\d*""")) -> "Double"
      argText.matches(Regex("""\d+""")) -> "Int"
      else -> null
    }
  }
}
