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
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [MutableParameterRule] — runs the rule and asserts whether it fires. */
class MutableParameterBehaviorTest : BasePlatformTestCase() {

  private val rule = MutableParameterRule()

  fun test_mutableListParameter_shouldViolate() {
    assertEquals(1, analyze("@Composable fun S(items: MutableList<Int>) {}"))
  }

  fun test_immutableListParameter_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(items: List<Int>) {}"))
  }

  fun test_functionReturningMutable_shouldNotViolate() {
    // A factory `() -> MutableList<T>` passes a function, not a mutable instance.
    assertEquals(0, analyze("@Composable fun S(factory: () -> MutableList<Int>) {}"))
  }

  fun test_mutableMapWithLambdaValueType_shouldViolate() {
    // The parameter is a MutableMap; the `->` lives inside its type arguments and must not exempt
    // it from the rule.
    assertEquals(1, analyze("@Composable fun S(handlers: MutableMap<String, () -> Unit>) {}"))
  }

  fun test_lambdaWithMutableReceiver_shouldNotViolate() {
    // `MutableList<T>.() -> Unit` is a function type (extension lambda), not a mutable instance.
    assertEquals(0, analyze("@Composable fun S(block: MutableList<Int>.() -> Unit) {}"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
