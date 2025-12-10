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
package io.androidpoet.composeguard.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.AnnotationClassRule
import io.androidpoet.composeguard.rules.AnyFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.rules.isCompositionLocal
import io.androidpoet.composeguard.rules.isSuppressed
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

/**
 * Inspection that checks for Compose rule violations.
 * This inspection runs all enabled rules from the ComposeRuleRegistry
 * and reports violations as problems.
 */
public class ComposeGuardInspection : LocalInspectionTool() {

  private val settings: ComposeGuardSettingsState
    get() = ComposeGuardSettingsState.getInstance()

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    // Check if rules are enabled
    if (!settings.isComposeRulesEnabled) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    val file = holder.file as? KtFile ?: return PsiElementVisitor.EMPTY_VISITOR
    val context = AnalysisContext(file, isOnTheFly)
    val enabledRules = ComposeRuleRegistry.getEnabledRules()

    if (enabledRules.isEmpty()) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return object : KtVisitorVoid() {
      override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val isComposable = function.isComposable()

        // Run all enabled rules on this function
        for (rule in enabledRules) {
          try {
            // Check if the rule should run on this function
            val shouldRun = when {
              // AnyFunctionRule with requiresComposable=false runs on all functions
              rule is AnyFunctionRule && !rule.requiresComposable -> true
              // Other rules only run on composable functions
              isComposable -> true
              else -> false
            }

            if (!shouldRun) continue

            val violations = rule.analyzeFunction(function, context)
            for (violation in violations) {
              // Skip suppressed violations
              if (isSuppressed(violation.element, rule.id)) continue

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
            // Silently skip rules that fail to prevent breaking the inspection
          }
        }
      }

      override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        // Check for CompositionLocal properties
        if (!property.isCompositionLocal()) {
          return
        }

        // Run property-level rules
        for (rule in enabledRules) {
          try {
            val violations = rule.analyzeProperty(property, context)
            for (violation in violations) {
              // Skip suppressed violations
              if (isSuppressed(violation.element, rule.id)) continue

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
            // Silently skip rules that fail
          }
        }
      }

      override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        // Only analyze annotation classes for now
        if (!klass.isAnnotation()) {
          return
        }

        // Run class-level rules (annotation class rules)
        for (rule in enabledRules) {
          try {
            // Only run AnnotationClassRule instances on classes
            if (rule !is AnnotationClassRule) continue

            val violations = rule.analyzeClass(klass, context)
            for (violation in violations) {
              // Skip suppressed violations
              if (isSuppressed(violation.element, rule.id)) continue

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
            // Silently skip rules that fail
          }
        }
      }
    }
  }

  override fun getDisplayName(): String = "Compose Rules"

  override fun getGroupDisplayName(): String = "Compose"

  override fun getShortName(): String = "ComposeRules"

  override fun isEnabledByDefault(): Boolean = true
}
