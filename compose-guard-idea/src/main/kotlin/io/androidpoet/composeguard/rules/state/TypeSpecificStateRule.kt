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

  // Basic primitive type mappings
  private val typeSpecificVariants = mapOf(
    "Int" to "mutableIntStateOf",
    "Long" to "mutableLongStateOf",
    "Float" to "mutableFloatStateOf",
    "Double" to "mutableDoubleStateOf",
  )

  // Extended mappings for primitive collections (from androidx.collection)
  private val collectionVariants = mapOf(
    // Primitive Lists
    "IntList" to "mutableIntListOf",
    "LongList" to "mutableLongListOf",
    "FloatList" to "mutableFloatListOf",
    "DoubleList" to "mutableDoubleListOf",
    // Primitive Sets
    "IntSet" to "mutableIntSetOf",
    "LongSet" to "mutableLongSetOf",
    "FloatSet" to "mutableFloatSetOf",
    "DoubleSet" to "mutableDoubleSetOf",
    // Primitive-to-Primitive Maps
    "IntIntMap" to "mutableIntIntMapOf",
    "IntLongMap" to "mutableIntLongMapOf",
    "IntFloatMap" to "mutableIntFloatMapOf",
    "IntDoubleMap" to "mutableIntDoubleMapOf",
    "LongIntMap" to "mutableLongIntMapOf",
    "LongLongMap" to "mutableLongLongMapOf",
    "LongFloatMap" to "mutableLongFloatMapOf",
    "LongDoubleMap" to "mutableLongDoubleMapOf",
    "FloatIntMap" to "mutableFloatIntMapOf",
    "FloatLongMap" to "mutableFloatLongMapOf",
    "FloatFloatMap" to "mutableFloatFloatMapOf",
    "FloatDoubleMap" to "mutableFloatDoubleMapOf",
    // Primitive-to-Object Maps
    "IntObjectMap" to "mutableIntObjectMapOf",
    "LongObjectMap" to "mutableLongObjectMapOf",
    "FloatObjectMap" to "mutableFloatObjectMapOf",
    // Object-to-Primitive Maps
    "ObjectIntMap" to "mutableObjectIntMapOf",
    "ObjectLongMap" to "mutableObjectLongMapOf",
    "ObjectFloatMap" to "mutableObjectFloatMapOf",
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

      // Check for primitive collection factories
      if (calleeName in setOf("mutableListOf", "mutableSetOf", "mutableMapOf")) {
        checkCollectionFactory(call, calleeName, violations)
        continue
      }

      // Check for mutableStateOf with primitive types
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
      argText.matches(Regex("""-?\d+L""")) -> "Long"
      argText.matches(Regex("""-?\d+\.\d*[fF]""")) -> "Float"
      argText.matches(Regex("""-?\d+\.\d+""")) -> "Double"
      argText.matches(Regex("""-?\d+""")) -> "Int"
      else -> null
    }
  }

  /**
   * Checks if using a generic collection factory when a primitive-specific one exists.
   * For example: mutableListOf<Int>() should be mutableIntListOf()
   */
  private fun checkCollectionFactory(
    call: KtCallExpression,
    calleeName: String,
    violations: MutableList<ComposeRuleViolation>,
  ) {
    val typeArgumentList = call.typeArgumentList ?: return
    val typeArgs = typeArgumentList.arguments.map { it.text }

    val suggestion = when (calleeName) {
      "mutableListOf" -> {
        when (typeArgs.firstOrNull()) {
          "Int" -> "mutableIntListOf"
          "Long" -> "mutableLongListOf"
          "Float" -> "mutableFloatListOf"
          "Double" -> "mutableDoubleListOf"
          else -> null
        }
      }
      "mutableSetOf" -> {
        when (typeArgs.firstOrNull()) {
          "Int" -> "mutableIntSetOf"
          "Long" -> "mutableLongSetOf"
          "Float" -> "mutableFloatSetOf"
          "Double" -> "mutableDoubleSetOf"
          else -> null
        }
      }
      "mutableMapOf" -> {
        if (typeArgs.size == 2) {
          val keyType = typeArgs[0]
          val valueType = typeArgs[1]
          collectionVariants["${keyType}${valueType}Map"]
        } else null
      }
      else -> null
    }

    if (suggestion != null) {
      val typeArgsStr = typeArgs.joinToString(", ")
      violations.add(
        createViolation(
          element = call,
          message = "Consider using '$suggestion' instead of '$calleeName<$typeArgsStr>'",
          tooltip = """
            For primitive types, using type-specific collection factories avoids
            autoboxing overhead and improves memory efficiency.

            Change:
              $calleeName<$typeArgsStr>()

            To:
              $suggestion()

            These primitive collections are from androidx.collection library.
          """.trimIndent(),
          quickFixes = listOf(SuppressComposeRuleFix(id)),
        ),
      )
    }
  }
}
