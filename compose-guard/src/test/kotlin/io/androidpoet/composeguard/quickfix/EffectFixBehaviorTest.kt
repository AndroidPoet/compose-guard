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
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class EffectFixBehaviorTest : BasePlatformTestCase() {

  // Regression guard: addImportsIfNeeded read element.containingKtFile AFTER the element had
  // been replaced inside replaceAllReferencesInEffects, throwing IllegalStateException
  // ("KtElement not inside KtFile"). The lambda reference flagged by the rule is exactly the
  // one that gets replaced, so this reliably reproduced.
  fun test_useRememberUpdatedState_doesNotCrashAndAddsImport() {
    val fix = UseRememberUpdatedStateFix("onTimeout")
    val file = configure(
      "@Composable fun S(onTimeout: () -> Unit) { LaunchedEffect(Unit) { onTimeout() } }",
    )
    // The reference inside the effect lambda — the one the fix replaces.
    val ref = PsiTreeUtil.findChildrenOfType(file, KtNameReferenceExpression::class.java)
      .first { it.getReferencedName() == "onTimeout" && it.parent is KtCallExpression }
    val text = stripped(apply(fix, ref))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("rememberUpdatedState(onTimeout)"))
    assertTrue(text, text.contains("importandroidx.compose.runtime.rememberUpdatedState"))
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
