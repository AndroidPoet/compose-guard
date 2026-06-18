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
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty

class HoistStateFixBehaviorTest : BasePlatformTestCase() {

  fun test_hoist_noOtherParams_producesValidParams() {
    val text = applyHoist(
      """
        @Composable
        fun Widget() {
          val query = remember { mutableStateOf("") }
          Child(text = query)
        }
      """.trimIndent(),
      "query",
    )
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("query: String"))
    assertTrue(text, text.contains("onQueryChange: (String) -> Unit"))
    assertFalse("hoisted property should be removed", text.contains("remember {"))
  }

  fun test_hoist_withTrailingModifier_keepsModifierLast() {
    val text = applyHoist(
      """
        @Composable
        fun Widget(title: String, modifier: Modifier = Modifier) {
          val query = remember { mutableStateOf("") }
          Child(text = query, modifier = modifier)
        }
      """.trimIndent(),
      "query",
    )
    assertNoSyntaxErrors()
    // required params before the optional modifier.
    assertTrue(text, text.indexOf("onQueryChange") < text.indexOf("modifier: Modifier"))
  }

  fun test_hoist_intState_infersIntType() {
    val text = applyHoist(
      """
        @Composable
        fun Widget() {
          val count = remember { mutableIntStateOf(0) }
          Child(value = count)
        }
      """.trimIndent(),
      "count",
    )
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("count: Int"))
    assertTrue(text, text.contains("onCountChange: (Int) -> Unit"))
  }

  private fun assertNoSyntaxErrors() {
    val file = myFixture.file
    val error = PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${file.text}", error)
  }

  private fun applyHoist(code: String, propertyName: String): String {
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nclass Modifier { companion object }\n$code",
    ) as KtFile
    val property = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java).first { it.name == propertyName }
    val fix = HoistStateFix(propertyName)
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      property.nameIdentifier ?: property,
      "test",
      arrayOf(fix),
      ProblemHighlightType.WARNING,
      true,
      false,
    )
    WriteCommandAction.runWriteCommandAction(project) {
      fix.applyFix(project, descriptor)
    }
    return file.text
  }
}
