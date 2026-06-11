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

/** Behavioral coverage for [ViewModelForwardingRule] — runs the rule and asserts whether it fires. */
class ViewModelForwardingBehaviorTest : BasePlatformTestCase() {

  private val rule = ViewModelForwardingRule()

  fun test_viewModelForwardedToChildComposable_shouldViolate() {
    assertEquals(
      1,
      analyze(
        "class MyViewModel\n" +
          "@Composable fun Child(viewModel: MyViewModel) {}\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { Child(viewModel = viewModel) }",
      ),
    )
  }

  fun test_viewModelForwardedPositionally_shouldViolate() {
    // The most common forwarding form passes the ViewModel positionally, not as a named argument.
    assertEquals(
      1,
      analyze(
        "class MyViewModel\n" +
          "@Composable fun Child(viewModel: MyViewModel) {}\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { Child(viewModel) }",
      ),
    )
  }

  fun test_viewModelForwardedUnderDifferentParamName_shouldViolate() {
    // The ViewModel is still forwarded even when the child names its parameter something else.
    assertEquals(
      1,
      analyze(
        "class MyViewModel\n" +
          "@Composable fun Child(state: MyViewModel) {}\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { Child(state = viewModel) }",
      ),
    )
  }

  fun test_viewModelAsLaunchedEffectKey_shouldNotViolate() {
    // Passing the ViewModel as an effect restart key is not forwarding it to another composable.
    assertEquals(
      0,
      analyze(
        "class MyViewModel\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { LaunchedEffect(viewModel) { } }",
      ),
    )
  }

  fun test_viewModelPassedToLowercaseHelper_shouldNotViolate() {
    assertEquals(
      0,
      analyze(
        "class MyViewModel\n" +
          "fun helper(viewModel: MyViewModel) {}\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { helper(viewModel = viewModel) }",
      ),
    )
  }

  fun test_viewModelUsedAsReceiver_shouldNotViolate() {
    assertEquals(
      0,
      analyze(
        "class MyViewModel { fun load() {} }\n" +
          "@Composable fun Parent(viewModel: MyViewModel) { viewModel.load() }",
      ),
    )
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Parent" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
