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
package io.androidpoet.composeguard.rules.parameters

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class TrailingLambdaFalsePositiveTest : BasePlatformTestCase() {

  private val rule = TrailingLambdaRule()

  fun test_multipleSlots_contentNotLast_shouldNotViolate() {
    // With several content slots, which one trails is the API author's choice.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun TwoPane(
          content: @Composable () -> Unit,
          detail: @Composable () -> Unit,
        ) {}
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_listOfComposablesWithTrailingEventHandler_shouldNotViolate() {
    // 'pages' is a List<@Composable () -> Unit>, NOT a content slot — its arrow is inside the
    // generic. With no real trailing content slot, an onX event handler in last position is fine.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Pager(
          pages: List<@Composable () -> Unit>,
          onPageSelected: (Int) -> Unit,
        ) {}
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_singleContentSlotNotLast_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Card(
          content: @Composable () -> Unit,
          elevation: Int,
        ) {}
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_overrideWithContentSlotNotTrailing_shouldNotViolate() {
    // The override inherits its parameter order from the supertype and cannot move the content slot
    // to the trailing position, so the reorder fix is not actionable.
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\n" +
        "interface P { @Composable fun S(content: @Composable () -> Unit, count: Int) }\n" +
        "class C : P { @Composable override fun S(content: @Composable () -> Unit, count: Int) {} }",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first()
  }
}
