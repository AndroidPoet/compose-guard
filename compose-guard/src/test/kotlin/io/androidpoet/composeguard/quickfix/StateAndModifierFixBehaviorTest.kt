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
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference

class StateAndModifierFixBehaviorTest : BasePlatformTestCase() {

  fun test_useTypeSpecificState_intReplacement() {
    val fix = UseTypeSpecificStateFix(UseTypeSpecificStateFix.MUTABLE_INT_STATE_OF)
    val file = configure("@Composable fun S() { val c = mutableStateOf(0) }")
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "mutableStateOf" }
    val text = stripped(apply(fix, call.calleeExpression ?: call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("mutableIntStateOf(0)"))
    assertFalse(text, text.contains("mutableStateOf(0)"))
  }

  fun test_addModifierDefaultValue() {
    val fix = AddModifierDefaultValueFix()
    val file = configure("@Composable fun S(modifier: Modifier) { }")
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "S" }
    val param = fn.valueParameters.first { it.name == "modifier" }
    val text = stripped(apply(fix, param))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("modifier:Modifier=Modifier"))
  }

  // Regression guard: these fixes used to call element.containingKtFile AFTER replacing the
  // element, which threw IllegalStateException("KtElement not inside KtFile") because the old
  // element was detached. The fixes now capture the file before the replace.

  fun test_useImmutableCollection_doesNotCrashAndAddsImport() {
    val fix = UseImmutableCollectionFix.forList()
    val file = configure("class Item\n@Composable fun S(items: List<Item>) { }")
    val typeRef = PsiTreeUtil.findChildrenOfType(file, KtTypeReference::class.java)
      .first { it.text.contains("List<Item>") }
    val text = stripped(apply(fix, typeRef))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("ImmutableList<Item>"))
    assertTrue(text, text.contains("importkotlinx.collections.immutable.ImmutableList"))
  }

  fun test_useLifecycleAwareCollector_doesNotCrashAndAddsImport() {
    val fix = UseLifecycleAwareCollectorFix()
    val file = configure(
      "class VM { val state = Any() }\n@Composable fun S(vm: VM) { val s = vm.state.collectAsState() }",
    )
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "collectAsState" }
    val text = stripped(apply(fix, call))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("collectAsStateWithLifecycle()"))
    assertTrue(text, text.contains("importandroidx.lifecycle.compose.collectAsStateWithLifecycle"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun configure(code: String): KtFile =
    myFixture.configureByText("Sample.kt", "annotation class Composable\nclass Modifier\n$code") as KtFile

  private fun apply(fix: LocalQuickFix, target: PsiElement): String {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return myFixture.file.text
  }
}
