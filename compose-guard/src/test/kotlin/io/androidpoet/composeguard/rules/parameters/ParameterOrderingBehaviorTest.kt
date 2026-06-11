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
package io.androidpoet.composeguard.rules.parameters

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [ParameterOrderingRule] — runs the rule and asserts whether it fires. */
class ParameterOrderingBehaviorTest : BasePlatformTestCase() {

  private val rule = ParameterOrderingRule()

  fun test_requiredAfterOptional_shouldViolate() {
    assertTrue(analyze("@Composable fun S(a: Int = 0, b: String) {}") >= 1)
  }

  fun test_requiredBeforeOptional_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(a: Int, b: String = \"\") {}"))
  }

  fun test_singleParameter_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(a: Int) {}"))
  }

  fun test_listOfComposablesIsNotAContentLambda_shouldNotViolate() {
    // 'pages' is a List<@Composable () -> Unit>, not a content slot — its arrow is inside the
    // generic. It must not be required to trail a following non-lambda parameter.
    assertEquals(
      0,
      analyze("@Composable fun S(pages: List<@Composable () -> Unit>, selectedIndex: Int) {}"),
    )
  }

  fun test_overrideWithContentLambdaNotTrailing_shouldNotViolate() {
    // The override inherits its parameter order from the supertype and cannot reorder it, so the
    // reorder fix is not actionable. Matches ModifierNaming/ModifierRequired/ComposableNaming.
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\n" +
        "interface P { @Composable fun S(content: @Composable () -> Unit, count: Int) }\n" +
        "class C : P { @Composable override fun S(content: @Composable () -> Unit, count: Int) {} }",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
    assertEquals(0, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
