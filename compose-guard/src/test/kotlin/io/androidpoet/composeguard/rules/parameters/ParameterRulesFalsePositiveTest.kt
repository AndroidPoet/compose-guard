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

class ParameterRulesFalsePositiveTest : BasePlatformTestCase() {

  private val mutableParam = MutableParameterRule()
  private val forwarding = ViewModelForwardingRule()
  private val explicit = ExplicitDependenciesRule()

  // ----- MutableParameter -----

  fun test_mutableParam_functionTypeReturningMutableList_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(factory: () -> MutableList<Int>) {}
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(mutableParam.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_mutableParam_mutableStateFlow_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(stream: MutableStateFlow<Int>) {}
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(mutableParam.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_mutableParam_wrapperAroundHashMap_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(cache: Wrapper<HashMap<String, Int>>) {}
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(mutableParam.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_mutableParam_directMutableList_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(items: MutableList<String>) {}
      """.trimIndent(),
      "Screen",
    )
    assertEquals(1, mutableParam.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  // ----- ViewModelForwarding -----

  fun test_forwarding_passedToNonComposableHelper_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(viewModel: MyViewModel) {
          register(viewModel = viewModel)
        }
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(forwarding.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_forwarding_passedToChildComposable_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen(viewModel: MyViewModel) {
          ChildScreen(viewModel = viewModel)
        }
      """.trimIndent(),
      "Screen",
    )
    assertEquals(1, forwarding.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  // ----- ExplicitDependencies -----

  fun test_explicit_frameworkCompositionLocalRead_shouldNotViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          val density = LocalDensity.current
          val config = LocalConfiguration.current
          val dir = LocalLayoutDirection.current
        }
      """.trimIndent(),
      "Screen",
    )
    assertEmpty(explicit.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_explicit_implicitViewModelFactory_shouldViolate() {
    val fn = configure(
      """
        annotation class Composable
        @Composable
        fun Screen() {
          val vm = viewModel<MyViewModel>()
        }
      """.trimIndent(),
      "Screen",
    )
    assertEquals(1, explicit.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun configure(code: String, name: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == name }
  }
}
