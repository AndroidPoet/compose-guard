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

public class MultipreviewNamingRule : AnnotationClassRule() {
  override val id: String = "MultipreviewNaming"
  override val name: String = "Multipreview Naming"
  override val description: String = "Multipreview annotations should be named with a 'Preview' reference."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WEAK_WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-multipreview-annotations-properly"

  override fun doAnalyze(ktClass: KtClass, context: AnalysisContext): List<ComposeRuleViolation> {
    // A multipreview is an ANNOTATION CLASS meta-annotated with @Preview. A composable function
    // that merely stacks several @Preview annotations is a normal preview (named per the Preview
    // rule), so flagging functions here was a false positive that contradicted PreviewNaming.
    val previewCount = ktClass.annotationEntries.count { it.shortName?.asString() == "Preview" }
    if (previewCount < 1) return emptyList()

    val name = ktClass.name ?: return emptyList()
    if (!name.contains("Preview", ignoreCase = true)) {
      val suggestedName = "Preview$name"
      return listOf(
        createViolation(
          element = ktClass.nameIdentifier ?: ktClass,
          message = "Multipreview annotation '$name' should reference 'Preview' in its name",
          tooltip = "Annotations grouping multiple @Preview should be named like 'PreviewScreenSizes' or 'FontScalePreviews'.",
          quickFixes = listOf(
            RenameComposableFix(suggestedName),
            SuppressComposeRuleFix(id),
          ),
        ),
      )
    }
    return emptyList()
  }
}
