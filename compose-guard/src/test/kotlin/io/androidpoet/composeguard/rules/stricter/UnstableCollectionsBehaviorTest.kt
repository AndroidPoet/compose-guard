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
package io.androidpoet.composeguard.rules.stricter

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [UnstableCollectionsRule] — runs the rule and asserts whether it fires. */
class UnstableCollectionsBehaviorTest : BasePlatformTestCase() {

  private val rule = UnstableCollectionsRule()

  fun test_listParameter_shouldViolate() {
    assertEquals(1, analyze("@Composable fun S(items: List<Int>) {}"))
  }

  fun test_mapParameter_shouldViolate() {
    assertEquals(1, analyze("@Composable fun S(items: Map<String, Int>) {}"))
  }

  fun test_immutableListParameter_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(items: ImmutableList<Int>) {}"))
  }

  fun test_nonCollectionParameter_shouldNotViolate() {
    assertEquals(0, analyze("@Composable fun S(count: Int) {}"))
  }

  fun test_overrideWithListParameter_shouldNotViolate() {
    // The fix rewrites the parameter type (List -> ImmutableList), a signature change an override
    // cannot make. Matches the override exemption across the signature-changing-fix rules.
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\n" +
        "interface P { @Composable fun S(items: List<Int>) }\n" +
        "class C : P { @Composable override fun S(items: List<Int>) {} }",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
    assertEquals(0, rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
