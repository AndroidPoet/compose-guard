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
package io.androidpoet.composeguard.rules.stricter

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseMaterial3Fix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposeRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Don't use Material 2.
 */
public class Material2Rule : ComposeRule {
  override val id: String = "Material2Usage"
  override val name: String = "Don't Use Material 2"
  override val description: String = "Use Material 3 instead of Material 2 for modern theming."
  override val category: RuleCategory = RuleCategory.STRICTER
  override val severity: RuleSeverity = RuleSeverity.INFO
  override val enabledByDefault: Boolean = true
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#dont-use-material-2"

  private val material2Packages = setOf(
    "androidx.compose.material.",
    "androidx.compose.material.icons.",
  )

  private val material3Package = "androidx.compose.material3"

  override fun analyzeFunction(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    return analyzeFile(context.file)
  }

  override fun analyzeElement(element: KtElement, context: AnalysisContext): List<ComposeRuleViolation> {
    return if (element is KtFile) analyzeFile(element) else emptyList()
  }

  private fun analyzeFile(file: KtFile): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val imports = file.importDirectives

    for (import in imports) {
      val importPath = import.importedFqName?.asString() ?: continue

      // Skip if it's Material 3
      if (importPath.startsWith(material3Package)) continue

      // Check for Material 2 imports (but not icons, they're shared)
      if (importPath.startsWith("androidx.compose.material.") &&
        !importPath.startsWith("androidx.compose.material.icons.")
      ) {
        violations.add(
          createViolation(
            element = import,
            message = "Using Material 2 component from '$importPath'",
            tooltip = "Consider migrating to Material 3 (androidx.compose.material3) for modern theming and Material You support.",
          ),
        )
      }
    }

    return violations
  }

  private fun createViolation(element: KtImportDirective, message: String, tooltip: String): ComposeRuleViolation {
    return ComposeRuleViolation(
      rule = this,
      element = element,
      message = message,
      tooltip = tooltip,
      highlightType = com.intellij.codeInspection.ProblemHighlightType.INFORMATION,
      quickFixes = listOf(
        UseMaterial3Fix(),
        SuppressComposeRuleFix(id),
      ),
    )
  }
}
