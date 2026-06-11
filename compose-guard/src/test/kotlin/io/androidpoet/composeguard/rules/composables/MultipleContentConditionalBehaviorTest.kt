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

/**
 * Coverage for content emitters that live inside `if`/`when` branches. Only one branch of a
 * conditional executes, so alternatives must NOT be summed, but an emitter that runs alongside a
 * conditional emitter — or two emitters in a single branch — are genuinely multiple emissions.
 */
class MultipleContentConditionalBehaviorTest : BasePlatformTestCase() {

  private val rule = MultipleContentRule()

  fun test_unconditionalEmitterPlusConditionalEmitter_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Screen(showImage: Boolean) {
          Text("a")
          if (showImage) {
            Image(painter)
          }
        }
      """.trimIndent(),
    )
    // Text always emits and Image emits on one path, so up to two pieces of content reach the parent.
    assertEquals(1, rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)).size)
  }

  fun test_multipleEmittersInSingleBranch_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Screen(loading: Boolean) {
          if (loading) {
            Text("a")
            Image(painter)
          }
        }
      """.trimIndent(),
    )
    // Both emitters run together when the branch is taken: two top-level emissions.
    assertEquals(1, rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)).size)
  }

  fun test_eitherOrEmittersInIfElse_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Screen(loading: Boolean) {
          if (loading) {
            CircularProgressIndicator()
          } else {
            Text("done")
          }
        }
      """.trimIndent(),
    )
    // Exactly one branch runs, so this is a single emission — must not be flagged.
    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_singleEmitterPerWhenBranch_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Screen(state: Int) {
          when (state) {
            0 -> Text("zero")
            1 -> Image(painter)
            else -> CircularProgressIndicator()
          }
        }
      """.trimIndent(),
    )
    // One when-branch executes, each emitting a single piece of content: not multiple emission.
    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.name != null && it.bodyBlockExpression != null }
  }
}
