/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.rules.parameters

import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.AddExplicitParameterFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Rule: Make dependencies explicit (ViewModels, CompositionLocals).
 */
public class ExplicitDependenciesRule : ComposableFunctionRule() {
  override val id: String = "ExplicitDependencies"
  override val name: String = "Make Dependencies Explicit"
  override val description: String = "ViewModels and CompositionLocals acquired in body should be parameters."
  override val category: RuleCategory = RuleCategory.PARAMETER
  override val severity: RuleSeverity = RuleSeverity.INFO
  override val documentationUrl: String = "https://mrmans0n.github.io/compose-rules/latest/rules/#make-dependencies-explicit-viewmodels"

  private val implicitDependencyPatterns = setOf(
    "viewModel", "hiltViewModel", "koinViewModel",
    "LocalContext", "LocalConfiguration", "LocalDensity"
  )

  override fun doAnalyze(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()
    val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)
    val violations = mutableListOf<ComposeRuleViolation>()

    for (call in calls) {
      val calleeName = call.calleeExpression?.text ?: continue
      if (calleeName in implicitDependencyPatterns ||
          calleeName.endsWith(".current") ||
          calleeName.startsWith("Local")) {
        val paramType = inferParameterType(calleeName)
        violations.add(createViolation(
          element = call,
          message = "Consider making '$calleeName' an explicit parameter",
          tooltip = "Implicit dependencies make composables harder to test. Consider passing as parameter.",
          quickFixes = listOf(
            AddExplicitParameterFix(calleeName, paramType),
            SuppressComposeRuleFix(id),
          ),
        ))
      }
    }

    return violations
  }

  private fun inferParameterType(calleeName: String): String {
    return when {
      calleeName.contains("ViewModel") -> "ViewModel"
      calleeName == "LocalContext" || calleeName.endsWith("LocalContext.current") -> "Context"
      calleeName == "LocalConfiguration" -> "Configuration"
      calleeName == "LocalDensity" -> "Density"
      calleeName.startsWith("Local") -> calleeName.removePrefix("Local")
      else -> "Any"
    }
  }
}
