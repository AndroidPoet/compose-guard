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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class TypeSpecificStateFalsePositiveTest : BasePlatformTestCase() {

  private val rule = TypeSpecificStateRule()

  fun test_plainMutableListOfInt_shouldNotViolate() {
    // A plain Kotlin mutableListOf<Int> is not a compose-rule violation; suggesting the unrelated
    // androidx.collection mutableIntListOf was non-canonical noise.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          val ids = mutableListOf<Int>()
          ids.add(1)
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_mutableStateOfPrimitive_shouldStillViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          val count = mutableStateOf<Int>(0)
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_mutableStateOfFloatLiteralWithoutDecimalPoint_shouldViolate() {
    // `0f` / `1f` (no decimal point) are extremely common Float literals in Compose (alpha,
    // progress, rotation). The inferred-type check previously required a decimal point, so these
    // were missed and never suggested mutableFloatStateOf.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          val alpha = mutableStateOf(0f)
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
