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
@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE")

package io.androidpoet.composeguard.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow

// ═══════════════════════════════════════════════════════════════════════════════
// STATE RULES DEMO
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun StateRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test state management patterns", modifier = Modifier.padding(bottom = 8.dp))
    Remembered()
  }
}

// -------------------------------------------------------------------------------
// 14. RememberState - State should be remembered
// -------------------------------------------------------------------------------

/** BAD: State not wrapped in remember */
@Composable
fun NotRemembered() {
  var count by remember { mutableIntStateOf(0) }
  Button(onClick = { count++ }) { Text("$count") }
}

/** GOOD: State properly remembered */
@Composable
fun Remembered(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Button(onClick = { count++ }, modifier = modifier) { Text("$count") }
}

// -------------------------------------------------------------------------------
// 15. TypeSpecificState - Use mutableIntStateOf for primitives
// -------------------------------------------------------------------------------

/** BAD: Using generic mutableStateOf for Int */
@Composable
fun GenericState(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Text("$count", modifier = modifier)
}

/** GOOD: Using type-specific mutableIntStateOf */
@Composable
fun SpecificState(modifier: Modifier = Modifier) {
  var count by remember { mutableIntStateOf(0) }
  Text("$count", modifier = modifier)
}

// -------------------------------------------------------------------------------
// 16. DerivedStateOfCandidate - Use remember for computed values
// -------------------------------------------------------------------------------

/** BAD: Computed value not remembered - will recompute every recomposition */
@Composable
fun NoRemember(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

/** BAD: Sorted list not remembered */
@Composable
fun NoRememberSorted(items: List<String>, modifier: Modifier = Modifier) {
  val sorted = remember(items) { items.sortedBy { it.length } }
  Column(modifier = modifier) {
    sorted.forEach { Text(it) }
  }
}

/** BAD: Mapped list not remembered */
@Composable
fun NoRememberMapped(items: List<String>, modifier: Modifier = Modifier) {
  val mapped = remember(items) { items.map { it.uppercase() } }
  Column(modifier = modifier) {
    mapped.forEach { Text(it) }
  }
}

/** GOOD: Computed value properly remembered with keys */
@Composable
fun WithRemember(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

/** GOOD: Using derivedStateOf for reactive computations */
@Composable
fun WithDerived(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered by remember(items, query) {
    derivedStateOf { items.filter { it.contains(query) } }
  }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

// -------------------------------------------------------------------------------
// 17. FrequentRecomposition - Remember expensive objects
// -------------------------------------------------------------------------------

/** BAD: Creating new TextStyle on every recomposition */
@Composable
fun ObjectEveryTime(modifier: Modifier = Modifier) {
  val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
  Text("Hello", style = style, modifier = modifier)
}

/** GOOD: TextStyle is remembered */
@Composable
fun ObjectRemembered(modifier: Modifier = Modifier) {
  val style = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
  Text("Hello", style = style, modifier = modifier)
}

/** BAD: Flow collected without lifecycle awareness */
@Composable
fun FlowNoLifecycle(flow: Flow<String>, modifier: Modifier = Modifier) {
  val state by flow.collectAsState(initial = "")
  Text(state, modifier = modifier)
}

// -------------------------------------------------------------------------------
// 18. MutableStateParameter - Use value + callback pattern
// -------------------------------------------------------------------------------

/** BAD: Passing MutableState as parameter */
@Composable
fun MutableParam(count: MutableState<Int>, modifier: Modifier = Modifier) {
  Text("${count.value}", modifier = modifier)
}

/** GOOD: Using value + callback pattern */
@Composable
fun ValueCallback(count: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = { onChange(count + 1) }, modifier = modifier) {
    Text("$count")
  }
}

// -------------------------------------------------------------------------------
// 19. HoistState - Hoist shared state
// -------------------------------------------------------------------------------

/** BAD: State defined here but shared with child */
@Composable
fun SharedState(modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  Header(expanded = expanded, onToggle = { expanded = !expanded })
  if (expanded) Content()
}

/** BAD: State passed to child */
@Composable
fun PassedToChild(modifier: Modifier = Modifier) {
  var query by remember { mutableStateOf("") }
  SearchField(query = query, onQueryChange = { query = it })
}

/** GOOD: Screen-level state is acceptable */
@Composable
fun ScreenLevel(modifier: Modifier = Modifier) {
  var tab by remember { mutableIntStateOf(0) }
  Column(modifier = modifier) { Text("Tab: $tab") }
}

// Helper composables
@Composable
private fun Header(expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text("$expanded") }
}

@Composable
private fun Content(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text("Content") }
}

@Composable
private fun SearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  TextField(value = query, onValueChange = onQueryChange, modifier = modifier)
}
