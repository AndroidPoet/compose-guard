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
package io.androidpoet.composeguard.rules.composables

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class LazyListContentTypeFalsePositiveTest : BasePlatformTestCase() {

  private val rule = LazyListContentTypeRule()

  /**
   * A LazyColumn whose only child is a single homogeneous `items(...)`, with a nested
   * LazyRow inside each row, must not be flagged: the inner LazyRow's `items` belong to
   * the inner list, not the outer one.
   */
  fun test_nestedLazyRowItems_notAttributedToOuterList_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Feed(rows: List<Row>) {
          LazyColumn {
            items(rows) { row ->
              LazyRow {
                items(row.cells) { cell -> Cell(cell) }
              }
            }
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  /**
   * Two sibling homogeneous lazy lists, each with a single `items(...)`, must not be merged
   * into one heterogeneous report. Each list owns only its own items.
   */
  fun test_twoSiblingHomogeneousLists_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Feed(a: List<X>, b: List<Y>) {
          LazyColumn { items(a) { Row(it) } }
          LazyRow { items(b) { Cell(it) } }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  /**
   * A genuinely heterogeneous single list (header item + homogeneous items) must still be
   * reported so the nesting fix does not silence real cases.
   */
  fun test_genuinelyHeterogeneousSingleList_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(data: List<Item>) {
          LazyColumn {
            item { Header() }
            items(data) { Row(it) }
          }
        }
      """.trimIndent(),
    )
    assertTrue(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).isNotEmpty())
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.name == "Feed" || it.name == "Screen" }
  }
}
