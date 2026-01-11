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

@Composable
fun StateRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test state management patterns", modifier = Modifier.padding(bottom = 8.dp))
    Remembered()
  }
}

@Composable
fun NotRemembered() {
  var count by remember { mutableIntStateOf(0) }
  Button(onClick = { count++ }) { Text("$count") }
}

@Composable
fun Remembered(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Button(onClick = { count++ }, modifier = modifier) { Text("$count") }
}

@Composable
fun GenericState(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Text("$count", modifier = modifier)
}

@Composable
fun SpecificState(modifier: Modifier = Modifier) {
  var count by remember { mutableIntStateOf(0) }
  Text("$count", modifier = modifier)
}

@Composable
fun NoRemember(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

@Composable
fun NoRememberSorted(items: List<String>, modifier: Modifier = Modifier) {
  val sorted = remember(items) { items.sortedBy { it.length } }
  Column(modifier = modifier) {
    sorted.forEach { Text(it) }
  }
}

@Composable
fun NoRememberMapped(items: List<String>, modifier: Modifier = Modifier) {
  val mapped = remember(items) { items.map { it.uppercase() } }
  Column(modifier = modifier) {
    mapped.forEach { Text(it) }
  }
}

@Composable
fun WithRemember(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

@Composable
fun WithDerived(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered by remember(items, query) {
    derivedStateOf { items.filter { it.contains(query) } }
  }
  Column(modifier = modifier) {
    filtered.forEach { Text(it) }
  }
}

@Composable
fun ObjectEveryTime(modifier: Modifier = Modifier) {
  val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun ObjectRemembered(modifier: Modifier = Modifier) {
  val style = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun FlowNoLifecycle(flow: Flow<String>, modifier: Modifier = Modifier) {
  val state by flow.collectAsState(initial = "")
  Text(state, modifier = modifier)
}

@Composable
fun MutableParam(count: MutableState<Int>, modifier: Modifier = Modifier) {
  Text("${count.value}", modifier = modifier)
}

@Composable
fun ValueCallback(count: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = { onChange(count + 1) }, modifier = modifier) {
    Text("$count")
  }
}

@Composable
fun SharedState(modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  Header(expanded = expanded, onToggle = { expanded = !expanded })
  if (expanded) Content()
}

@Composable
fun PassedToChild(modifier: Modifier = Modifier) {
  var query by remember { mutableStateOf("") }
  SearchField(query = query, onQueryChange = { query = it })
}

@Composable
fun ScreenLevel(modifier: Modifier = Modifier) {
  var tab by remember { mutableIntStateOf(0) }
  Column(modifier = modifier) { Text("Tab: $tab") }
}

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
