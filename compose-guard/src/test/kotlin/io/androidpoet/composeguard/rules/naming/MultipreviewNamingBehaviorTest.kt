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
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

/** Behavioral coverage for [MultipreviewNamingRule] — runs the rule and asserts whether it fires. */
class MultipreviewNamingBehaviorTest : BasePlatformTestCase() {

  private val rule = MultipreviewNamingRule()

  fun test_multipreviewAnnotationWithoutPreviewName_shouldViolate() {
    assertEquals(1, analyze("@Preview annotation class Devices", "Devices"))
  }

  fun test_multipreviewAnnotationWithPreviewName_shouldNotViolate() {
    assertEquals(0, analyze("@Preview annotation class PreviewDevices", "PreviewDevices"))
  }

  fun test_annotationWithoutPreview_shouldNotBeChecked() {
    // No meta @Preview -> not a multipreview, so naming is not enforced.
    assertEquals(0, analyze("annotation class Devices", "Devices"))
  }

  private fun analyze(code: String, className: String): Int {
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Preview\n$code",
    ) as KtFile
    val ktClass = PsiTreeUtil.findChildrenOfType(file, KtClass::class.java).first { it.name == className }
    return rule.analyzeClass(ktClass, AnalysisContext(ktClass.containingKtFile)).size
  }
}
