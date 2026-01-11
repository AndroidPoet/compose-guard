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

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity

public data class ViolationInfo(
  val ruleId: String,
  val ruleName: String,
  val category: RuleCategory,
  val severity: RuleSeverity,
  val message: String,
  val filePath: String,
  val fileName: String,
  val lineNumber: Int,
)

public data class RuleStatistics(
  val ruleId: String,
  val ruleName: String,
  val category: RuleCategory,
  val severity: RuleSeverity,
  val violationCount: Int,
  val violations: List<ViolationInfo>,
)

public data class CategoryStatistics(
  val category: RuleCategory,
  val totalViolations: Int,
  val ruleStatistics: List<RuleStatistics>,
  val errorCount: Int,
  val warningCount: Int,
  val weakWarningCount: Int,
  val infoCount: Int,
)

public data class FileStatistics(
  val filePath: String,
  val fileName: String,
  val totalViolations: Int,
  val violations: List<ViolationInfo>,
  val errorCount: Int,
  val warningCount: Int,
)

public data class ProjectStatistics(
  val totalFiles: Int,
  val filesWithViolations: Int,
  val totalViolations: Int,
  val categoryStatistics: Map<RuleCategory, CategoryStatistics>,
  val fileStatistics: List<FileStatistics>,
  val ruleStatistics: Map<String, RuleStatistics>,
  val errorCount: Int,
  val warningCount: Int,
  val weakWarningCount: Int,
  val infoCount: Int,
  val scanDurationMs: Long,
  val lastScanTime: Long,
) {
  public companion object {
    public fun empty(): ProjectStatistics = ProjectStatistics(
      totalFiles = 0,
      filesWithViolations = 0,
      totalViolations = 0,
      categoryStatistics = emptyMap(),
      fileStatistics = emptyList(),
      ruleStatistics = emptyMap(),
      errorCount = 0,
      warningCount = 0,
      weakWarningCount = 0,
      infoCount = 0,
      scanDurationMs = 0,
      lastScanTime = 0,
    )
  }
}
