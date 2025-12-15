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
@file:Suppress("unused", "UNUSED_PARAMETER")

package io.androidpoet.composeguard.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════════════════
// NAMING RULES DEMO
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun NamingRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test naming conventions", modifier = Modifier.padding(bottom = 8.dp))
    GoodButton()
  }
}

// -------------------------------------------------------------------------------
// 1. ComposableNaming - Should start with uppercase
// -------------------------------------------------------------------------------

/** BAD: Composable name starts with lowercase */
@Composable
fun badButton() {
  Button(onClick = {}) { Text("Click") }
}

/** GOOD: Composable name starts with uppercase */
@Composable
fun GoodButton(modifier: Modifier = Modifier) {
  Button(onClick = {}, modifier = modifier) { Text("Click") }
}

// -------------------------------------------------------------------------------
// 2. CompositionLocalNaming - Should start with "Local"
// -------------------------------------------------------------------------------

/** BAD: CompositionLocal doesn't start with "Local" */
val CurrentUser = compositionLocalOf { "Guest" }

/** GOOD: CompositionLocal starts with "Local" */
val LocalUser = compositionLocalOf { "Guest" }

// -------------------------------------------------------------------------------
// 3. PreviewNaming - Should contain "Preview"
// -------------------------------------------------------------------------------

/** BAD: Preview function doesn't contain "Preview" in name */
@Preview
@Composable
fun ButtonTest() {
  Text("Test")
}

/** GOOD: Preview function contains "Preview" in name and is private */
@Preview
@Composable
private fun ButtonPreview() {
  Text("Test")
}

// -------------------------------------------------------------------------------
// 4. MultipreviewNaming - Should start with "Previews"
// -------------------------------------------------------------------------------

/** BAD: Multipreview annotation doesn't start with "Previews" */
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemePreview

/** GOOD: Multipreview annotation starts with "Previews" */
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewsTheme

// -------------------------------------------------------------------------------
// 5. ComposableAnnotationNaming - Should end with "Composable"
// -------------------------------------------------------------------------------

// NOTE: @Composable cannot be applied to annotation classes in actual code.
// This rule is intended for custom composable annotations that wrap @Composable.
// Example of what the rule checks for:
// annotation class MyScreen // BAD: should be MyScreenComposable

// -------------------------------------------------------------------------------
// 6. EventParameterNaming - No past tense
// -------------------------------------------------------------------------------

/** BAD: Event parameter uses past tense */
@Composable
fun BadEvent(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

/** GOOD: Event parameter uses present tense */
@Preview
@Composable
 fun GoodEvent(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

@Composable
fun BFormField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  errorMessage: String = "",
  enabled: Boolean = true,
  placeholder: String = "",
) {
}

// ✅ CORRECT ORDER
@Composable
fun AFormField(
  label: String, // required
  value: String, // required
  onValueChange: (String) -> Unit, // required
  modifier: Modifier = Modifier, // FIRST optional ✅
  isError: Boolean = false, // optional after modifier
  errorMessage: String = "",
  enabled: Boolean = true,
  placeholder: String = "",
) {}
