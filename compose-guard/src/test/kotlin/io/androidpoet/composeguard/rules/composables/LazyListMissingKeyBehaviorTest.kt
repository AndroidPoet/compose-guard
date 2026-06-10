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

class LazyListMissingKeyBehaviorTest : BasePlatformTestCase() {

  private val rule = LazyListMissingKeyRule()

  // --- The core true positive that the rule previously missed entirely. ---

  fun test_itemsWithoutKey_shouldViolate() {
    assertEquals(1, count("LazyColumn { items(data) { Row(it) } }"))
  }

  fun test_itemsIndexedWithoutKey_shouldViolate() {
    assertEquals(1, count("LazyColumn { itemsIndexed(data) { i, it -> Row(it) } }"))
  }

  // --- Keyed forms must remain clean (no false positives). ---

  fun test_itemsWithNamedKey_shouldNotViolate() {
    assertEquals(0, count("LazyColumn { items(data, key = { it.id }) { Row(it) } }"))
  }

  fun test_itemsWithPositionalKey_shouldNotViolate() {
    assertEquals(0, count("LazyColumn { items(data, { it.id }) { Row(it) } }"))
  }

  fun test_itemsWithNamedKeyAndContentType_shouldNotViolate() {
    assertEquals(0, count("LazyColumn { items(data, key = { it.id }, contentType = { \"x\" }) { Row(it) } }"))
  }

  // --- Key omitted but a non-key named arg present still violates. ---

  fun test_itemsWithContentTypeButNoKey_shouldViolate() {
    assertEquals(1, count("LazyColumn { items(data, contentType = { \"x\" }) { Row(it) } }"))
  }

  private fun count(snippet: String): Int {
    val fn = configure(
      "annotation class Composable\n@Composable\nfun Screen(data: List<Item>) { $snippet }",
    )
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
