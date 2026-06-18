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
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty

class WrapFixBehaviorTest : BasePlatformTestCase() {

  fun test_wrapInRemember_withParamKey_isValid() {
    val text = stripped(applyToInitializer(WrapInRememberFix(), "val names = users.map { it.length }", "names"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("remember(users){") || text.contains("remember(users) {".replace(" ", "")))
    assertTrue(text, text.contains("users.map{it.length}"))
  }

  fun test_wrapInDerivedStateOf_usesByDelegation() {
    val text = stripped(
      applyToInitializer(
        WrapInDerivedStateOfFix(),
        "val showButton = listState.firstVisibleItemIndex > 0",
        "showButton",
      ),
    )
    assertNoSyntaxErrors()
    // derivedStateOf returns State<T>; the property must use `by` so reads stay the unwrapped type.
    assertTrue(text, text.contains("valshowButtonbyremember{derivedStateOf{"))
    assertFalse("must not assign a State<T> with =", text.contains("valshowButton=remember{derivedStateOf"))
  }

  fun test_wrapInDerivedStateOf_keepsTypeAnnotation() {
    val text = stripped(
      applyToInitializer(
        WrapInDerivedStateOfFix(),
        "val showButton: Boolean = listState.firstVisibleItemIndex > 0",
        "showButton",
      ),
    )
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("valshowButton:Booleanbyremember{derivedStateOf{"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun applyToInitializer(fix: LocalQuickFix, decl: String, propName: String): String {
    val code = "annotation class Composable\n@Composable\nfun Screen(users: List<U>, listState: Any) { $decl }"
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    val property = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java).first { it.name == propName }
    val target = property.initializer ?: property
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
    return file.text
  }
}
