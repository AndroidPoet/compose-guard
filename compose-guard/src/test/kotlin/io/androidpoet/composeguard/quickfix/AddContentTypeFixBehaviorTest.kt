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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

class AddContentTypeFixBehaviorTest : BasePlatformTestCase() {

  private val fix = AddContentTypeFix()

  fun test_item_addsContentTypeAndKeepsContent() {
    val text = stripped(applyTo("item { Text(\"h\") }", "item"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("contentType=\"contentType1\""))
    assertTrue(text, text.contains("Text(\"h\")"))
  }

  fun test_items_addsLambdaContentTypeAndKeepsContent() {
    val text = stripped(applyTo("items(data) { Text(it) }", "items"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("items=data"))
    assertTrue(text, text.contains("contentType={_->\"contentType1\"}"))
    assertTrue(text, text.contains("Text(it)"))
  }

  fun test_itemsIndexed_addsTwoArgContentType() {
    val text = stripped(applyTo("itemsIndexed(data) { i, x -> Text(x) }", "itemsIndexed"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("contentType={_,_->\"contentType1\"}"))
    assertTrue(text, text.contains("Text(x)"))
  }

  /** Regression (parallel to the AddKey bug): named itemContent must not be dropped. */
  fun test_namedItemContent_contentPreserved() {
    val text = stripped(applyTo("items(data, itemContent = { Text(it) })", "items"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("contentType="))
    assertTrue("named itemContent must not be dropped", text.contains("Text(it)"))
  }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun applyTo(itemCall: String, callee: String): String {
    val code = "annotation class Composable\n@Composable\nfun L(data: List<U>) { LazyColumn { $itemCall } }"
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == callee }
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      call.calleeExpression ?: call, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
    return file.text
  }
}
