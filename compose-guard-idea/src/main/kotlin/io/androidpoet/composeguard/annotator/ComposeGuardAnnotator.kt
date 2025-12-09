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
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.rules.isCompositionLocal
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Annotator that provides real-time highlighting for compose rule violations.
 * This runs as the user types and provides immediate feedback.
 */
public class ComposeGuardAnnotator : Annotator {

  private val settings: ComposeGuardSettingsState
    get() = ComposeGuardSettingsState.getInstance()

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    // Check if rules are enabled
    if (!settings.isComposeRulesEnabled) {
      return
    }

    when (element) {
      is KtNamedFunction -> annotateFunction(element, holder)
      is KtProperty -> annotateProperty(element, holder)
    }
  }

  private fun annotateFunction(function: KtNamedFunction, holder: AnnotationHolder) {
    // Only analyze composable functions
    if (!function.isComposable()) {
      return
    }

    val file = function.containingKtFile
    val context = AnalysisContext(file, isOnTheFly = true)
    val enabledRules = ComposeRuleRegistry.getEnabledRules()

    for (rule in enabledRules) {
      try {
        val violations = rule.analyzeFunction(function, context)
        for (violation in violations) {
          createAnnotation(violation, holder)
        }
      } catch (e: Exception) {
        // Silently skip rules that fail
      }
    }
  }

  private fun annotateProperty(property: KtProperty, holder: AnnotationHolder) {
    // Only analyze CompositionLocal properties
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
          createAnnotation(violation, holder)
        }
      } catch (e: Exception) {
        // Silently skip rules that fail
      }
    }
  }

  private fun createAnnotation(violation: ComposeRuleViolation, holder: AnnotationHolder) {
    val severity = mapSeverity(violation.rule.severity)
    val message = "[${violation.rule.id}] ${violation.message}"

    val builder = holder.newAnnotation(severity, message)
      .range(violation.element)
      .tooltip(buildTooltip(violation))
      .highlightType(mapHighlightType(violation.highlightType))

    // Add quick fixes as intention actions
    for (quickFix in violation.quickFixes) {
      builder.withFix(QuickFixIntentionWrapper(quickFix, violation.element))
    }

    builder.create()
  }

  private fun mapSeverity(severity: RuleSeverity): HighlightSeverity {
    return when (severity) {
      RuleSeverity.ERROR -> HighlightSeverity.ERROR
      RuleSeverity.WARNING -> HighlightSeverity.WARNING
      RuleSeverity.WEAK_WARNING -> HighlightSeverity.WEAK_WARNING
      RuleSeverity.INFO -> HighlightSeverity.INFORMATION
    }
  }

  private fun mapHighlightType(type: ProblemHighlightType): ProblemHighlightType {
    return type
  }

  private fun buildTooltip(violation: ComposeRuleViolation): String {
    return buildString {
      append("<html><body>")
      append("<b>${violation.rule.name}</b><br/>")
      append("<br/>")
      append(violation.tooltip ?: violation.message)

      // Add documentation link if available
      violation.rule.documentationUrl?.let { url ->
        append("<br/><br/>")
        append("<a href='$url'>Learn more</a>")
      }

      append("</body></html>")
    }
  }
}

/**
 * Wrapper to convert LocalQuickFix to IntentionAction for use in annotations.
 */
private class QuickFixIntentionWrapper(
  private val quickFix: com.intellij.codeInspection.LocalQuickFix,
  private val element: PsiElement,
) : com.intellij.codeInsight.intention.IntentionAction {

  override fun getText(): String = quickFix.name

  override fun getFamilyName(): String = quickFix.familyName

  override fun isAvailable(
    project: com.intellij.openapi.project.Project,
    editor: com.intellij.openapi.editor.Editor?,
    file: com.intellij.psi.PsiFile?,
  ): Boolean {
    return element.isValid
  }

  override fun invoke(
    project: com.intellij.openapi.project.Project,
    editor: com.intellij.openapi.editor.Editor?,
    file: com.intellij.psi.PsiFile?,
  ) {
    // Create a synthetic ProblemDescriptor to invoke the quick fix
    val descriptor = com.intellij.codeInspection.InspectionManager.getInstance(project)
      .createProblemDescriptor(
        element,
        "",
        quickFix,
        com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        true,
      )
    quickFix.applyFix(project, descriptor)
  }

  override fun startInWriteAction(): Boolean = true
}
