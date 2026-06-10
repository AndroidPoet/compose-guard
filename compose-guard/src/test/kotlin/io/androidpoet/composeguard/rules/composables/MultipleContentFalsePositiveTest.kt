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

class MultipleContentFalsePositiveTest : BasePlatformTestCase() {

  private val rule = MultipleContentRule()

  fun test_singleEmitterWithComputedFactoryValue_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Badge(label: String) {
          val color = Color(0xFF00FF00)
          val shape = RoundedCornerShape(4.dp)
          Text(label)
        }
      """.trimIndent(),
    )

    // Color(...) and RoundedCornerShape(...) are consumed values, so only Text emits: one emitter.
    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_twoRealEmitters_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun TwoThings() {
          Text("a")
          Image(painter)
        }
      """.trimIndent(),
    )

    assertEquals(1, rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)).size)
  }

  fun test_scopeExtensionWithMultipleEmitters_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun ColumnScope.InnerContent() {
          Text("a")
          Text("b")
        }
      """.trimIndent(),
    )

    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.name != null && it.bodyBlockExpression != null }
  }
}
