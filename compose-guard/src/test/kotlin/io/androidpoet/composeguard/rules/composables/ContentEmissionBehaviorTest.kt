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

/** Behavioral coverage for [ContentEmissionRule] — runs the rule and asserts whether it fires. */
class ContentEmissionBehaviorTest : BasePlatformTestCase() {

  private val rule = ContentEmissionRule()

  fun test_emitsContentAndReturnsValue_shouldViolate() {
    // Emits UI (Text) AND returns a value — the rule's "emit OR return, not both" case.
    assertEquals(1, analyze("@Composable fun S(): Int { Text(\"x\"); return 5 }"))
  }

  fun test_returnsValueWithoutEmitting_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(): Int { return 5 }"))
  }

  fun test_emitsContentReturningUnit_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S() { Text(\"x\") }"))
  }

  fun test_emitsContentReturningFullyQualifiedUnit_shouldNotViolate() {
    // `: kotlin.Unit` is still Unit — an emitter, not an emit-and-return. Regression guard for
    // returnsUnit() only recognizing the bare "Unit" spelling.
    assertEquals(0, analyze("@Composable fun S(): kotlin.Unit { Text(\"x\") }"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nfun Text(s: String) {}\n$code",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
