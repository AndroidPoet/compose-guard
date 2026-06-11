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

class ContentSlotReusedFalsePositiveTest : BasePlatformTestCase() {

  private val rule = ContentSlotReusedRule()

  fun test_slotInvokedOncePerMutuallyExclusiveBranch_shouldNotViolate() {
    // Calling the slot once in each branch of an if is the correct single-use pattern.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Foo(content: @Composable () -> Unit, wide: Boolean) {
          if (wide) {
            Row { content() }
          } else {
            Column { content() }
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_nullableSlotInvokedOnceViaSafeCall_shouldNotViolate() {
    // A single `content?.invoke()` is one invocation. It must not be double-counted (once as the
    // safe-qualified call, once again as the receiver reference) into a phantom "reused" report.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Foo(content: (@Composable () -> Unit)?) {
          content?.invoke()
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_slotInvokedTwiceOnSamePath_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Foo(content: @Composable () -> Unit) {
          Row { content() }
          Column { content() }
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Foo" }
  }
}
