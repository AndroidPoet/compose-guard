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
package io.androidpoet.composeguard.statistics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.AnnotationClassRule
import io.androidpoet.composeguard.rules.AnyFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleRegistry
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isComposable
import io.androidpoet.composeguard.rules.isCompositionLocal
import io.androidpoet.composeguard.rules.isSuppressed
import io.androidpoet.composeguard.settings.ComposeGuardSettingsState
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service for scanning the project and collecting violation statistics.
 */
@Service(Service.Level.PROJECT)
public class ComposeGuardStatisticsService(private val project: Project) {

  private var currentStatistics: ProjectStatistics = ProjectStatistics.empty()
  private val listeners = CopyOnWriteArrayList<StatisticsListener>()
  private var isScanning = false

  /**
   * Listener for statistics updates.
   */
  public fun interface StatisticsListener {
    public fun onStatisticsUpdated(statistics: ProjectStatistics)
  }

  /**
   * Add a listener for statistics updates.
   */
  public fun addListener(listener: StatisticsListener) {
    listeners.add(listener)
  }

  /**
   * Remove a statistics listener.
   */
  public fun removeListener(listener: StatisticsListener) {
    listeners.remove(listener)
  }

  /**
   * Get the current statistics.
   */
  public fun getCurrentStatistics(): ProjectStatistics = currentStatistics

  /**
   * Check if a scan is currently in progress.
   */
  public fun isScanning(): Boolean = isScanning

  /**
   * Scan the entire project for violations.
   */
  public fun scanProject(onComplete: (() -> Unit)? = null) {
    if (isScanning) return

    isScanning = true
    notifyListeners(currentStatistics.copy()) // Notify with current state to show loading

    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Scanning Compose Files", true) {
      override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
        indicator.text = "Collecting Kotlin files..."

        val startTime = System.currentTimeMillis()
        val violations = mutableListOf<ViolationInfo>()
        val filesAnalyzed = mutableSetOf<String>()

        try {
          val kotlinFiles = ReadAction.compute<Collection<VirtualFile>, Throwable> {
            FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
          }

          val totalFiles = kotlinFiles.size
          var processedFiles = 0

          for (virtualFile in kotlinFiles) {
            if (indicator.isCanceled) break

            indicator.fraction = processedFiles.toDouble() / totalFiles
            indicator.text2 = "Analyzing: ${virtualFile.name}"

            ReadAction.run<Throwable> {
              val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
              if (psiFile is KtFile) {
                val fileViolations = analyzeFile(psiFile)
                if (fileViolations.isNotEmpty()) {
                  violations.addAll(fileViolations)
                  filesAnalyzed.add(virtualFile.path)
                }
              }
            }

            processedFiles++
          }

          val scanDuration = System.currentTimeMillis() - startTime
          currentStatistics =
            buildStatistics(violations, filesAnalyzed.size, totalFiles, scanDuration)
        } catch (e: Exception) {
          // Handle errors gracefully
          currentStatistics = ProjectStatistics.empty()
        } finally {
          isScanning = false
        }
      }

      override fun onSuccess() {
        notifyListeners(currentStatistics)
        onComplete?.invoke()
      }

      override fun onCancel() {
        isScanning = false
        notifyListeners(currentStatistics)
      }
    })
  }

  private fun analyzeFile(file: KtFile): List<ViolationInfo> {
    val settings = ComposeGuardSettingsState.getInstance()
    if (!settings.isComposeRulesEnabled) {
      return emptyList()
    }

    val violations = mutableListOf<ViolationInfo>()
    val context = AnalysisContext(file, isOnTheFly = false)
    val enabledRules = ComposeRuleRegistry.getEnabledRules()

    if (enabledRules.isEmpty()) {
      return emptyList()
    }

    file.accept(object : KtTreeVisitorVoid() {
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

            val ruleViolations = rule.analyzeFunction(function, context)
            for (violation in ruleViolations) {
              if (isSuppressed(violation.element, rule.id)) continue

              violations.add(createViolationInfo(violation, file))
            }
          } catch (e: Exception) {
            // Skip rules that fail
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
            val ruleViolations = rule.analyzeProperty(property, context)
            for (violation in ruleViolations) {
              if (isSuppressed(violation.element, rule.id)) continue

              violations.add(createViolationInfo(violation, file))
            }
          } catch (e: Exception) {
            // Skip rules that fail
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

            val ruleViolations = rule.analyzeClass(klass, context)
            for (violation in ruleViolations) {
              if (isSuppressed(violation.element, rule.id)) continue

              violations.add(createViolationInfo(violation, file))
            }
          } catch (e: Exception) {
            // Skip rules that fail
          }
        }
      }
    })

    return violations
  }

  private fun createViolationInfo(violation: ComposeRuleViolation, file: KtFile): ViolationInfo {
    val document = file.viewProvider.document
    val lineNumber = document?.getLineNumber(violation.element.textOffset)?.plus(1) ?: 0

    return ViolationInfo(
      ruleId = violation.rule.id,
      ruleName = violation.rule.name,
      category = violation.rule.category,
      severity = violation.rule.severity,
      message = violation.message,
      filePath = file.virtualFile?.path ?: "",
      fileName = file.name,
      lineNumber = lineNumber,
    )
  }

  private fun buildStatistics(
    violations: List<ViolationInfo>,
    filesWithViolations: Int,
    totalFiles: Int,
    scanDurationMs: Long,
  ): ProjectStatistics {
    // Group violations by category
    val byCategory = violations.groupBy { it.category }
    val categoryStats = RuleCategory.entries.associateWith { category ->
      val categoryViolations = byCategory[category] ?: emptyList()
      val byRule = categoryViolations.groupBy { it.ruleId }

      CategoryStatistics(
        category = category,
        totalViolations = categoryViolations.size,
        ruleStatistics = byRule.map { (ruleId, ruleViolations) ->
          val first = ruleViolations.first()
          RuleStatistics(
            ruleId = ruleId,
            ruleName = first.ruleName,
            category = first.category,
            severity = first.severity,
            violationCount = ruleViolations.size,
            violations = ruleViolations,
          )
        }.sortedByDescending { it.violationCount },
        errorCount = categoryViolations.count { it.severity == RuleSeverity.ERROR },
        warningCount = categoryViolations.count { it.severity == RuleSeverity.WARNING },
        weakWarningCount = categoryViolations.count { it.severity == RuleSeverity.WEAK_WARNING },
        infoCount = categoryViolations.count { it.severity == RuleSeverity.INFO },
      )
    }

    // Group violations by file
    val byFile = violations.groupBy { it.filePath }
    val fileStats = byFile.map { (filePath, fileViolations) ->
      FileStatistics(
        filePath = filePath,
        fileName = fileViolations.first().fileName,
        totalViolations = fileViolations.size,
        violations = fileViolations,
        errorCount = fileViolations.count { it.severity == RuleSeverity.ERROR },
        warningCount = fileViolations.count { it.severity == RuleSeverity.WARNING },
      )
    }.sortedByDescending { it.totalViolations }

    // Group violations by rule
    val byRule = violations.groupBy { it.ruleId }
    val ruleStats = byRule.mapValues { (ruleId, ruleViolations) ->
      val first = ruleViolations.first()
      RuleStatistics(
        ruleId = ruleId,
        ruleName = first.ruleName,
        category = first.category,
        severity = first.severity,
        violationCount = ruleViolations.size,
        violations = ruleViolations,
      )
    }

    return ProjectStatistics(
      totalFiles = totalFiles,
      filesWithViolations = filesWithViolations,
      totalViolations = violations.size,
      categoryStatistics = categoryStats,
      fileStatistics = fileStats,
      ruleStatistics = ruleStats,
      errorCount = violations.count { it.severity == RuleSeverity.ERROR },
      warningCount = violations.count { it.severity == RuleSeverity.WARNING },
      weakWarningCount = violations.count { it.severity == RuleSeverity.WEAK_WARNING },
      infoCount = violations.count { it.severity == RuleSeverity.INFO },
      scanDurationMs = scanDurationMs,
      lastScanTime = System.currentTimeMillis(),
    )
  }

  private fun notifyListeners(statistics: ProjectStatistics) {
    ApplicationManager.getApplication().invokeLater {
      for (listener in listeners) {
        listener.onStatisticsUpdated(statistics)
      }
    }
  }

  public companion object {
    public fun getInstance(project: Project): ComposeGuardStatisticsService {
      return project.service()
    }
  }
}
