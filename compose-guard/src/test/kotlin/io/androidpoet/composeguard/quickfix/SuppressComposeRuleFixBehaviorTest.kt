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
import io.androidpoet.composeguard.rules.isSuppressed
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Behavioral coverage for [SuppressComposeRuleFix] — used by nearly every rule, so the round-trip
 * (apply the fix, then [isSuppressed] recognizes it) and no-syntax-error properties matter.
 */
class SuppressComposeRuleFixBehaviorTest : BasePlatformTestCase() {

  fun test_addsSuppressToComposableFunction() {
    val file = configure("@Composable fun Card() { }")
    apply(SuppressComposeRuleFix("ModifierRequired"), func(file, "Card"))
    assertNoSyntaxErrors()
    val after = func(myFixture.file as KtFile, "Card")
    assertTrue(myFixture.file.text, isSuppressed(after.nameIdentifier!!, "ModifierRequired"))
  }

  fun test_mergesIntoExistingSuppressPreservingPrevious() {
    val file = configure("@Suppress(\"SomethingElse\") @Composable fun Card() { }")
    apply(SuppressComposeRuleFix("ModifierRequired"), func(file, "Card"))
    assertNoSyntaxErrors()
    val after = func(myFixture.file as KtFile, "Card")
    assertTrue(isSuppressed(after.nameIdentifier!!, "ModifierRequired"))
    assertTrue("existing suppression must be preserved", isSuppressed(after.nameIdentifier!!, "SomethingElse"))
    assertEquals(
      "should not add a second @Suppress annotation",
      1,
      after.annotationEntries.count { it.shortName?.asString() == "Suppress" },
    )
  }

  fun test_doesNotDuplicateWhenRuleAlreadySuppressed() {
    val file = configure("@Suppress(\"ModifierRequired\") @Composable fun Card() { }")
    apply(SuppressComposeRuleFix("ModifierRequired"), func(file, "Card"))
    assertNoSyntaxErrors()
    val after = func(myFixture.file as KtFile, "Card")
    val suppress = after.annotationEntries.single { it.shortName?.asString() == "Suppress" }
    assertEquals(
      "ModifierRequired must appear exactly once",
      1,
      suppress.valueArgumentList?.arguments?.count {
        it.getArgumentExpression()?.text?.trim('"') == "ModifierRequired"
      },
    )
  }

  fun test_addsSuppressToFunctionWithoutModifierList() {
    // A plain function with no annotations/modifiers exercises the no-modifier-list branch.
    val file = configure("fun plain() { }")
    apply(SuppressComposeRuleFix("AvoidComposed"), func(file, "plain"))
    assertNoSyntaxErrors()
    val after = func(myFixture.file as KtFile, "plain")
    assertTrue(isSuppressed(after.nameIdentifier!!, "AvoidComposed"))
  }

  private fun func(file: KtFile, name: String): KtNamedFunction =
    PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == name }

  private fun assertNoSyntaxErrors() {
    val error = PsiTreeUtil.findChildOfType(myFixture.file, PsiErrorElement::class.java)
    assertNull("Quick fix produced a syntax error: ${error?.errorDescription} in:\n${myFixture.file.text}", error)
  }

  private fun configure(code: String): KtFile =
    myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile

  private fun apply(fix: LocalQuickFix, target: PsiElement) {
    val manager = InspectionManager.getInstance(project)
    val descriptor = manager.createProblemDescriptor(
      target, "test", arrayOf(fix), ProblemHighlightType.WARNING, true, false,
    )
    WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
  }
}
