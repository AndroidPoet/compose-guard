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

class AddExplicitParameterFixBehaviorTest : BasePlatformTestCase() {

  // Hoist an implicitly-injected ViewModel to a defaulted parameter. The local `vm` becomes the
  // parameter `viewModel`, so the property must be removed, the parameter added with the call as
  // its default value, and the body usage `vm.load()` rewritten to `viewModel.load()`.
  fun test_hoistsViewModelToParameterAndRewritesUsages() {
    val fix = AddExplicitParameterFix("MyViewModel", "MyViewModel")
    val file = configure(
      "@Composable fun MyScreen() { val vm = viewModel<MyViewModel>(); vm.load() }",
    )
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "viewModel" }
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()

    assertTrue("parameter not added:\n$text", text.contains("MyScreen(viewModel:MyViewModel=viewModel<MyViewModel>())"))
    assertTrue("body usage not rewritten:\n$text", text.contains("viewModel.load()"))
    assertFalse("local property not removed:\n$text", text.contains("valvm="))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nclass MyViewModel { fun load() {} }\nfun <T> viewModel(): T = TODO()\n$code",
    ) as KtFile

  private fun apply(fix: LocalQuickFix, target: PsiElement): String {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target,
      "test",
      arrayOf(fix),
      ProblemHighlightType.WARNING,
      true,
      false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return myFixture.file.text
  }
}
