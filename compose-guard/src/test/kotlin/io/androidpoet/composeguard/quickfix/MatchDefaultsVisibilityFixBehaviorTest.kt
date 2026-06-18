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
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class MatchDefaultsVisibilityFixBehaviorTest : BasePlatformTestCase() {

  fun test_privateDefaultsBecomesPublic() {
    val fix = MatchDefaultsVisibilityFix("public")
    val file = configure("private object MyComponentDefaults { val color = 0 }")
    val obj = objNamed(file)
    val text = stripped(apply(fix, obj.nameIdentifier ?: obj))
    assertNoSyntaxErrors()
    assertFalse("private modifier not removed:\n$text", text.contains("privateobjectMyComponentDefaults"))
    assertTrue("object missing:\n$text", text.contains("objectMyComponentDefaults"))
  }

  fun test_publicDefaultsBecomesInternal() {
    val fix = MatchDefaultsVisibilityFix("internal")
    val file = configure("object MyComponentDefaults { val color = 0 }")
    val obj = objNamed(file)
    val text = stripped(apply(fix, obj.nameIdentifier ?: obj))
    assertNoSyntaxErrors()
    assertTrue("internal modifier not added:\n$text", text.contains("internalobjectMyComponentDefaults"))
  }

  fun test_privateDefaultsBecomesInternal() {
    val fix = MatchDefaultsVisibilityFix("internal")
    val file = configure("private object MyComponentDefaults { val color = 0 }")
    val obj = objNamed(file)
    val text = stripped(apply(fix, obj.nameIdentifier ?: obj))
    assertNoSyntaxErrors()
    assertTrue("internal modifier not added:\n$text", text.contains("internalobjectMyComponentDefaults"))
    assertFalse("private modifier not removed:\n$text", text.contains("privateobject") || text.contains("privateinternal"))
  }

  private fun objNamed(file: KtFile): KtObjectDeclaration =
    PsiTreeUtil.findChildrenOfType(file, KtObjectDeclaration::class.java).first { it.name == "MyComponentDefaults" }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText("Sample.kt", code) as KtFile

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
