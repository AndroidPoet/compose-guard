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

class UseLambdaModifierFixBehaviorTest : BasePlatformTestCase() {

  fun test_convertsAlphaToGraphicsLayer() {
    val fix = UseLambdaModifierFix("alpha", "graphicsLayer")
    val file = configure("val m = Modifier.alpha(0.5f)")
    val call = callNamed("alpha")
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("graphicsLayer{alpha=0.5f}"))
  }

  fun test_convertsOffsetToLambda() {
    val fix = UseLambdaModifierFix("offset", "offset")
    val file = configure("val m = Modifier.offset(x = 4.dp, y = 8.dp)")
    val call = callNamed("offset")
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("offset{IntOffset("))
    assertTrue(text, text.contains("roundToPx()"))
  }

  fun test_convertsRotateToGraphicsLayer() {
    val fix = UseLambdaModifierFix("rotate", "graphicsLayer")
    val file = configure("val m = Modifier.rotate(90f)")
    val call = callNamed("rotate")
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("graphicsLayer{rotationZ=90f}"))
  }

  private fun callNamed(name: String): KtCallExpression =
    PsiTreeUtil.findChildrenOfType(myFixture.file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == name }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText(
      "Sample.kt",
      "object Modifier {\n" +
        "  fun alpha(a: Float): Modifier = this\n" +
        "  fun offset(x: Int = 0, y: Int = 0): Modifier = this\n" +
        "  fun rotate(d: Float): Modifier = this\n" +
        "}\nval Int.dp: Int get() = this\n$code",
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
