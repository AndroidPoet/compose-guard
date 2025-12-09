/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.naming

import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.PropertyRule
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isCompositionLocal
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Rule: CompositionLocal should be named with "Local" prefix.
 */
public class CompositionLocalNamingRule : PropertyRule() {
  override val id: String = "CompositionLocalNaming"
  override val name: String = "CompositionLocal Naming"
  override val description: String = "CompositionLocal instances should start with 'Local' prefix."
  override val category: RuleCategory = RuleCategory.NAMING
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#naming-compositionlocals-properly"

  override fun shouldAnalyze(property: KtProperty): Boolean = property.isCompositionLocal()

  override fun doAnalyze(property: KtProperty, context: AnalysisContext): List<ComposeRuleViolation> {
    val name = property.name ?: return emptyList()
    if (!name.startsWith("Local")) {
      return listOf(createViolation(
        element = property.nameIdentifier ?: property,
        message = "CompositionLocal '$name' should start with 'Local' prefix",
        tooltip = "Rename to 'Local$name' to follow naming convention."
      ))
    }
    return emptyList()
  }
}
