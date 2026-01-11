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

public enum class RuleCategory(public val displayName: String) {
  NAMING("Naming Conventions"),
  MODIFIER("Modifier Rules"),
  STATE("State Management"),
  PARAMETER("Parameter Rules"),
  COMPOSABLE("Composable Structure"),
  STRICTER("Stricter Rules"),
}

public enum class RuleSeverity {
  ERROR,
  WARNING,
  WEAK_WARNING,
  INFO,
}

public data class AnalysisContext(
  val file: KtFile,
  val isOnTheFly: Boolean = true,
)

public data class ComposeRuleViolation(
  val rule: ComposeRule,
  val element: PsiElement,
  val message: String,
  val tooltip: String? = null,
  val highlightType: ProblemHighlightType = ProblemHighlightType.WARNING,
  val quickFixes: List<LocalQuickFix> = emptyList(),
)

public interface ComposeRule {
  public val id: String

  public val name: String

  public val description: String

  public val category: RuleCategory

  public val severity: RuleSeverity

  public val enabledByDefault: Boolean
    get() = true

  public val documentationUrl: String?
    get() = null

  public fun analyzeFunction(function: KtNamedFunction, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  public fun analyzeProperty(property: KtProperty, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  public fun analyzeClass(ktClass: KtClass, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

  public fun analyzeElement(element: KtElement, context: AnalysisContext): List<ComposeRuleViolation> {
    return emptyList()
  }

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

public abstract class ComposableFunctionRule : ComposeRule {

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

  protected abstract fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

public abstract class PropertyRule : ComposeRule {

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

  protected abstract fun doAnalyze(
    property: KtProperty,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

public abstract class AnyFunctionRule : ComposeRule {

  public open val requiresComposable: Boolean = false

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

  protected abstract fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

public abstract class AnnotationClassRule : ComposeRule {

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

  protected abstract fun doAnalyze(
    ktClass: KtClass,
    context: AnalysisContext,
  ): List<ComposeRuleViolation>
}

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
