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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

class WrapInRememberFixBehaviorTest : BasePlatformTestCase() {

  fun test_wrapsExpressionInRemember() {
    val fix = WrapInRememberFix()
    val file = configure("@Composable fun S() { val x = compute() }")
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "compute" }
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("valx=remember{compute()}"))
    assertTrue(text, text.contains("importandroidx.compose.runtime.remember"))
  }

  fun test_wrapsWithParameterKey() {
    val fix = WrapInRememberFix()
    val file = configure("@Composable fun S(id: Int) { val x = compute(id) }")
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "compute" }
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("remember(id){compute(id)}"))
  }

  fun test_doesNotDoubleWrap() {
    val fix = WrapInRememberFix()
    val file = configure("@Composable fun S() { val x = remember { compute() } }")
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "compute" }
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertFalse("must not wrap an expression already inside remember:\n$text", text.contains("remember{remember{"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nfun compute(key: Int = 0): Int = key\n$code",
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
