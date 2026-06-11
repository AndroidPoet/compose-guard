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

/** Behavioral coverage for [ComposableNamingRule] — runs the rule and asserts whether it fires. */
class ComposableNamingBehaviorTest : BasePlatformTestCase() {

  private val rule = ComposableNamingRule()

  fun test_unitReturningLowercase_shouldViolate() {
    assertEquals(1, analyze("@Composable fun myButton() {}", "myButton"))
  }

  fun test_unitReturningPascalCase_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun MyButton() {}", "MyButton"))
  }

  fun test_valueReturningUppercase_shouldViolate() {
    assertEquals(1, analyze("@Composable fun Calculate(): Int = 5", "Calculate"))
  }

  fun test_valueReturningCamelCase_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun calculate(): Int = 5", "calculate"))
  }

  fun test_rememberFamilyName_shouldNotViolate() {
    // Value-returning factories whose names start with an allowed remember-family prefix are fine.
    assertEquals(0, analyze("@Composable fun rememberThing(): Int = 5", "rememberThing"))
  }

  private fun analyze(code: String, fnName: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == fnName }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
