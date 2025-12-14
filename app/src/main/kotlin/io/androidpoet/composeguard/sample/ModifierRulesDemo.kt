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

// ═══════════════════════════════════════════════════════════════════════════════
// MODIFIER RULES DEMO
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModifierRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test modifier patterns", modifier = Modifier.padding(bottom = 8.dp))
    WithModifier()
  }
}

// -------------------------------------------------------------------------------
// 7. ModifierRequired - Should have modifier parameter
// -------------------------------------------------------------------------------

/** BAD: No modifier parameter */
@Composable
fun NoModifier(modifier: Modifier = Modifier) {
  Column { Text("Content") }
}

/** GOOD: Has modifier parameter with default */
@Composable
fun WithModifier(modifier: Modifier = Modifier) {
  Column(modifier = modifier) { Text("Content") }
}

// -------------------------------------------------------------------------------
// 8. ModifierDefaultValue - Modifier should have default
// -------------------------------------------------------------------------------

/** BAD: Modifier parameter has no default value */
@Composable
fun NoDefault(modifier: Modifier) {
  Column(modifier = modifier) { Text("Card") }
}

// -------------------------------------------------------------------------------
// 9. ModifierNaming - Should be named "modifier"
// -------------------------------------------------------------------------------

/** BAD: Modifier parameter not named "modifier" */
@Composable
fun WrongName(cardModifier: Modifier = Modifier) {
  Column(modifier = cardModifier) { Text("Card") }
}

// -------------------------------------------------------------------------------
// 10. ModifierTopMost - Modifier on root element
// -------------------------------------------------------------------------------

/** BAD: Modifier not applied to root element */
@Composable
fun NotOnRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box(modifier = modifier) { Text("Content") }
  }
}

/** GOOD: Modifier applied to root element */
@Composable
fun OnRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box { Text("Content") }
  }
}

// -------------------------------------------------------------------------------
// 11. ModifierReuse - Don't reuse on multiple elements
// -------------------------------------------------------------------------------

/** BAD: Same modifier instance used on multiple elements */
@Composable
fun Reused(modifier: Modifier = Modifier) {
  Column {
    Text("A", modifier = modifier)
    Text("B", modifier = modifier)
  }
}

// -------------------------------------------------------------------------------
// 12. ModifierOrder - Chain order matters
// -------------------------------------------------------------------------------

/** BAD: padding before clickable (click area includes padding) */
@Composable
fun BadOrder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.clickable { }.padding(16.dp),
  ) { Text("Card") }
}

/** GOOD: clickable before padding (padding is visual only) */
@Composable
fun GoodOrder(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clickable { }
      .padding(16.dp),
  ) { Text("Card") }
}

// -------------------------------------------------------------------------------
// 13. AvoidComposed - Use Modifier.Node instead
// -------------------------------------------------------------------------------

/** BAD: Using Modifier.composed (prefer Modifier.Node) */
fun Modifier.badBorder() = composed {
  this.border(1.dp, Color.Black)
}
