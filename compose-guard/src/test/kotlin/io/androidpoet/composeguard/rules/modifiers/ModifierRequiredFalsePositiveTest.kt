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

class ModifierRequiredFalsePositiveTest : BasePlatformTestCase() {

  private val rule = ModifierRequiredRule()

  fun test_composableThatOnlyComputesFactoryValues_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun BuildPalette() {
          val primary = Color(0xFF112233)
          val style = TextStyle(color = primary)
          val padding = PaddingValues(8.dp)
        }
      """.trimIndent(),
    )

    // Color(...)/TextStyle(...)/PaddingValues(...) are consumed as values, not emitted as UI.
    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_composableReturningFactoryValue_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun ProvideBrush() {
          return Brush.verticalGradient(listOf(Color.Red, Color.Blue))
        }
      """.trimIndent(),
    )

    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_composableThatEmitsContentWithoutModifier_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun UserCard(name: String) {
          Column {
            Text(name)
          }
        }
      """.trimIndent(),
    )

    val violations = rule.analyzeFunction(function, AnalysisContext(function.containingKtFile))
    assertEquals(1, violations.size)
  }

  fun test_composableThatEmitsContentWithModifier_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun UserCard(name: String, modifier: Modifier = Modifier) {
          Column(modifier = modifier) {
            Text(name)
          }
        }
      """.trimIndent(),
    )

    assertEmpty(rule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first()
  }
}
