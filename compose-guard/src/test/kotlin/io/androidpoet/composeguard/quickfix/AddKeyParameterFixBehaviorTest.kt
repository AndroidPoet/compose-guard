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

class AddKeyParameterFixBehaviorTest : BasePlatformTestCase() {

  private val fix = AddKeyParameterFix()

  fun test_itemsPositional_addsKeyAndKeepsContent() {
    val text = stripped(applyTo("items(users) { user -> Text(user.name) }"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("key={user->user.id}"))
    assertTrue("content lambda must be preserved", text.contains("Text(user.name)"))
  }

  fun test_itemsIndexed_addsTwoArgKey() {
    val text = stripped(applyTo("itemsIndexed(users) { index, user -> Text(user.name) }"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("key={_,user->user.id}"))
    assertTrue(text, text.contains("Text(user.name)"))
  }

  fun test_itemsWithContentType_keepsContentTypeAndContent() {
    val text = stripped(applyTo("items(users, contentType = { \"row\" }) { Text(it.name) }"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("key={it.id}"))
    assertTrue(text, text.contains("contentType={\"row\"}"))
    assertTrue(text, text.contains("Text(it.name)"))
  }

  /** Regression: content passed as a named itemContent arg must not be dropped. */
  fun test_namedItemContent_contentPreserved() {
    val text = stripped(applyTo("items(users, itemContent = { Text(it.name) })"))
    assertNoSyntaxErrors()
    assertTrue(text, text.contains("key="))
    assertTrue("named itemContent must not be dropped", text.contains("Text(it.name)"))
  }

  // The IDE reformats the rewritten call, so strip all whitespace before substring checks.
  private fun stripped(text: String): String = text.replace(Regex("\\s+"), "")

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun applyTo(itemsCall: String): String {
    val code = "annotation class Composable\n@Composable\nfun L(users: List<U>) { LazyColumn { $itemsCall } }"
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    val call = PsiTreeUtil.findChildrenOfType(file, KtCallExpression::class.java)
      .first { it.calleeExpression?.text == "items" || it.calleeExpression?.text == "itemsIndexed" }
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      call.calleeExpression ?: call,
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
