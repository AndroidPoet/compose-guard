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
package io.androidpoet.composeguard.rules.modifiers

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [AvoidComposedRule] — runs the rule and asserts whether it fires. */
class AvoidComposedBehaviorTest : BasePlatformTestCase() {

  private val rule = AvoidComposedRule()

  // The rule only analyzes Modifier extension factories (fun Modifier.x()), so every case is one.

  fun test_composedWithLambda_shouldViolate() {
    // Canonical deprecated usage: `composed { ... }` inside a Modifier extension factory.
    assertEquals(1, analyze("fun Modifier.target(): Modifier = composed { this }"))
  }

  fun test_composedWithoutLambda_shouldNotViolate() {
    // A bare composed() call (no factory lambda) is not the deprecated composed { }.
    assertEquals(0, analyze("fun Modifier.target(): Modifier = composed()"))
  }

  fun test_noComposedCall_shouldNotViolate() {
    assertEquals(0, analyze("fun Modifier.target(): Modifier = then()"))
  }

  fun test_nonModifierExtension_shouldNotBeChecked() {
    // No Modifier receiver -> the rule does not analyze it at all.
    assertEquals(0, analyze("fun target(): Int { val m = Modifier().composed { Modifier() }; return 0 }"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText(
      "Sample.kt",
      "class Modifier\n" +
        "fun Modifier.composed(block: Modifier.() -> Modifier): Modifier = this\n" +
        "fun Modifier.composed(): Modifier = this\n" +
        "fun Modifier.then(): Modifier = this\n$code",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "target" }
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
