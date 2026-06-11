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

class AddModifierParameterFixBehaviorTest : BasePlatformTestCase() {

  private val fix = AddModifierParameterFix()

  fun test_noParams_appendsModifier() {
    val text = applyTo("@Composable fun Foo() { Text(\"x\") }", "Foo")
    assertContainsModifier(text)
    assertNoSyntaxErrors()
  }

  fun test_oneRequiredParam_appendsAfter() {
    val text = applyTo("@Composable fun Foo(text: String) { Text(text) }", "Foo")
    assertTrue(text, text.contains("modifier: Modifier = Modifier"))
    // modifier comes after the required param.
    assertTrue(text, text.indexOf("text: String") < text.indexOf("modifier"))
    assertNoSyntaxErrors()
  }

  fun test_trailingContentLambda_modifierInsertedBeforeIt() {
    val text = applyTo(
      "@Composable fun Foo(title: String, content: @Composable () -> Unit) { content() }",
      "Foo",
    )
    assertTrue(text, text.contains("modifier: Modifier = Modifier"))
    // modifier must precede the trailing content lambda so callers can still use trailing syntax.
    assertTrue(text, text.indexOf("modifier") < text.indexOf("content:"))
    assertNoSyntaxErrors()
  }

  fun test_existingOptionalParam_modifierInsertedBeforeIt() {
    val text = applyTo("@Composable fun Foo(enabled: Boolean = true) { Text(\"x\") }", "Foo")
    assertTrue(text, text.contains("modifier: Modifier = Modifier"))
    assertTrue(text, text.indexOf("modifier") < text.indexOf("enabled"))
    assertNoSyntaxErrors()
  }

  private fun assertContainsModifier(text: String) {
    assertTrue(text, text.contains("modifier: Modifier = Modifier"))
  }

  private fun assertNoSyntaxErrors() {
    val file = myFixture.file
    val error = PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${file.text}", error)
  }

  private fun applyTo(code: String, fnName: String): String {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == fnName }
    applyFix(fix, fn.nameIdentifier ?: fn)
    return file.text
  }

  private fun applyFix(fix: LocalQuickFix, target: PsiElement) {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) {
      fix.applyFix(project, descriptor)
    }
  }
}
