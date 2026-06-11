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
import org.jetbrains.kotlin.psi.KtNamedFunction

class ReorderParametersFixBehaviorTest : BasePlatformTestCase() {

  private val fix = ReorderParametersFix()

  fun test_scrambledSignature_reordersToComposeGuidelines() {
    val text = applyTo(
      "@Composable fun Foo(content: @Composable () -> Unit, modifier: Modifier = Modifier, title: String) { content() }",
      "Foo",
    )
    assertNoSyntaxErrors()
    // Expected idiomatic order: required (title) -> modifier -> trailing content lambda.
    val iTitle = text.indexOf("title: String")
    val iModifier = text.indexOf("modifier: Modifier")
    val iContent = text.indexOf("content:")
    assertTrue(text, iTitle in 0 until iModifier)
    assertTrue(text, iModifier in 0 until iContent)
  }

  fun test_requiredAfterOptional_movesRequiredFirst() {
    val text = applyTo("@Composable fun Foo(a: Int = 0, b: Int) { }", "Foo")
    assertNoSyntaxErrors()
    assertTrue(text, text.indexOf("b: Int") < text.indexOf("a: Int = 0"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun applyTo(code: String, fnName: String): String {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\nclass Modifier\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == fnName }
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      fn.nameIdentifier ?: fn, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return file.text
  }
}
