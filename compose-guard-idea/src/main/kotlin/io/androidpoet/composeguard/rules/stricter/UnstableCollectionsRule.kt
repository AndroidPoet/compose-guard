/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.stricter

import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.quickfix.UseImmutableCollectionFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isStandardCollection
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Avoid using unstable collections as parameters.
 */
public class UnstableCollectionsRule : ComposableFunctionRule() {
  override val id: String = "UnstableCollections"
  override val name: String = "Avoid Unstable Collections"
  override val description: String = "Use ImmutableList/PersistentList instead of List for stable parameters."
  override val category: RuleCategory = RuleCategory.STRICTER
  override val severity: RuleSeverity = RuleSeverity.WARNING
  override val enabledByDefault: Boolean = true
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#avoid-using-unstable-collections"

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()

    for (param in function.valueParameters) {
      val typeText = param.typeReference?.text ?: continue

      // Extract the base collection type (before <T>)
      val baseType = typeText.substringBefore("<").trim()

      if (baseType.isStandardCollection()) {
        val suggestion = when (baseType) {
          "List" -> "ImmutableList"
          "Set" -> "ImmutableSet"
          "Map" -> "ImmutableMap"
          else -> "immutable equivalent"
        }

        val quickFix = when (baseType) {
          "List" -> UseImmutableCollectionFix.forList()
          "Set" -> UseImmutableCollectionFix.forSet()
          "Map" -> UseImmutableCollectionFix.forMap()
          else -> null
        }

        violations.add(createViolation(
          element = param.typeReference ?: param,
          message = "Parameter '$typeText' uses unstable collection interface",
          tooltip = """
            Standard collection interfaces (List, Set, Map) may be backed by mutable
            implementations, making them unstable for compose.

            Consider using $suggestion from kotlinx-collections-immutable:
            - ImmutableList<T> instead of List<T>
            - ImmutableSet<T> instead of Set<T>
            - ImmutableMap<K,V> instead of Map<K,V>

            Or annotate the composable with @Stable if you know the collection won't change.
          """.trimIndent(),
          quickFixes = listOfNotNull(quickFix, SuppressComposeRuleFix(id)),
        ))
      }
    }

    return violations
  }
}
