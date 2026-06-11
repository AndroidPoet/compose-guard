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

class ModifierOrderFalsePositiveTest : BasePlatformTestCase() {

  private val rule = ModifierOrderRule()

  fun test_order_offsetBeforeClickable_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Chip() {
          Box(modifier = Modifier.offset(x = 4.dp).clickable { })
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_order_paddingBeforeClickable_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Chip() {
          Box(modifier = Modifier.padding(16.dp).clickable { })
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_order_clickableThenPadding_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Chip() {
          Box(modifier = Modifier.clickable { }.padding(16.dp))
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_order_paddingBeforeTwoInteractionModifiers_reportsPaddingOnce() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Chip() {
          Box(modifier = Modifier.padding(16.dp).clickable { }.toggleable(value = true) { })
        }
      """.trimIndent(),
    )
    // The single padding precedes two interaction modifiers, but it is one ordering problem and
    // should be reported once — not once per following interaction modifier.
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Chip" }
  }
}
