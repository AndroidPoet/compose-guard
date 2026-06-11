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
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault

/**
 * A `<Component>Defaults` object should have the same visibility as the composable it accompanies.
 * A public composable with a private defaults object prevents callers from reusing those defaults
 * (mrmans0n compose-rules: "ComponentDefaults object should match the composable visibility").
 */
public class ComponentDefaultsVisibilityRule : ComposableFunctionRule() {
  override val id: String = "ComponentDefaultsVisibility"
  override val name: String = "ComponentDefaults Visibility"
  override val description: String =
    "A ComponentDefaults object should have the same visibility as its composable."
  override val category: RuleCategory = RuleCategory.COMPOSABLE
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/rules/#componentdefaults-object-should-match-the-composable-visibility"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val name = function.name ?: return emptyList()
    val defaultsName = "${name}Defaults"

    val defaultsObject = PsiTreeUtil.findChildrenOfType(
      function.containingKtFile,
      KtObjectDeclaration::class.java,
    ).firstOrNull { it.name == defaultsName } ?: return emptyList()

    val functionVisibility = function.visibilityModifierTypeOrDefault()
    val objectVisibility = defaultsObject.visibilityModifierTypeOrDefault()

    if (functionVisibility == objectVisibility) {
      return emptyList()
    }

    return listOf(
      createViolation(
        element = defaultsObject.nameIdentifier ?: function.nameIdentifier ?: function,
        message = "'$defaultsName' is ${objectVisibility.value} but composable '$name' is ${functionVisibility.value}; visibility should match",
        tooltip = """
          A ComponentDefaults object should have the same visibility as the composable it
          accompanies. A public composable paired with a more restricted defaults object stops
          callers from reading and building on those defaults.

          Change '$defaultsName' to be ${functionVisibility.value} to match '$name'.
        """.trimIndent(),
        quickFixes = listOf(
          SuppressComposeRuleFix(id),
        ),
      ),
    )
  }
}
