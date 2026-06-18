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

class MoveToTrailingLambdaFixBehaviorTest : BasePlatformTestCase() {

  private val fix = MoveToTrailingLambdaFix()

  fun test_lambdaBeforeRequired_movedToEnd() {
    val text = stripped(
      applyTo("@Composable fun Foo(content: @Composable () -> Unit, title: String) { content() }", "content"),
    )
    assertNoSyntaxErrors()
    // content must now be the last parameter.
    assertTrue(text, text.indexOf("title:String") < text.indexOf("content:@Composable"))
  }

  fun test_lambdaBeforeModifierAndOptional_movedToEnd() {
    val text = stripped(
      applyTo(
        "@Composable fun Foo(content: @Composable () -> Unit, modifier: Modifier = Modifier, count: Int = 0) { content() }",
        "content",
      ),
    )
    assertNoSyntaxErrors()
    val iContent = text.indexOf("content:@Composable")
    assertTrue(text, text.indexOf("modifier:Modifier") < iContent)
    assertTrue(text, text.indexOf("count:Int") < iContent)
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun applyTo(code: String, paramName: String): String {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\nclass Modifier\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "Foo" }
    val param = fn.valueParameters.first { it.name == paramName }
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      param,
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
