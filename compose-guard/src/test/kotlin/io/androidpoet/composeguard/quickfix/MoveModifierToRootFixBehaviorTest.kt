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
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtValueArgument

class MoveModifierToRootFixBehaviorTest : BasePlatformTestCase() {

  fun test_movesModifierToRootAndRemovesFromChild() {
    val fix = MoveModifierToRootFix("fillMaxSize")
    val file = configure(
      "@Composable fun S() { Column { Text(\"hi\", modifier = Modifier.fillMaxSize()) } }",
    )
    val modifierArg = PsiTreeUtil.findChildrenOfType(file, KtValueArgument::class.java)
      .first { it.getArgumentName()?.asName?.asString() == "modifier" }
    val text = stripped(apply(fix, modifierArg))
    assertNoSyntaxErrors()

    // Modifier must be moved to the Column, not left on (or duplicated onto) the Text.
    assertTrue("modifier not added to root Column:\n$text", text.contains("Column(modifier=Modifier.fillMaxSize())"))
    assertFalse("modifier still present on child Text:\n$text", text.contains("Text(\"hi\",modifier=Modifier.fillMaxSize())"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nobject Modifier { fun fillMaxSize(): Modifier = this }\n" +
        "fun Column(modifier: Modifier = Modifier, content: () -> Unit) {}\n" +
        "fun Text(text: String, modifier: Modifier = Modifier) {}\n$code",
    ) as KtFile

  private fun apply(fix: LocalQuickFix, target: PsiElement): String {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return myFixture.file.text
  }
}
