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
package io.androidpoet.composeguard.rules.performance

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class DeferStateReadsFalsePositiveTest : BasePlatformTestCase() {

  private val rule = DeferStateReadsRule()

  fun test_plainComputedValueWithAnimatedSoundingName_shouldNotViolate() {
    // 'progress' is a plain computed value, not animated state. The old name-substring heuristic
    // wrongly treated it (and identifiers containing x/y) as frequently-changing state.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Bar(count: Int, total: Int) {
          val progress = count.toFloat() / total
          Box(modifier = Modifier.alpha(progress))
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_realAnimatedStateReadDuringComposition_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Bar(target: Float) {
          val animatedAlpha by animateFloatAsState(targetValue = target)
          Box(modifier = Modifier.alpha(animatedAlpha))
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Bar" }
  }
}
