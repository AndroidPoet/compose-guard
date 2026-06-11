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
package io.androidpoet.composeguard.linemarker

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class ComposeGuardLineMarkerProviderTest : BasePlatformTestCase() {

  private val provider = ComposeGuardLineMarkerProvider()

  fun test_unsuppressedViolation_hasGutterMarker() {
    // A public composable that emits content without a Modifier parameter violates ModifierRequired.
    val nameId = nameIdentifier(
      "@Composable\nfun UserCard(name: String) { Column { Text(name) } }",
    )
    assertNotNull(provider.getLineMarkerInfo(nameId))
  }

  fun test_suppressedViolation_hasNoGutterMarker() {
    // With the rule suppressed, the inline annotation disappears — the gutter icon must too, instead
    // of still showing a stale violation color and count.
    val nameId = nameIdentifier(
      "@Composable @Suppress(\"ModifierRequired\")\nfun UserCard(name: String) { Column { Text(name) } }",
    )
    assertNull(provider.getLineMarkerInfo(nameId))
  }

  private fun nameIdentifier(code: String): PsiElement {
    val file = myFixture.configureByText("Sample.kt", "annotation class Composable\n$code") as KtFile
    val fn = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first { it.name == "UserCard" }
    return fn.nameIdentifier!!
  }
}
