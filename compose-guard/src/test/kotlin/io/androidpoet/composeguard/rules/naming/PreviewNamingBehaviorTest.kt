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
package io.androidpoet.composeguard.rules.naming

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/** Behavioral coverage for [PreviewNamingRule] — runs the rule and asserts whether it fires. */
class PreviewNamingBehaviorTest : BasePlatformTestCase() {

  private val rule = PreviewNamingRule()

  fun test_previewNameWithoutPreviewWord_shouldViolate() {
    assertEquals(1, analyze("@Preview @Composable fun ProfileCard() {}"))
  }

  fun test_previewNameWithPreviewWord_shouldNotViolate() {
    assertEquals(0, analyze("@Preview @Composable fun ProfileCardPreview() {}"))
  }

  fun test_nonPreviewFunction_shouldNotBeChecked() {
    assertEquals(0, analyze("@Composable fun ProfileCard() {}"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nannotation class Preview\n$code",
    ) as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first()
    return rule.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size
  }
}
