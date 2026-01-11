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
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

public class ComposeGuardInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
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

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
          }
        }
      }

      override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        if (!property.isCompositionLocal()) {
          return
        }

        for (rule in enabledRules) {
          try {
            val violations = rule.analyzeProperty(property, context)
            for (violation in violations) {
              if (isSuppressed(violation.element, rule.id)) continue

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
          }
        }
      }

      override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        if (!klass.isAnnotation()) {
          return
        }

        for (rule in enabledRules) {
          try {
            if (rule !is AnnotationClassRule) continue

            val violations = rule.analyzeClass(klass, context)
            for (violation in violations) {
              if (isSuppressed(violation.element, rule.id)) continue

              holder.registerProblem(
                violation.element,
                "[${rule.id}] ${violation.message}",
                violation.highlightType,
                *violation.quickFixes.toTypedArray(),
              )
            }
          } catch (e: Exception) {
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
