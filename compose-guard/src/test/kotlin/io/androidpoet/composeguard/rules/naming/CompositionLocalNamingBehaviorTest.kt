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
import org.jetbrains.kotlin.psi.KtProperty

/** Behavioral coverage for [CompositionLocalNamingRule] — runs the rule and asserts whether it fires. */
class CompositionLocalNamingBehaviorTest : BasePlatformTestCase() {

  private val rule = CompositionLocalNamingRule()

  fun test_compositionLocalWithoutLocalPrefix_shouldViolate() {
    assertEquals(1, analyze("val ContentColor = compositionLocalOf { 0 }"))
  }

  fun test_compositionLocalWithLocalPrefix_shouldNotViolate() {
    assertEquals(0, analyze("val LocalContentColor = compositionLocalOf { 0 }"))
  }

  fun test_staticCompositionLocalWithoutLocalPrefix_shouldViolate() {
    assertEquals(1, analyze("val Theme = staticCompositionLocalOf { 0 }"))
  }

  fun test_ordinaryProperty_shouldNotBeChecked() {
    // Not a CompositionLocal, so the rule does not analyze it even without a Local prefix.
    assertEquals(0, analyze("val ContentColor = 0"))
  }

  fun test_stringLiteralMentioningFactory_shouldNotBeChecked() {
    // The initializer text contains "compositionLocalOf" but it's just a string — not a
    // CompositionLocal. Regression guard against the old substring-based detection.
    assertEquals(0, analyze("val ContentColor = \"see compositionLocalOf docs\""))
  }

  fun test_callableReferenceToFactory_shouldNotBeChecked() {
    // `::compositionLocalOf` is a function reference, not a CompositionLocal value.
    assertEquals(0, analyze("val ContentColor = ::staticCompositionLocalOf"))
  }

  private fun analyze(code: String): Int {
    val file = myFixture.configureByText(
      "Sample.kt",
      "fun <T> compositionLocalOf(block: () -> T): T = block()\n" +
        "fun <T> staticCompositionLocalOf(block: () -> T): T = block()\n$code",
    ) as KtFile
    val property = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java)
      .first { it.name == "ContentColor" || it.name == "LocalContentColor" || it.name == "Theme" }
    return rule.analyzeProperty(property, AnalysisContext(property.containingKtFile)).size
  }
}
