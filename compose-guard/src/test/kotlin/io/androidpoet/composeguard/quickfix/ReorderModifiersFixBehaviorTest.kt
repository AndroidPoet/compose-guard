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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

class ReorderModifiersFixBehaviorTest : BasePlatformTestCase() {

  private val fix = ReorderModifiersFix("padding", "clickable")

  fun test_paddingBeforeClickable_swapped() {
    val text = stripped(applyTo("Modifier.padding(16.dp).clickable { }", "padding"))
    assertNoSyntaxErrors()
    // clickable must now come before padding.
    assertTrue(text, text.indexOf("clickable") < text.indexOf("padding"))
  }

  fun test_surroundingModifiersPreserved() {
    val text = stripped(applyTo("Modifier.fillMaxWidth().padding(16.dp).clickable { }.background(red)", "padding"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("fillMaxWidth()"))
    assertTrue(text, text.contains("background(red)"))
    assertTrue(text, text.indexOf("clickable") < text.indexOf(".padding"))
  }

  /** offset must NOT be reordered — it does not reduce the touch target (matches the rule). */
  fun test_offsetNotReordered() {
    val text = stripped(applyTo("Modifier.offset(x=4.dp).padding(16.dp).clickable { }", "padding"))
    assertNoSyntaxErrors()
    // offset stays first; only padding moves after clickable.
    assertTrue(text, text.indexOf("offset") < text.indexOf("clickable"))
    assertTrue(text, text.indexOf("clickable") < text.indexOf(".padding"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun applyTo(chain: String, boundReducingCallee: String): String {
    val code = "annotation class Composable\n@Composable\nfun Foo() { Box(modifier = $chain) }"
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    // Target the bound-reducing call (what the rule flags).
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == boundReducingCallee }
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      call,
      "test",
      arrayOf(fix),
      ProblemHighlightType.WARNING,
      true,
      false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return file.text
  }
}
