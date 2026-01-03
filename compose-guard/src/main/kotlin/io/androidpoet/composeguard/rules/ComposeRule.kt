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
package io.androidpoet.composeguard.rules

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Category of compose rules.
 */
public enum class RuleCategory(public val displayName: String) {
  NAMING("Naming Conventions"),
  MODIFIER("Modifier Rules"),
  STATE("State Management"),
  PARAMETER("Parameter Rules"),
  COMPOSABLE("Composable Structure"),
  STRICTER("Stricter Rules"),
}

/**
 * Severity level for rule violations.
 */
public enum class RuleSeverity {
  ERROR,
  WARNING,
  WEAK_WARNING,
  INFO,
}

/**
 * Context for rule analysis containing project and file information.
 */
public data class AnalysisContext(
  val file: KtFile,
  val isOnTheFly: Boolean = true,
)

/**
 * Represents a violation of a compose rule.
 */
public data class ComposeRuleViolation(
  val rule: ComposeRule,
  val element: PsiElement,
  val message: String,
  val tooltip: String? = null,
  val highlightType: ProblemHighlightType = ProblemHighlightType.WARNING,
  val quickFixes: List<LocalQuickFix> = emptyList(),
)

/**
 * Base interface for all compose rules.
 *
 * Each rule defines:
 * - Metadata (id, name, description, category, severity)
 * - Detection logic (analyze methods)
 * - Quick fixes for violations
 */
public interface ComposeRule {
  /**
   * Unique identifier for this rule.
   * Example: "ComposableNaming", "ModifierRequired"
   */
  public val id: String

  /**
   * Display name for this rule.
   * Example: "Composable Naming Convention"
   */
  public val name: String

  /**
   * Full description of this rule explaining what it checks and why.
   */
  public val description: String

  /**
   * Category this rule belongs to.
   */
  public val category: RuleCategory

  /**
   * Severity level for violations of this rule.
   */
  public val severity: RuleSeverity

  /**
   * Whether this rule is enabled by default.
   */
  public val enabledByDefault: Boolean
    get() = true

  /**
   * URL to documentation for this rule (optional).
   */
  public val documentationUrl: String?
    get() = null

  /**
   * Analyze a composable function for violations.
   * Override this for function-level rules.
   *
   * @param function The composable function to analyze
   * @param context Analysis context
   * @return List of violations found
   */
  public fun analyzeFunction(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  /**
   * Analyze a property for violations.
   * Override this for property-level rules (e.g., CompositionLocal naming).
   *
   * @param property The property to analyze
   * @param context Analysis context
   * @return List of violations found
   */
  public fun analyzeProperty(property: KtProperty, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  /**
   * Analyze a class for violations.
   * Override this for class-level rules (e.g., annotation class naming).
   *
   * @param ktClass The class to analyze
   * @param context Analysis context
   * @return List of violations found
   */
  public fun analyzeClass(ktClass: KtClass, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  /**
   * Analyze any element for violations.
   * Override this for element-agnostic rules.
   *
   * @param element The element to analyze
   * @param context Analysis context
   * @return List of violations found
   */
  public fun analyzeElement(element: KtElement, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  /**
   * Create a violation with this rule's default settings.
   * @param severity Override the rule's default severity for this specific violation
   */
  public fun createViolation(
    element: PsiElement,
    message: String,
    tooltip: String? = null,
    quickFixes: List<LocalQuickFix> = emptyList(),
    severity: RuleSeverity? = null,
  ): ComposeRuleViolation {
    val effectiveSeverity = severity ?: this.severity
    val highlightType = when (effectiveSeverity) {
      RuleSeverity.ERROR -> ProblemHighlightType.ERROR
      RuleSeverity.WARNING -> ProblemHighlightType.WARNING
      RuleSeverity.WEAK_WARNING -> ProblemHighlightType.WEAK_WARNING
      RuleSeverity.INFO -> ProblemHighlightType.INFORMATION
    }
    return ComposeRuleViolation(
      rule = this,
      element = element,
      message = message,
      tooltip = tooltip ?: description,
      highlightType = highlightType,
      quickFixes = quickFixes,
    )
  }
}

/**
 * Base class for function-level compose rules.
 * Provides common utilities for analyzing composable functions.
 */
public abstract class ComposableFunctionRule : ComposeRule {

  /**
   * Check if a function should be analyzed by this rule.
   * Override to add custom filtering logic.
   */
  protected open fun shouldAnalyze(function: KtNamedFunction): Boolean {
    return true
  }

  final override fun analyzeFunction(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    if (!shouldAnalyze(function)) {
      return emptyList()
    }
    return doAnalyze(function, context)
  }

  /**
   * Perform the actual analysis.
   * Subclasses implement this method to check for violations.
   */
  protected abstract fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

/**
 * Base class for property-level compose rules.
 */
public abstract class PropertyRule : ComposeRule {

  /**
   * Check if a property should be analyzed by this rule.
   */
  protected open fun shouldAnalyze(property: KtProperty): Boolean {
    return true
  }

  final override fun analyzeProperty(
    property: KtProperty,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    if (!shouldAnalyze(property)) {
      return emptyList()
    }
    return doAnalyze(property, context)
  }

  /**
   * Perform the actual analysis.
   */
  protected abstract fun doAnalyze(
    property: KtProperty,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

/**
 * Base class for rules that analyze ANY function (not just composables).
 * Use this for rules like AvoidComposed that need to check Modifier extension functions.
 */
public abstract class AnyFunctionRule : ComposeRule {

  /**
   * Whether this rule requires composable annotation.
   * Override to return false for rules that analyze non-composable functions.
   */
  public open val requiresComposable: Boolean = false

  /**
   * Check if a function should be analyzed by this rule.
   */
  protected open fun shouldAnalyze(function: KtNamedFunction): Boolean {
    return true
  }

  final override fun analyzeFunction(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    if (!shouldAnalyze(function)) {
      return emptyList()
    }
    return doAnalyze(function, context)
  }

  /**
   * Perform the actual analysis.
   */
  protected abstract fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

/**
 * Base class for rules that analyze annotation classes.
 * Use this for rules like ComposableAnnotationNaming.
 */
public abstract class AnnotationClassRule : ComposeRule {

  /**
   * Check if a class should be analyzed by this rule.
   */
  protected open fun shouldAnalyze(ktClass: KtClass): Boolean {
    return ktClass.isAnnotation()
  }

  final override fun analyzeClass(
    ktClass: KtClass,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    if (!shouldAnalyze(ktClass)) {
      return emptyList()
    }
    return doAnalyze(ktClass, context)
  }

  /**
   * Perform the actual analysis.
   */
  protected abstract fun doAnalyze(
    ktClass: KtClass,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

/**
 * Checks if a rule is suppressed for the given element.
 * Looks for @Suppress annotations on the element itself and its parent function/property/class.
 *
 * @param element The PSI element to check
 * @param ruleId The rule ID to check for suppression
 * @return true if the rule is suppressed, false otherwise
 */
public fun isSuppressed(element: PsiElement, ruleId: String): Boolean {
  var current: PsiElement? = element

  while (current != null) {
    if (current is KtAnnotated) {
      val suppressAnnotation = current.annotationEntries.find {
        it.shortName?.asString() == "Suppress"
      }
      if (suppressAnnotation != null) {
        val valueArguments = suppressAnnotation.valueArgumentList?.arguments ?: emptyList()
        for (arg in valueArguments) {
          val argText = arg.getArgumentExpression()?.text?.trim('"') ?: continue
          if (argText == ruleId) {
            return true
          }
        }
      }
    }

    if (current is KtAnnotated) {
      val prevSibling = current.prevSibling
      if (prevSibling is PsiComment) {
        val commentText = prevSibling.text
        if (commentText.contains("noinspection", ignoreCase = true) &&
          commentText.contains(ruleId)
        ) {
          return true
        }
      }
    }

    if (current is KtFile) break

    current = current.parent
  }

  return false
}
