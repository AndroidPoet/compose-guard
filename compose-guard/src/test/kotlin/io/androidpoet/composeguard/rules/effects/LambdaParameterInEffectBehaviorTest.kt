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
package io.androidpoet.composeguard.rules.effects

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Behavioral coverage for [LambdaParameterInEffectRule] — actually runs the rule against code and
 * asserts whether it fires, rather than only checking metadata.
 */
class LambdaParameterInEffectBehaviorTest : BasePlatformTestCase() {

  private val rule = LambdaParameterInEffectRule()

  fun test_lambdaInLaunchedEffectWithoutKey_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(onTimeout: () -> Unit) {
          LaunchedEffect(Unit) {
            onTimeout()
          }
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_lambdaAsKeyInLaunchedEffect_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(onTimeout: () -> Unit) {
          LaunchedEffect(onTimeout) {
            onTimeout()
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_lambdaWithRememberUpdatedState_shouldNotViolate() {
    // The canonical fix: reference the rememberUpdatedState-derived property inside the effect,
    // not the raw lambda parameter. The raw parameter is no longer used in the effect body.
    val fn = configure(
      """
        annotation class Composable
        fun <T> rememberUpdatedState(value: T): T = value
        @Composable
        fun Screen(onTimeout: () -> Unit) {
          val currentOnTimeout by rememberUpdatedState(onTimeout)
          LaunchedEffect(Unit) {
            currentOnTimeout()
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_lambdaInDisposableEffectWithoutKey_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(onDispose: () -> Unit) {
          DisposableEffect(Unit) {
            onDispose()
          }
        }
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  fun test_nonLambdaParameter_shouldNotBeChecked() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(userId: String) {
          LaunchedEffect(Unit) {
            println(userId)
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_mapOfLambdasIsNotALambdaParameter_shouldNotViolate() {
    // 'handlers' is a Map<String, () -> Unit>, not a lambda — its arrow is inside the type argument.
    // It must not be treated as an unkeyed lambda parameter just because the text contains "->".
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(handlers: Map<String, () -> Unit>) {
          LaunchedEffect(Unit) {
            handlers["x"]
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_typeNameContainingFunction_shouldNotViolate() {
    // 'registry' is a FunctionRegistry — an ordinary class whose name merely contains "Function".
    // The loose "Function" substring wrongly treated it as a function-type parameter.
    val fn = configure(
      """
        annotation class Composable
        class FunctionRegistry
        @Composable
        fun Screen(registry: FunctionRegistry) {
          LaunchedEffect(Unit) {
            println(registry)
          }
        }
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
