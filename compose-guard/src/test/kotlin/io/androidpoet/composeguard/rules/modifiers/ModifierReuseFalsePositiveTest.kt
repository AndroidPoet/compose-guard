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

class ModifierReuseFalsePositiveTest : BasePlatformTestCase() {

  private val rule = ModifierReuseRule()

  fun test_modifierUsedOncePerMutuallyExclusiveBranch_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Foo(modifier: Modifier = Modifier, expanded: Boolean) {
          if (expanded) {
            Column(modifier = modifier) { Text("a") }
          } else {
            Row(modifier = modifier) { Text("b") }
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_modifierReusedOnTwoLiveNodes_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Foo(modifier: Modifier = Modifier) {
          Column(modifier = modifier) {
            Text("a", modifier = modifier)
          }
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
