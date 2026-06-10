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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class DerivedStateOfCandidateFalsePositiveTest : BasePlatformTestCase() {

  private val rule = DerivedStateOfCandidateRule()

  /** Expensive op inside a lambda-valued property runs on invocation, not composition. */
  fun test_lambdaValuedEventHandler_shouldNotViolate() {
    assertEmpty(analyze("val onSave = { items.sortedBy { it.date } }"))
  }

  fun test_typedLambdaValue_shouldNotViolate() {
    assertEmpty(analyze("val compute: () -> List<Int> = { items.filter { it > 0 } }"))
  }

  /** A computation evaluated directly at composition time is still a candidate. */
  fun test_directMapAtComposition_shouldViolate() {
    assertTrue(analyze("val names = users.map { it.name }").isNotEmpty())
  }

  fun test_directFilterAtComposition_shouldViolate() {
    assertTrue(analyze("val active = items.filter { it.active }").isNotEmpty())
  }

  private fun analyze(body: String): List<*> {
    val fn = configure("annotation class Composable\n@Composable\nfun Screen() { $body }")
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
