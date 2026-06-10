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

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile

class Material2FalsePositiveTest : BasePlatformTestCase() {

  private val rule = Material2Rule()

  fun test_rippleImport_shouldNotViolate() {
    val file = configure(
      """
        import androidx.compose.material.ripple.rememberRipple
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeElement(file, AnalysisContext(file)))
  }

  fun test_iconsStarImport_shouldNotViolate() {
    val file = configure(
      """
        import androidx.compose.material.icons.Icons
        import androidx.compose.material.icons.filled.Add
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeElement(file, AnalysisContext(file)))
  }

  fun test_material3Import_shouldNotViolate() {
    val file = configure(
      """
        import androidx.compose.material3.Button
      """.trimIndent(),
    )
    assertEmpty(rule.analyzeElement(file, AnalysisContext(file)))
  }

  fun test_material2ComponentImport_shouldViolate() {
    val file = configure(
      """
        import androidx.compose.material.Button
      """.trimIndent(),
    )
    assertEquals(1, rule.analyzeElement(file, AnalysisContext(file)).size)
  }

  private fun configure(code: String): KtFile {
    return myFixture.configureByText("Sample.kt", code) as KtFile
  }
}
