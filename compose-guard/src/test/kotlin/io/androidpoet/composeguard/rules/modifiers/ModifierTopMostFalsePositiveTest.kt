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
package io.androidpoet.composeguard.rules.modifiers

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class ModifierTopMostFalsePositiveTest : BasePlatformTestCase() {

  private val topMost = ModifierTopMostRule()
  private val avoidComposed = AvoidComposedRule()

  fun test_topMost_modifierInsideScaffoldContentSlot_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(modifier: Modifier = Modifier) {
          Scaffold { padding ->
            Column(modifier = modifier) { Text("a") }
          }
        }
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(topMost.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_topMost_modifierInsideBoxWithConstraints_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(modifier: Modifier = Modifier) {
          BoxWithConstraints {
            Column(modifier = modifier) { Text("a") }
          }
        }
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(topMost.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_topMost_modifierNestedInPlainBox_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(modifier: Modifier = Modifier) {
          Box {
            Column(modifier = modifier) { Text("a") }
          }
        }
      """.trimIndent(),
      "Screen",
    )
    assertEquals(1, topMost.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_avoidComposed_unrelatedComposedCall_shouldNotViolate() {
    val fn = configure(
      """
        fun Modifier.foo(): Modifier {
          val x = builder.composed()
          return this
        }
      """.trimIndent(),
      "foo",
    )
    assertEmpty(avoidComposed.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_avoidComposed_realComposedFactory_shouldViolate() {
    val fn = configure(
      """
        fun Modifier.shimmer(): Modifier = composed {
          this
        }
      """.trimIndent(),
      "shimmer",
    )
    assertEquals(1, avoidComposed.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String, name: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == name }
  }
}
