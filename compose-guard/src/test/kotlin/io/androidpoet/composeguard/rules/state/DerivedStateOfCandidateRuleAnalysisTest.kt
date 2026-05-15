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

class DerivedStateOfCandidateRuleAnalysisTest : BasePlatformTestCase() {

  private val rule = DerivedStateOfCandidateRule()

  fun test_onRemoveCallbackWithMutableCopies_shouldNotViolate() {
    val function = configureFunction(
      """
        annotation class Composable

        data class Form(
          val photoNames: List<String>,
          val photoBytes: List<ByteArray>,
        )

        @Composable
        fun PhotoStrip(
          photoNames: List<String>,
          photoBytes: List<ByteArray>,
          onRemove: (Int) -> Unit,
        ) = Unit

        @Composable
        fun Screen(form: Form) {
          PhotoStrip(
            photoNames = form.photoNames,
            photoBytes = form.photoBytes,
            onRemove = { index ->
              val updatedNames = form.photoNames.toMutableList().also { it.removeAt(index) }
              val updatedBytes = form.photoBytes.toMutableList().also {
                if (index in it.indices) {
                  it.removeAt(index)
                }
              }
            },
          )
        }
      """.trimIndent(),
    )

    val violations = rule.analyzeFunction(function, AnalysisContext(function.containingKtFile))

    assertEmpty(violations)
  }

  fun test_composableScopeComputedList_shouldViolate() {
    val function = configureFunction(
      """
        annotation class Composable

        @Composable
        fun Screen(items: List<String>) {
          val filtered = items.filter { it.isNotBlank() }
        }
      """.trimIndent(),
    )

    val violations = rule.analyzeFunction(function, AnalysisContext(function.containingKtFile))

    assertEquals(1, violations.size)
    assertTrue(violations.single().message.contains("filtered"))
  }

  private fun configureFunction(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
      .single { it.name == "Screen" }
  }
}
