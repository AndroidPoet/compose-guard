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
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class RenameFixBehaviorTest : BasePlatformTestCase() {

  fun test_renameComposable_renamesDeclarationAndCallSites() {
    val fix = RenameComposableFix("MyButton")
    val file = configure(
      "@Composable fun myButton() {}\n" +
        "@Composable fun Screen() { myButton() }",
    )
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "myButton" }
    val text = stripped(apply(fix, fn))
    assertNoSyntaxErrors()
    assertTrue("declaration not renamed:\n$text", text.contains("funMyButton()"))
    assertTrue("call site not renamed:\n$text", text.contains("{MyButton()}"))
    assertFalse("old name still present as a call:\n$text", text.contains("myButton("))
  }

  fun test_renameComposable_doesNotTouchSameNamedStringLiteral() {
    val fix = RenameComposableFix("MyButton")
    val file = configure(
      "@Composable fun myButton() {}\n" +
        "val label = \"myButton\"\n" +
        "@Composable fun Screen() { myButton() }",
    )
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "myButton" }
    val text = stripped(apply(fix, fn))
    assertNoSyntaxErrors()
    // Resolution-based rename must leave the unrelated string content alone.
    assertTrue("string literal was altered:\n$text", text.contains("\"myButton\""))
    assertTrue("call site not renamed:\n$text", text.contains("{MyButton()}"))
  }

  fun test_addLocalPrefix_renamesDeclarationAndReferences() {
    val fix = AddLocalPrefixFix("ContentColor")
    val file = myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\n" +
        "class ProvidableCompositionLocal<T> { val current: T get() = TODO() }\n" +
        "fun <T> compositionLocalOf(block: () -> T): ProvidableCompositionLocal<T> = TODO()\n" +
        "val ContentColor = compositionLocalOf { 0 }\n" +
        "@Composable fun S() { val c = ContentColor.current }",
    ) as KtFile
    val property = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java).first { it.name == "ContentColor" }
    val text = stripped(apply(fix, property))
    assertNoSyntaxErrors()
    assertTrue("declaration not renamed:\n$text", text.contains("valLocalContentColor="))
    assertTrue("reference not renamed:\n$text", text.contains("LocalContentColor.current"))
    assertFalse("old name still present:\n$text", text.contains("=ContentColor.current") || text.contains("valContentColor="))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile

  private fun apply(fix: LocalQuickFix, target: PsiElement): String {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return myFixture.file.text
  }
}
