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

/** Behavioral coverage for [ComponentDefaultsVisibilityRule] — runs the rule and asserts whether it fires. */
class ComponentDefaultsVisibilityBehaviorTest : BasePlatformTestCase() {

  private val rule = ComponentDefaultsVisibilityRule()

  fun test_publicComposableWithPrivateDefaults_shouldViolate() {
    assertEquals(
      1,
      analyze(
        "@Composable fun MyComponent() {}\n" +
          "private object MyComponentDefaults { val color = 0 }",
      ),
    )
  }

  fun test_matchingVisibility_shouldNotViolate() {
    assertEquals(
      0,
      analyze(
        "@Composable fun MyComponent() {}\n" +
          "object MyComponentDefaults { val color = 0 }",
      ),
    )
  }

  fun test_bothInternal_shouldNotViolate() {
    assertEquals(
      0,
      analyze(
        "@Composable internal fun MyComponent() {}\n" +
          "internal object MyComponentDefaults { val color = 0 }",
      ),
    )
  }

  fun test_noDefaultsObject_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun MyComponent() {}"))
  }

  fun test_internalComposableWithPublicDefaults_shouldViolate() {
    assertEquals(
      1,
      analyze(
        "@Composable internal fun MyComponent() {}\n" +
          "object MyComponentDefaults { val color = 0 }",
      ),
    )
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "MyComponent" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
