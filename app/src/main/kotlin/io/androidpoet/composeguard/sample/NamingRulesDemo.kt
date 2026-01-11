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

@Composable
fun NamingRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test naming conventions", modifier = Modifier.padding(bottom = 8.dp))
    GoodButton()
  }
}

@Composable
fun badButton() {
  Button(onClick = {}) { Text("Click") }
}

@Composable
fun GoodButton(modifier: Modifier = Modifier) {
  Button(onClick = {}, modifier = modifier) { Text("Click") }
}

val CurrentUser = compositionLocalOf { "Guest" }

val LocalUser = compositionLocalOf { "Guest" }

@Preview
@Composable
fun ButtonTest() {
  Text("Test")
}

@Preview
@Composable
private fun ButtonPreview() {
  Text("Test")
}

@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemePreview

@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewsTheme

@Composable
fun BadEvent(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

@Preview
@Composable
 fun GoodEvent(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

@Composable
fun BFormField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  errorMessage: String = "",
  isError: Boolean = false,
  enabled: Boolean = true,
  placeholder: String = ""
) {
}

@Composable
fun AFormField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  errorMessage: String = "",
  enabled: Boolean = true,
  placeholder: String = "",
) {}
