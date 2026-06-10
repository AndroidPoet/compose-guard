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

class ModifierOverrideFalsePositiveTest : BasePlatformTestCase() {

  private val defaultValueRule = ModifierDefaultValueRule()
  private val namingRule = ModifierNamingRule()

  fun test_defaultValue_overrideWithoutDefault_shouldNotViolate() {
    // An override cannot legally add a default value to an inherited parameter.
    val function = configure(
      """
        annotation class Composable

        class Buttons : ButtonSet {
          @Composable
          override fun Primary(modifier: Modifier) {}
        }
      """.trimIndent(),
    )

    assertEmpty(defaultValueRule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_defaultValue_abstractWithoutDefault_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        abstract class ButtonSet {
          @Composable
          abstract fun Primary(modifier: Modifier)
        }
      """.trimIndent(),
    )

    assertEmpty(defaultValueRule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_defaultValue_plainComposableWithoutDefault_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Primary(modifier: Modifier) {}
      """.trimIndent(),
    )

    assertEquals(1, defaultValueRule.analyzeFunction(function, AnalysisContext(function.containingKtFile)).size)
  }

  fun test_naming_overrideWithDifferentParamName_shouldNotViolate() {
    val function = configure(
      """
        annotation class Composable

        class Buttons : ButtonSet {
          @Composable
          override fun Primary(mod: Modifier) {}
        }
      """.trimIndent(),
    )

    assertEmpty(namingRule.analyzeFunction(function, AnalysisContext(function.containingKtFile)))
  }

  fun test_naming_plainComposableWithBadParamName_shouldViolate() {
    val function = configure(
      """
        annotation class Composable

        @Composable
        fun Primary(mod: Modifier) {}
      """.trimIndent(),
    )

    assertEquals(1, namingRule.analyzeFunction(function, AnalysisContext(function.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.name == "Primary" }
  }
}
