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
package io.androidpoet.composeguard.inlay

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Coverage for the inlay-hint violation collection — specifically that it respects suppression, so
 * a suppressed rule does not linger as an inlay hint after its inline highlight is gone.
 */
class ComposeGuardInlayHintsProviderTest : BasePlatformTestCase() {

  fun test_unsuppressedComposable_producesViolations() {
    // A public composable emitting content without a Modifier parameter violates ModifierRequired.
    val fn = configure("@Composable fun UserCard(name: String) { Column { Text(name) } }")
    assertTrue(collectUnsuppressedViolations(fn, AnalysisContext(fn.containingKtFile)).isNotEmpty())
  }

  fun test_suppressedComposable_producesNoViolations() {
    val fn = configure(
      "@Composable @Suppress(\"ModifierRequired\")\nfun UserCard(name: String) { Column { Text(name) } }",
    )
    val remaining = collectUnsuppressedViolations(fn, AnalysisContext(fn.containingKtFile))
    assertTrue(
      "suppressed ModifierRequired must not appear in inlay violations: ${remaining.map { it.rule.id }}",
      remaining.none { it.rule.id == "ModifierRequired" },
    )
  }

  private fun configure(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "UserCard" }
  }
}
