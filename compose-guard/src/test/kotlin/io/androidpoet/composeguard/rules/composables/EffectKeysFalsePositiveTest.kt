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

class EffectKeysFalsePositiveTest : BasePlatformTestCase() {

  private val rule = EffectKeysRule()

  fun test_runOnceEffectCapturingNothing_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          LaunchedEffect(Unit) {
            analytics.logScreenView()
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_constantKeyButCapturedParamAlreadyKeyed_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(userId: String) {
          LaunchedEffect(Unit, userId) {
            load(userId)
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_constantKeyCapturingChangingParam_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(userId: String) {
          LaunchedEffect(Unit) {
            load(userId)
          }
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_constantKeyCapturingLambdaParam_shouldNotViolate() {
    // A lambda parameter captured in a constant-key effect is owned by LambdaParameterInEffect,
    // which gives the correct rememberUpdatedState-or-key guidance. EffectKeys must not also flag
    // it with one-sided "pass as key" advice.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(onTick: () -> Unit) {
          LaunchedEffect(Unit) {
            onTick()
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_constantKeyCapturingMapOfLambdas_shouldViolate() {
    // The parameter is a Map (a changing value that should be a key), not a lambda — the nested
    // arrow in its type argument must not exempt it.
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(handlers: Map<String, () -> Unit>) {
          LaunchedEffect(Unit) {
            handlers.forEach { it.value() }
          }
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
