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
package io.androidpoet.composeguard.annotator

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.AnyFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.rules.isCompositionLocal
import io.androidpoet.composeguard.rules.isSuppressed
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Annotator that provides real-time highlighting for compose rule violations.
 * This runs as the user types and provides immediate feedback.
 */
public class ComposeGuardAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is KtNamedFunction -> annotateFunction(element, holder)
      is KtProperty -> annotateProperty(element, holder)
    }
  }

  private fun annotateFunction(function: KtNamedFunction, holder: AnnotationHolder) {
    val isComposable = function.isComposable()

    val file = function.containingKtFile
    val context = AnalysisContext(file, isOnTheFly = true)
    val enabledRules = ComposeRuleRegistry.getEnabledRules()

    for (rule in enabledRules) {
      try {
        val shouldRun = when {
          rule is AnyFunctionRule && !rule.requiresComposable -> true
          isComposable -> true
          else -> false
        }

        if (!shouldRun) continue

        val violations = rule.analyzeFunction(function, context)
        for (violation in violations) {
          if (isSuppressed(violation.element, rule.id)) continue
          createAnnotation(violation, holder)
        }
      } catch (e: Exception) {
      }
    }
  }

  private fun annotateProperty(property: KtProperty, holder: AnnotationHolder) {
    if (!property.isCompositionLocal()) {
      return
    }

    val file = property.containingKtFile
    val context = AnalysisContext(file, isOnTheFly = true)
    val enabledRules = ComposeRuleRegistry.getEnabledRules()

    for (rule in enabledRules) {
      try {
        val violations = rule.analyzeProperty(property, context)
        for (violation in violations) {
          if (isSuppressed(violation.element, rule.id)) continue
          createAnnotation(violation, holder)
        }
      } catch (e: Exception) {
      }
    }
  }

  private fun createAnnotation(violation: ComposeRuleViolation, holder: AnnotationHolder) {
    val severity = mapHighlightTypeToSeverity(violation.highlightType)
    val message = "[${violation.rule.id}] ${violation.message}"

    holder.newAnnotation(severity, message)
      .range(violation.element)
      .tooltip(buildTooltip(violation))
      .highlightType(violation.highlightType)
      .create()
  }

  private fun mapHighlightTypeToSeverity(type: ProblemHighlightType): HighlightSeverity {
    return when (type) {
      ProblemHighlightType.ERROR -> HighlightSeverity.ERROR
      ProblemHighlightType.WARNING -> HighlightSeverity.WARNING
      ProblemHighlightType.WEAK_WARNING -> HighlightSeverity.WEAK_WARNING
      ProblemHighlightType.INFORMATION -> HighlightSeverity.INFORMATION
      else -> HighlightSeverity.WARNING
    }
  }

  private fun buildTooltip(violation: ComposeRuleViolation): String {
    return buildString {
      append("<html><body>")
      append("<b>${violation.rule.name}</b><br/>")
      append("<br/>")
      append(violation.tooltip ?: violation.message)

      violation.rule.documentationUrl?.let { url ->
        append("<br/><br/>")
        append("<a href='$url'>Learn more</a>")
      }

      append("</body></html>")
    }
  }
}
