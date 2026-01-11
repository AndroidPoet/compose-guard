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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@Composable
fun badNamingButton() {
  Button(onClick = {}) { Text("Click") }
}

@Composable
fun GoodNamingButton(modifier: Modifier = Modifier) {
  Button(onClick = {}, modifier = modifier) { Text("Click") }
}

val CurrentTheme = compositionLocalOf { "Light" }
val LocalTheme = compositionLocalOf { "Light" }

@Preview
@Composable
fun NamingButtonTest() { Text("Test") }

@Preview
@Composable
private fun NamingButtonPreview() { Text("Test") }

@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemeVariants

@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewThemeVariants

@Composable
fun BadEventNaming(onClicked: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClicked, modifier = modifier) { Text("Click") }
}

@Composable
fun GoodEventNaming(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

@Composable
fun NoModifierParam() {
  Column { Text("Content") }
}

@Composable
fun WithModifierParam(modifier: Modifier = Modifier) {
  Column(modifier = modifier) { Text("Content") }
}

@Composable
fun NoModifierDefault(modifier: Modifier) {
  Column(modifier = modifier) { Text("Content") }
}

@Composable
fun WrongModifierName(buttonModifier: Modifier = Modifier) {
  Column(modifier = buttonModifier) { Text("Content") }
}

@Composable
fun ModifierNotOnRoot(modifier: Modifier = Modifier) {
  Column {
    Box(modifier = modifier) { Text("Content") }
  }
}

@Composable
fun ModifierOnRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Box { Text("Content") }
  }
}

@Composable
fun ModifierReused(modifier: Modifier = Modifier) {
  Column {
    Text("A", modifier = modifier)
    Text("B", modifier = modifier)
  }
}

@Composable
fun BadModifierOrder(modifier: Modifier = Modifier) {
  Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Card") }
}

@Composable
fun GoodModifierOrder(modifier: Modifier = Modifier) {
  Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Card") }
}

fun Modifier.avoidComposedBorder() = composed {
  this.border(1.dp, Color.Black)
}

@Composable
fun StateNotRemembered() {
  var count by mutableStateOf(0)
  Button(onClick = { count++ }) { Text("$count") }
}

@Composable
fun StateRemembered(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Button(onClick = { count++ }, modifier = modifier) { Text("$count") }
}

@Composable
fun GenericIntState(modifier: Modifier = Modifier) {
  var count by remember { mutableStateOf(0) }
  Text("$count", modifier = modifier)
}

@Composable
fun SpecificIntState(modifier: Modifier = Modifier) {
  var count by remember { mutableIntStateOf(0) }
  Text("$count", modifier = modifier)
}

@Composable
fun ComputedNotRemembered(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = items.filter { it.contains(query) }
  Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

@Composable
fun ComputedRemembered(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } }
  Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

@Composable
fun ObjectCreatedEveryTime(modifier: Modifier = Modifier) {
  val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun ObjectRememberedProperly(modifier: Modifier = Modifier) {
  val style = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun FlowWithoutLifecycle(flow: Flow<String>, modifier: Modifier = Modifier) {
  val state by flow.collectAsState(initial = "")
  Text(state, modifier = modifier)
}

@Composable
fun MutableStateAsParam(count: MutableState<Int>, modifier: Modifier = Modifier) {
  Text("${count.value}", modifier = modifier)
}

@Composable
fun ValueCallbackPattern(
  count: Int,
  onChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(onClick = { onChange(count + 1) }, modifier = modifier) { Text("$count") }
}

@Composable
fun StateSharedWithChild() {
  var expanded by remember { mutableStateOf(false) }
  ExpandableHeader(expanded = expanded, onToggle = { expanded = !expanded })
  if (expanded) ExpandableContent()
}

@Composable
private fun ExpandableHeader(
  expanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) { Text("Expanded: $expanded") }
}

@Composable
private fun ExpandableContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text("Content") }
}

@Composable
fun BadParamOrdering(enabled: Boolean = true, modifier: Modifier = Modifier, title: String) {
  Column(modifier = modifier) { Text(title) }
}

@Composable
fun GoodParamOrdering(
  title: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Column(modifier = modifier) { Text(title) }
}

@Composable
fun ContentNotTrailingDemo(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

@Composable
fun ContentTrailingDemo(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

@Composable
fun MutableListParameter(items: MutableList<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

@Composable
fun ImmutableListParameter(items: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

@Composable
fun ImplicitDependency() {
  val vm = viewModel<AllRulesViewModel>()
  Text("Data")
}

@Composable
fun ExplicitDependency(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) {
  Text("Data", modifier = modifier)
}

@Composable
fun ParentForwardsViewModel(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    ChildWithViewModel(viewModel = viewModel)
  }
}

@Composable
fun ChildWithViewModel(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) {
  Text("Forwarded VM", modifier = modifier)
}

@Composable
fun ParentPassesData(data: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    ChildWithData(data = data)
  }
}

@Composable
fun ChildWithData(data: String, modifier: Modifier = Modifier) {
  Text(data, modifier = modifier)
}

@Composable
fun EmitsAndReturns(): String {
  Column { Text("Hello") }
  return "World"
}

@Composable
fun OnlyEmits(modifier: Modifier = Modifier) {
  Column(modifier = modifier) { Text("Hello") }
}

@Composable
fun MultipleRoots() {
  Text("First")
  Text("Second")
}

@Composable
fun SingleRootElement(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("First")
    Text("Second")
  }
}

@Composable
fun SlotInvokedTwice(
  flag: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    if (flag) content() else content()
  }
}

@Composable
fun EffectWithUnitKey(id: String, modifier: Modifier = Modifier) {
  LaunchedEffect(Unit) { println("never restarts") }
  Box(modifier = modifier) { Text(id) }
}

@Composable
fun EffectWithProperKey(id: String, modifier: Modifier = Modifier) {
  LaunchedEffect(id) { println("restarts on change") }
  Box(modifier = modifier) { Text(id) }
}

@Composable
fun LazyListNoKey(users: List<UserData>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    items(users) { Text(it.name) }
  }
}

@Composable
fun LazyListWithKey(users: List<UserData>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    items(users, key = { it.id }) { Text(it.name) }
  }
}

@Composable
fun LambdaNotInEffectKey(onComplete: () -> Unit, modifier: Modifier = Modifier) {
  LaunchedEffect(Unit) {
    delay(1000)
    onComplete()
  }
  Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun LambdaInEffectKey(onComplete: () -> Unit, modifier: Modifier = Modifier) {
  LaunchedEffect(onComplete) {
    delay(1000)
    onComplete()
  }
  Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun MovableContentNotRemembered(modifier: Modifier = Modifier) {
  val content = remember { movableContentOf { Text("Movable") } }
  Box(modifier = modifier) { content() }
}

@Composable
fun MovableContentRemembered(modifier: Modifier = Modifier) {
  val content = remember { movableContentOf { Text("Movable") } }
  Box(modifier = modifier) { content() }
}

@Preview
@Composable
fun PublicPreviewFunction() { Text("Preview") }

@Preview
@Composable
private fun PrivatePreviewFunction() { Text("Preview") }

@Composable
fun LazyListNoContentType(users: List<UserData>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    item(contentType = "contentType1") { Text("Header") }
    items(items = users, key = { it.id }, contentType = { _ -> "contentType2" }) { Text(it.name) }
    item { Text("Footer") }
  }
}

@Composable
fun LazyListWithContentType(users: List<UserData>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    item(contentType = "header") { Text("Header") }
    items(
      items = users,
      key = { user -> user.id },
      contentType = { _ -> "contentType2" },
    ) { user -> Text(user.name) }
    item(contentType = "footer") { Text("Footer") }
  }
}

@Composable
fun UnstableListParam(items: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

class AllRulesViewModel : ViewModel()
data class UserData(val id: String, val name: String)
