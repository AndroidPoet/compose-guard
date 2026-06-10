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

class FrequentRecompositionFalsePositiveTest : BasePlatformTestCase() {

  private val rule = FrequentRecompositionRule()

  fun test_plainCollectAsState_shouldViolate() {
    assertEquals(1, count("val s = flow.collectAsState()"))
  }

  fun test_collectAsStateWithLifecycle_shouldNotViolate() {
    assertEquals(0, count("val s = flow.collectAsStateWithLifecycle()"))
  }

  fun test_customCollectAsStateListPrefix_shouldNotViolate() {
    assertEquals(0, count("val s = flow.collectAsStateList()"))
  }

  fun test_customCollectAsStateMapPrefix_shouldNotViolate() {
    assertEquals(0, count("val s = flow.collectAsStateMap()"))
  }

  fun test_collectAsStateWithInitialArg_shouldViolate() {
    assertEquals(1, count("val s = flow.collectAsState(initial = 0)"))
  }

  private fun count(body: String): Int {
    val fn = configure("annotation class Composable\n@Composable\nfun Screen() { $body }")
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Screen" }
  }
}
