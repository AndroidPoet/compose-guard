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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class HoistStateFalsePositiveTest : BasePlatformTestCase() {

  private val rule = HoistStateRule()

  /** A state named `value` must not be flagged just because a child has a `value =` parameter. */
  fun test_stateNameMatchesChildParamName_shouldNotViolate() {
    assertEmpty(
      analyze(
        """
          val value = remember { mutableStateOf(0f) }
          Column {
            Slider(value = 0f, onValueChange = {})
            Text("hello")
          }
        """,
      ),
    )
  }

  /** A state named `count` must not be flagged because child names contain `count` as a substring. */
  fun test_stateNameSubstringOfChildName_shouldNotViolate() {
    assertEmpty(
      analyze(
        """
          val count = remember { mutableStateOf(0) }
          Column {
            AccountBadge()
            AccountHeader()
          }
        """,
      ),
    )
  }

  /** State genuinely passed to children must still be flagged. */
  fun test_stateGenuinelyPassedToChildren_shouldViolate() {
    assertTrue(
      analyze(
        """
          val query = remember { mutableStateOf("") }
          Column {
            SearchField(text = query)
            ResultsList(filter = query)
          }
        """,
      ).isNotEmpty(),
    )
  }

  /** State genuinely reassigned internally must still be flagged (modified-in-callback). */
  fun test_stateGenuinelyModified_shouldViolate() {
    assertTrue(
      analyze(
        """
          val expanded = remember { mutableStateOf(false) }
          Column {
            Button(onClick = { expanded.value = true }) { Text("open") }
            Text("body")
          }
        """,
      ).isNotEmpty(),
    )
  }

  /**
   * State passed to children inside an OVERRIDE must not be flagged: HoistState's fix hoists local
   * state into a new parameter, a signature change the override cannot make.
   */
  fun test_overrideComposable_shouldNotViolate() {
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\n" +
        "interface Panel { @Composable fun Render() }\n" +
        "class P : Panel {\n" +
        "  @Composable override fun Render() {\n" +
        "    val query = remember { mutableStateOf(\"\") }\n" +
        "    Column {\n" +
        "      SearchField(text = query)\n" +
        "      ResultsList(filter = query)\n" +
        "    }\n" +
        "  }\n" +
        "}",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .first { it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
    assertEmpty(rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  private fun analyze(body: String): List<*> {
    val fn = configure("annotation class Composable\n@Composable\nfun Widget() { $body }")
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile))
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Widget" }
  }
}
