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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ModifierRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test modifier patterns", modifier = Modifier.padding(bottom = 8.dp))
    WithModifier()
  }
}

@Composable
fun NoModifier(modifier: Modifier = Modifier) {
  Column { Text("Content") }
}

@Composable
fun WithModifier(modifier: Modifier = Modifier) {
  Column(modifier = modifier) { Text("Content") }
}

@Composable
fun NoDefault(modifier: Modifier) {
  Column(modifier = modifier) { Text("Card") }
}

@Composable
fun WrongName(cardModifier: Modifier = Modifier) {
  Column(modifier = cardModifier) { Text("Card") }
}

@Composable
fun NotOnRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box(modifier = modifier) { Text("Content") }
  }
}

@Composable
fun OnRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box { Text("Content") }
  }
}

@Composable
fun Reused(modifier: Modifier = Modifier) {
  Column {
    Text("A", modifier = modifier)
    Text("B", modifier = modifier)
  }
}

@Composable
fun BadOrder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.clickable { }.padding(16.dp),
  ) { Text("Card") }
}

@Composable
fun GoodOrder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clickable { }
      .padding(16.dp),
  ) { Text("Card") }
}

fun Modifier.badBorder() = composed {
  this.border(1.dp, Color.Black)
}
