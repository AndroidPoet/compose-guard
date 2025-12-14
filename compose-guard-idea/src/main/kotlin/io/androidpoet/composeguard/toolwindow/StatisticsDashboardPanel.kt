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
package io.androidpoet.composeguard.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import io.androidpoet.composeguard.statistics.CategoryStatistics
import io.androidpoet.composeguard.statistics.ComposeGuardStatisticsService
import io.androidpoet.composeguard.statistics.FileStatistics
import io.androidpoet.composeguard.statistics.ProjectStatistics
import io.androidpoet.composeguard.statistics.RuleStatistics
import io.androidpoet.composeguard.statistics.ViolationInfo
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Main dashboard panel for displaying violation statistics.
 */
public class StatisticsDashboardPanel(private val project: Project) : JPanel(BorderLayout()) {

  private val statisticsService = ComposeGuardStatisticsService.getInstance(project)
  private val summaryPanel = SummaryPanel()
  private val categoryPanel = CategoryBreakdownPanel(project)
  private val filesPanel = FilesBreakdownPanel(project)
  private val rulesPanel = RulesBreakdownPanel(project)
  private val violationsPanel = ViolationsListPanel(project)
  private val statusLabel = JBLabel("Click 'Scan Project' to analyze your codebase")
  private val scanButton = JButton("Scan Project", AllIcons.Actions.Refresh)
  private val lastScanLabel = JBLabel("")

  init {
    border = JBUI.Borders.empty(10)
    setupUI()
    setupListeners()
  }

  private fun setupUI() {
    // Header panel with scan button
    val headerPanel = JPanel(BorderLayout()).apply {
      border = JBUI.Borders.emptyBottom(10)

      val titlePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
        val titleLabel = JBLabel("ComposeGuard Statistics").apply {
          font = font.deriveFont(Font.BOLD, 16f)
        }
        add(titleLabel)
      }

      val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 10, 0)).apply {
        add(lastScanLabel)
        add(scanButton)
      }

      add(titlePanel, BorderLayout.WEST)
      add(buttonPanel, BorderLayout.EAST)
    }

    // Summary panel at top
    val topPanel = JPanel(BorderLayout()).apply {
      add(headerPanel, BorderLayout.NORTH)
      add(summaryPanel, BorderLayout.CENTER)
    }

    // Tabbed pane for detailed views
    val tabbedPane = JTabbedPane().apply {
      addTab("By Category", categoryPanel)
      addTab("By File", filesPanel)
      addTab("By Rule", rulesPanel)
      addTab("All Violations", violationsPanel)
    }

    // Status panel at bottom
    val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
      add(statusLabel)
    }

    add(topPanel, BorderLayout.NORTH)
    add(tabbedPane, BorderLayout.CENTER)
    add(statusPanel, BorderLayout.SOUTH)
  }

  private fun setupListeners() {
    scanButton.addActionListener {
      scanButton.isEnabled = false
      statusLabel.text = "Scanning project..."
      statisticsService.scanProject {
        scanButton.isEnabled = true
      }
    }

    statisticsService.addListener { statistics ->
      updateStatistics(statistics)
    }
  }

  private fun updateStatistics(statistics: ProjectStatistics) {
    summaryPanel.update(statistics)
    categoryPanel.update(statistics)
    filesPanel.update(statistics)
    rulesPanel.update(statistics)
    violationsPanel.update(statistics)

    if (statistics.lastScanTime > 0) {
      val dateFormat = SimpleDateFormat("HH:mm:ss")
      lastScanLabel.text = "Last scan: ${dateFormat.format(Date(statistics.lastScanTime))} (${statistics.scanDurationMs}ms)"
      statusLabel.text = "Found ${statistics.totalViolations} violations in ${statistics.filesWithViolations} of ${statistics.totalFiles} files"
    }
  }
}

/**
 * Panel showing summary statistics with visual cards.
 */
private class SummaryPanel : JPanel(GridBagLayout()) {

  private val totalViolationsCard = StatCard("Total Violations", "0", JBColor.GRAY)
  private val errorsCard = StatCard("Errors", "0", JBColor.RED)
  private val warningsCard = StatCard("Warnings", "0", JBColor.ORANGE)
  private val filesCard = StatCard("Files Affected", "0", JBColor.BLUE)

  init {
    border = JBUI.Borders.empty(10, 0)

    val gbc = GridBagConstraints().apply {
      fill = GridBagConstraints.HORIZONTAL
      weightx = 1.0
      insets = JBUI.insets(5)
    }

    gbc.gridx = 0
    add(totalViolationsCard, gbc)

    gbc.gridx = 1
    add(errorsCard, gbc)

    gbc.gridx = 2
    add(warningsCard, gbc)

    gbc.gridx = 3
    add(filesCard, gbc)
  }

  fun update(statistics: ProjectStatistics) {
    totalViolationsCard.setValue(statistics.totalViolations.toString())
    errorsCard.setValue(statistics.errorCount.toString())
    warningsCard.setValue((statistics.warningCount + statistics.weakWarningCount).toString())
    filesCard.setValue("${statistics.filesWithViolations}/${statistics.totalFiles}")
  }
}

/**
 * A card widget showing a statistic.
 */
private class StatCard(title: String, initialValue: String, private val accentColor: Color) : JPanel(BorderLayout()) {

  private val valueLabel = JBLabel(initialValue).apply {
    font = font.deriveFont(Font.BOLD, 24f)
    horizontalAlignment = SwingConstants.CENTER
  }

  init {
    border = BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(accentColor, 2),
      JBUI.Borders.empty(10),
    )
    preferredSize = Dimension(150, 80)

    val titleLabel = JBLabel(title).apply {
      foreground = JBColor.GRAY
      horizontalAlignment = SwingConstants.CENTER
      font = font.deriveFont(11f)
    }

    add(titleLabel, BorderLayout.NORTH)
    add(valueLabel, BorderLayout.CENTER)
  }

  fun setValue(value: String) {
    valueLabel.text = value
  }
}

/**
 * Panel showing breakdown by category.
 */
private class CategoryBreakdownPanel(private val project: Project) : JPanel(BorderLayout()) {

  private val tableModel = CategoryTableModel()
  private val table = JBTable(tableModel).apply {
    setShowGrid(true)
    rowHeight = 30
    tableHeader.reorderingAllowed = false
    setDefaultRenderer(Any::class.java, CategoryCellRenderer())
  }

  init {
    add(JBScrollPane(table), BorderLayout.CENTER)
  }

  fun update(statistics: ProjectStatistics) {
    tableModel.update(statistics.categoryStatistics.values.toList())
  }
}

private class CategoryTableModel : AbstractTableModel() {
  private val columns = arrayOf("Category", "Violations", "Errors", "Warnings", "Info")
  private var data: List<CategoryStatistics> = emptyList()

  fun update(categories: List<CategoryStatistics>) {
    data = categories.sortedByDescending { it.totalViolations }
    fireTableDataChanged()
  }

  override fun getRowCount(): Int = data.size
  override fun getColumnCount(): Int = columns.size
  override fun getColumnName(column: Int): String = columns[column]

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    val category = data[rowIndex]
    return when (columnIndex) {
      0 -> category.category.displayName
      1 -> category.totalViolations
      2 -> category.errorCount
      3 -> category.warningCount + category.weakWarningCount
      4 -> category.infoCount
      else -> ""
    }
  }
}

private class CategoryCellRenderer : DefaultTableCellRenderer() {
  override fun getTableCellRendererComponent(
    table: javax.swing.JTable?,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int,
  ): Component {
    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    horizontalAlignment = if (column == 0) SwingConstants.LEFT else SwingConstants.CENTER
    return component
  }
}

/**
 * Panel showing breakdown by file.
 */
private class FilesBreakdownPanel(private val project: Project) : JPanel(BorderLayout()) {

  private val tableModel = FilesTableModel()
  private val table = JBTable(tableModel).apply {
    setShowGrid(true)
    rowHeight = 28
    tableHeader.reorderingAllowed = false
    setDefaultRenderer(Any::class.java, FilesCellRenderer())

    addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) {
        if (e.clickCount == 2) {
          val row = rowAtPoint(e.point)
          if (row >= 0) {
            val fileStats = tableModel.getFileAt(row)
            navigateToFile(fileStats)
          }
        }
      }
    })

    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
  }

  init {
    val infoLabel = JBLabel("Double-click a file to open it").apply {
      foreground = JBColor.GRAY
      border = JBUI.Borders.empty(5)
    }

    add(infoLabel, BorderLayout.NORTH)
    add(JBScrollPane(table), BorderLayout.CENTER)
  }

  private fun navigateToFile(fileStats: FileStatistics) {
    val virtualFile = LocalFileSystem.getInstance().findFileByPath(fileStats.filePath) ?: return
    val descriptor = OpenFileDescriptor(project, virtualFile)
    FileEditorManager.getInstance(project).openEditor(descriptor, true)
  }

  fun update(statistics: ProjectStatistics) {
    tableModel.update(statistics.fileStatistics)
  }
}

private class FilesTableModel : AbstractTableModel() {
  private val columns = arrayOf("File", "Violations", "Errors", "Warnings")
  private var data: List<FileStatistics> = emptyList()

  fun update(files: List<FileStatistics>) {
    data = files
    fireTableDataChanged()
  }

  fun getFileAt(row: Int): FileStatistics = data[row]

  override fun getRowCount(): Int = data.size
  override fun getColumnCount(): Int = columns.size
  override fun getColumnName(column: Int): String = columns[column]

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    val file = data[rowIndex]
    return when (columnIndex) {
      0 -> file.fileName
      1 -> file.totalViolations
      2 -> file.errorCount
      3 -> file.warningCount
      else -> ""
    }
  }
}

private class FilesCellRenderer : DefaultTableCellRenderer() {
  override fun getTableCellRendererComponent(
    table: javax.swing.JTable?,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int,
  ): Component {
    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    horizontalAlignment = if (column == 0) SwingConstants.LEFT else SwingConstants.CENTER
    if (column == 0 && !isSelected) {
      foreground = JBColor.BLUE
    }
    return component
  }
}

/**
 * Panel showing breakdown by rule.
 */
private class RulesBreakdownPanel(private val project: Project) : JPanel(BorderLayout()) {

  private val tableModel = RulesTableModel()
  private val table = JBTable(tableModel).apply {
    setShowGrid(true)
    rowHeight = 28
    tableHeader.reorderingAllowed = false
    setDefaultRenderer(Any::class.java, RulesCellRenderer())
  }

  init {
    add(JBScrollPane(table), BorderLayout.CENTER)
  }

  fun update(statistics: ProjectStatistics) {
    tableModel.update(statistics.ruleStatistics.values.toList())
  }
}

private class RulesTableModel : AbstractTableModel() {
  private val columns = arrayOf("Rule", "Category", "Severity", "Violations")
  private var data: List<RuleStatistics> = emptyList()

  fun update(rules: List<RuleStatistics>) {
    data = rules.sortedByDescending { it.violationCount }
    fireTableDataChanged()
  }

  override fun getRowCount(): Int = data.size
  override fun getColumnCount(): Int = columns.size
  override fun getColumnName(column: Int): String = columns[column]

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    val rule = data[rowIndex]
    return when (columnIndex) {
      0 -> rule.ruleName
      1 -> rule.category.displayName
      2 -> rule.severity.name
      3 -> rule.violationCount
      else -> ""
    }
  }
}

private class RulesCellRenderer : DefaultTableCellRenderer() {
  override fun getTableCellRendererComponent(
    table: javax.swing.JTable?,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int,
  ): Component {
    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    horizontalAlignment = if (column == 0 || column == 1) SwingConstants.LEFT else SwingConstants.CENTER

    // Color-code severity
    if (column == 2 && !isSelected) {
      foreground = when (value) {
        "ERROR" -> JBColor.RED
        "WARNING" -> JBColor.ORANGE
        "WEAK_WARNING" -> JBColor.YELLOW.darker()
        else -> JBColor.GRAY
      }
    } else if (!isSelected) {
      foreground = JBColor.foreground()
    }

    return component
  }
}

/**
 * Panel showing all violations in a list.
 */
private class ViolationsListPanel(private val project: Project) : JPanel(BorderLayout()) {

  private val tableModel = ViolationsTableModel()
  private val table = JBTable(tableModel).apply {
    setShowGrid(true)
    rowHeight = 28
    tableHeader.reorderingAllowed = false
    setDefaultRenderer(Any::class.java, ViolationsCellRenderer())

    addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) {
        if (e.clickCount == 2) {
          val row = rowAtPoint(e.point)
          if (row >= 0) {
            val violation = tableModel.getViolationAt(row)
            navigateToViolation(violation)
          }
        }
      }
    })

    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
  }

  init {
    val infoLabel = JBLabel("Double-click a violation to navigate to it").apply {
      foreground = JBColor.GRAY
      border = JBUI.Borders.empty(5)
    }

    add(infoLabel, BorderLayout.NORTH)
    add(JBScrollPane(table), BorderLayout.CENTER)
  }

  private fun navigateToViolation(violation: ViolationInfo) {
    val virtualFile = LocalFileSystem.getInstance().findFileByPath(violation.filePath) ?: return
    val descriptor = OpenFileDescriptor(project, virtualFile, violation.lineNumber - 1, 0)
    FileEditorManager.getInstance(project).openEditor(descriptor, true)
  }

  fun update(statistics: ProjectStatistics) {
    val allViolations = statistics.fileStatistics.flatMap { it.violations }
    tableModel.update(allViolations)
  }
}

private class ViolationsTableModel : AbstractTableModel() {
  private val columns = arrayOf("File", "Line", "Rule", "Severity", "Message")
  private var data: List<ViolationInfo> = emptyList()

  fun update(violations: List<ViolationInfo>) {
    data = violations
    fireTableDataChanged()
  }

  fun getViolationAt(row: Int): ViolationInfo = data[row]

  override fun getRowCount(): Int = data.size
  override fun getColumnCount(): Int = columns.size
  override fun getColumnName(column: Int): String = columns[column]

  override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
    val violation = data[rowIndex]
    return when (columnIndex) {
      0 -> violation.fileName
      1 -> violation.lineNumber
      2 -> violation.ruleName
      3 -> violation.severity.name
      4 -> violation.message
      else -> ""
    }
  }
}

private class ViolationsCellRenderer : DefaultTableCellRenderer() {
  override fun getTableCellRendererComponent(
    table: javax.swing.JTable?,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int,
  ): Component {
    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

    horizontalAlignment = when (column) {
      1 -> SwingConstants.CENTER // Line number
      3 -> SwingConstants.CENTER // Severity
      else -> SwingConstants.LEFT
    }

    // Color-code severity
    if (column == 3 && !isSelected) {
      foreground = when (value) {
        "ERROR" -> JBColor.RED
        "WARNING" -> JBColor.ORANGE
        "WEAK_WARNING" -> JBColor.YELLOW.darker()
        else -> JBColor.GRAY
      }
    } else if (column == 0 && !isSelected) {
      foreground = JBColor.BLUE
    } else if (!isSelected) {
      foreground = JBColor.foreground()
    }

    return component
  }
}
