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
package io.androidpoet.composeguard.rules.naming

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [EventParameterNamingRule] — runs the rule and asserts whether it fires. */
class EventParameterNamingBehaviorTest : BasePlatformTestCase() {

  private val rule = EventParameterNamingRule()

  fun test_pastTenseLambdaParam_shouldViolate() {
    assertEquals(1, analyze("@Composable fun S(onClicked: () -> Unit) {}"))
  }

  fun test_presentTenseLambdaParam_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(onClick: () -> Unit) {}"))
  }

  fun test_eedWordLambdaParam_shouldNotViolate() {
    // 'onProceed' ends with 'eed' — present tense / noun, not verb+ed.
    assertEquals(0, analyze("@Composable fun S(onProceed: () -> Unit) {}"))
  }

  fun test_nonLambdaParam_shouldNotBeChecked() {
    // 'loaded' is past tense but the parameter is not a lambda, so it is out of scope.
    assertEquals(0, analyze("@Composable fun S(loaded: Boolean) {}"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
