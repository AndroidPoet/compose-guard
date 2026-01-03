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
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.quickfix.RenameComposableFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.AnnotationClassRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtClass

/**
 * Rule: Custom composable annotations should end with "Composable" suffix.
 *
 * When creating custom annotations that are meant to be used on @Composable functions
 * (like custom previews or markers), they should follow the naming convention of
 * ending with "Composable" to make their purpose clear.
 *
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#naming-composable-annotations-properly">Naming Composable Annotations</a>
 */
public class ComposableAnnotationNamingRule : AnnotationClassRule() {
  override val id: String = "ComposableAnnotationNaming"
  override val name: String = "Composable Annotation Naming"
  override val description: String = "Custom composable annotations should end with 'Composable' suffix."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-composable-annotations-properly"

  override fun doAnalyze(ktClass: KtClass, context: AnalysisContext): List<ComposeRuleViolation> {
    if (!isComposableAnnotation(ktClass)) {
      return emptyList()
    }

    val className = ktClass.name ?: return emptyList()

    if (!className.endsWith("Composable")) {
      val suggestedName = "${className}Composable"
      return listOf(
        createViolation(
          element = ktClass.nameIdentifier ?: ktClass,
          message = "Composable annotation '$className' should end with 'Composable' suffix",
          tooltip = """
            Custom annotations used on @Composable functions should follow
            the naming convention of ending with "Composable".

            This makes it clear that the annotation is related to Compose
            and should be used on composable functions.

            ❌ Bad:
            @Composable
            annotation class MyPreview

            ✅ Good:
            @Composable
            annotation class MyPreviewComposable

            Or for preview annotations:
            @Composable
            annotation class MyCustomPreview (if it includes @Preview)
          """.trimIndent(),
          quickFixes = listOf(
            RenameComposableFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }

    return emptyList()
  }

  private fun isComposableAnnotation(ktClass: KtClass): Boolean {
    return ktClass.annotationEntries.any { annotation ->
      val shortName = annotation.shortName?.asString()
      shortName == "Composable"
    }
  }
}
