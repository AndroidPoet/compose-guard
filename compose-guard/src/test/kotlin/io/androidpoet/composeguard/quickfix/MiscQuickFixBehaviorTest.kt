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
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

class MiscQuickFixBehaviorTest : BasePlatformTestCase() {

  fun test_makePreviewPrivate() {
    val fix = MakePreviewPrivateFix()
    val file = configure("@Composable fun PreviewX() { }")
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "PreviewX" }
    val text = stripped(apply(fix, fn))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("privatefunPreviewX()"))
  }

  fun test_makePreviewPrivate_replacesInternal() {
    val fix = MakePreviewPrivateFix()
    val file = configure("@Composable internal fun PreviewX() { }")
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "PreviewX" }
    val text = stripped(apply(fix, fn))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("privatefunPreviewX()"))
    assertFalse(text, text.contains("internal"))
  }

  fun test_useMaterial3_rewritesImport() {
    val fix = UseMaterial3Fix()
    val file = myFixture.configureByText(
      "Sample.kt",
      "import androidx.compose.material.Button\nfun x() {}",
    ) as KtFile
    val import = PsiTreeUtil.findChildrenOfType(file, KtImportDirective::class.java).first()
    val text = stripped(apply(fix, import))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("importandroidx.compose.material3.Button"))
    assertFalse(text, text.contains("material.Button"))
  }

  fun test_addLambdaAsEffectKey_replacesUnitKey() {
    val fix = AddLambdaAsEffectKeyFix("onClick", "LaunchedEffect")
    val file = configure(
      "@Composable fun S(onClick: () -> Unit) { LaunchedEffect(Unit) { onClick() } }",
    )
    val ref = PsiTreeUtil.findChildrenOfType(file, KtNameReferenceExpression::class.java)
      .first { it.getReferencedName() == "onClick" && it.parent is KtCallExpression }
    val text = stripped(apply(fix, ref))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("LaunchedEffect(onClick){onClick()}"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText(
      "Sample.kt",
      "annotation class Composable\nfun LaunchedEffect(key: Any?, block: () -> Unit) {}\n$code",
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
