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
package io.androidpoet.composeguard.rules.naming

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.androidpoet.composeguard.rules.AnalysisContext
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class NamingFalsePositiveTest : BasePlatformTestCase() {

  private val eventNaming = EventParameterNamingRule()
  private val annotationNaming = ComposableAnnotationNamingRule()
  private val multipreview = MultipreviewNamingRule()

  // ----- EventParameterNaming -----

  fun test_event_presentTenseNounEndingInEd_shouldNotViolate() {
    val fn = configureFn(
      """
        annotation class Composable
        @Composable
        fun Player(onSpeed: () -> Unit, onFeed: () -> Unit, onProceed: () -> Unit, onNeed: () -> Unit) {}
      """.trimIndent(),
    )
    assertEmpty(eventNaming.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)))
  }

  fun test_event_genuinePastTense_shouldViolate() {
    val fn = configureFn(
      """
        annotation class Composable
        @Composable
        fun Widget(onClicked: () -> Unit) {}
      """.trimIndent(),
    )
    assertEquals(1, eventNaming.analyzeFunction(fn, AnalysisContext(fn.containingKtFile)).size)
  }

  // ----- ComposableAnnotationNaming -----

  fun test_annotation_composableBearingAnnotation_shouldNotViolate() {
    // A plain @Composable-tagged annotation is NOT a target marker; it must not be flagged.
    val cls = configureClass(
      """
        annotation class Composable

        @Composable
        annotation class MyPreview
      """.trimIndent(),
      "MyPreview",
    )
    assertEmpty(annotationNaming.analyzeClass(cls, AnalysisContext(cls.containingKtFile)))
  }

  fun test_annotation_targetMarkerWithoutSuffix_shouldViolate() {
    val cls = configureClass(
      """
        annotation class ComposableTargetMarker

        @ComposableTargetMarker
        annotation class GoogleMap
      """.trimIndent(),
      "GoogleMap",
    )
    assertEquals(1, annotationNaming.analyzeClass(cls, AnalysisContext(cls.containingKtFile)).size)
  }

  // ----- MultipreviewNaming -----

  fun test_multipreview_stackedPreviewFunction_isNotAnnotationClass_shouldNotViolate() {
    // A composable function stacking @Preview is not a multipreview annotation; analyzeClass only.
    val cls = configureClass(
      """
        annotation class Preview

        @Preview
        @Preview
        annotation class PreviewScreenSizes
      """.trimIndent(),
      "PreviewScreenSizes",
    )
    assertEmpty(multipreview.analyzeClass(cls, AnalysisContext(cls.containingKtFile)))
  }

  fun test_multipreview_annotationWithoutPreviewInName_shouldViolate() {
    val cls = configureClass(
      """
        annotation class Preview

        @Preview
        @Preview
        annotation class Devices
      """.trimIndent(),
      "Devices",
    )
    assertEquals(1, multipreview.analyzeClass(cls, AnalysisContext(cls.containingKtFile)).size)
  }

  private fun configureFn(code: String): KtNamedFunction {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java).first()
  }

  private fun configureClass(code: String, name: String): KtClass {
    val file = myFixture.configureByText("Sample.kt", code) as KtFile
    return PsiTreeUtil.findChildrenOfType(file, KtClass::class.java).first { it.name == name }
  }
}
