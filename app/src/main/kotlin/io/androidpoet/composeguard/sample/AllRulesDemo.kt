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

// ═══════════════════════════════════════════════════════════════════════════════
// NAMING RULES (6)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 1: ComposableNaming - Should start with uppercase
@Composable
fun badNamingButton() { // BAD: lowercase
  Button(onClick = {}) { Text("Click") }
}

@Composable
fun GoodNamingButton(modifier: Modifier = Modifier) { // GOOD: uppercase
  Button(onClick = {}, modifier = modifier) { Text("Click") }
}

// Rule 2: CompositionLocalNaming - Should start with "Local"
val CurrentTheme = compositionLocalOf { "Light" } // BAD: doesn't start with Local
val LocalTheme = compositionLocalOf { "Light" } // GOOD: starts with Local

// Rule 3: PreviewNaming - Should contain "Preview"
@Preview
@Composable
fun NamingButtonTest() { Text("Test") } // BAD: no "Preview" in name

@Preview
@Composable
private fun NamingButtonPreview() { Text("Test") } // GOOD: contains "Preview"

// Rule 4: MultipreviewNaming - Should start with "Preview"
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemeVariants // BAD: doesn't start with Preview

@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewThemeVariants // GOOD: starts with Preview

// Rule 5: ComposableAnnotationNaming - (commented: @Composable can't apply to annotation)
// annotation class MyScreen // BAD: should be MyScreenComposable

// Rule 6: EventParameterNaming - No past tense
@Composable
fun BadEventNaming(onClicked: () -> Unit, modifier: Modifier = Modifier) { // BAD: past tense
  Button(onClick = onClicked, modifier = modifier) { Text("Click") }
}

@Composable
fun GoodEventNaming(onClick: () -> Unit, modifier: Modifier = Modifier) { // GOOD: present
  Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODIFIER RULES (7)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 7: ModifierRequired - Should have modifier parameter
@Composable
fun NoModifierParam() { // BAD: no modifier
  Column { Text("Content") }
}

@Composable
fun WithModifierParam(modifier: Modifier = Modifier) { // GOOD
  Column(modifier = modifier) { Text("Content") }
}

// Rule 8: ModifierDefaultValue - Modifier should have default
@Composable
fun NoModifierDefault(modifier: Modifier) { // BAD: no default
  Column(modifier = modifier) { Text("Content") }
}

// Rule 9: ModifierNaming - Should be named "modifier"
@Composable
fun WrongModifierName(buttonModifier: Modifier = Modifier) { // BAD: wrong name
  Column(modifier = buttonModifier) { Text("Content") }
}

// Rule 10: ModifierTopMost - Modifier on root element
@Composable
fun ModifierNotOnRoot(modifier: Modifier = Modifier) { // BAD: not on root
  Column {
    Box(modifier = modifier) { Text("Content") }
  }
}

@Composable
fun ModifierOnRoot(modifier: Modifier = Modifier) { // GOOD
  Column(modifier = modifier) {
    Box { Text("Content") }
  }
}

// Rule 11: ModifierReuse - Don't reuse on multiple elements
@Composable
fun ModifierReused(modifier: Modifier = Modifier) { // BAD: reused
  Column {
    Text("A", modifier = modifier)
    Text("B", modifier = modifier)
  }
}

// Rule 12: ModifierOrder - Chain order matters
@Composable
fun BadModifierOrder(modifier: Modifier = Modifier) { // BAD: padding before clickable
  Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Card") }
}

@Composable
fun GoodModifierOrder(modifier: Modifier = Modifier) { // GOOD: clickable before padding
  Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Card") }
}

// Rule: AvoidComposed - Use Modifier.Node
fun Modifier.avoidComposedBorder() = composed { // BAD: use Modifier.Node
  this.border(1.dp, Color.Black)
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATE RULES (6)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 13: RememberState - State should be remembered
@Composable
fun StateNotRemembered() { // BAD: not remembered
  var count by mutableStateOf(0)
  Button(onClick = { count++ }) { Text("$count") }
}

@Composable
fun StateRemembered(modifier: Modifier = Modifier) { // GOOD
  var count by remember { mutableStateOf(0) }
  Button(onClick = { count++ }, modifier = modifier) { Text("$count") }
}

// Rule 14: TypeSpecificState - Use mutableIntStateOf for primitives
@Composable
fun GenericIntState(modifier: Modifier = Modifier) { // BAD: generic for Int
  var count by remember { mutableStateOf(0) }
  Text("$count", modifier = modifier)
}

@Composable
fun SpecificIntState(modifier: Modifier = Modifier) { // GOOD: type-specific
  var count by remember { mutableIntStateOf(0) }
  Text("$count", modifier = modifier)
}

// Rule 15: DerivedStateOfCandidate - Use remember for computed values
@Composable
fun ComputedNotRemembered(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = items.filter { it.contains(query) } // BAD: recomputes every time
  Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

@Composable
fun ComputedRemembered(items: List<String>, query: String, modifier: Modifier = Modifier) {
  val filtered = remember(items, query) { items.filter { it.contains(query) } } // GOOD
  Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

// Rule 16: FrequentRecomposition - Remember expensive objects
@Composable
fun ObjectCreatedEveryTime(modifier: Modifier = Modifier) { // BAD
  val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun ObjectRememberedProperly(modifier: Modifier = Modifier) { // GOOD
  val style = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
  Text("Hello", style = style, modifier = modifier)
}

@Composable
fun FlowWithoutLifecycle(flow: Flow<String>, modifier: Modifier = Modifier) { // BAD
  val state by flow.collectAsState(initial = "")
  Text(state, modifier = modifier)
}

// Rule 17: MutableStateParameter - Use value + callback pattern
@Composable
fun MutableStateAsParam(count: MutableState<Int>, modifier: Modifier = Modifier) { // BAD
  Text("${count.value}", modifier = modifier)
}

@Composable
fun ValueCallbackPattern(
  count: Int,
  onChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) { // GOOD
  Button(onClick = { onChange(count + 1) }, modifier = modifier) { Text("$count") }
}

// Rule 18: HoistState - Hoist shared state
@Composable
fun StateSharedWithChild() { // BAD: should hoist
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

// ═══════════════════════════════════════════════════════════════════════════════
// PARAMETER RULES (5)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 19: ParameterOrdering - Required first, optional last
@Composable
fun BadParamOrdering(enabled: Boolean = true, modifier: Modifier = Modifier, title: String) { // BAD
  Column(modifier = modifier) { Text(title) }
}

@Composable
fun GoodParamOrdering(
  title: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) { // GOOD
  Column(modifier = modifier) { Text(title) }
}

// Rule 20: TrailingLambda - Content should be last
@Composable
fun ContentNotTrailing(
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
fun ContentTrailing(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit, // GOOD: last
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

// Rule 21: MutableParameter - Use immutable collections
@Composable
fun MutableListParameter(items: MutableList<String>, modifier: Modifier = Modifier) { // BAD
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

@Composable
fun ImmutableListParameter(items: List<String>, modifier: Modifier = Modifier) { // GOOD
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

// Rule 22: ExplicitDependencies - Make dependencies explicit
@Composable
fun ImplicitDependency() { // BAD: implicit ViewModel
  val vm = viewModel<AllRulesViewModel>()
  Text("Data")
}

@Composable
fun ExplicitDependency(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) { // GOOD
  Text("Data", modifier = modifier)
}

// Rule 23: ViewModelForwarding - Don't forward ViewModel
@Composable
fun ParentForwardsViewModel(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) { // BAD
  Column(modifier = modifier) {
    ChildWithViewModel(viewModel = viewModel)
  }
}

@Composable
fun ChildWithViewModel(viewModel: AllRulesViewModel, modifier: Modifier = Modifier) {
  Text("Forwarded VM", modifier = modifier)
}

@Composable
fun ParentPassesData(data: String, modifier: Modifier = Modifier) { // GOOD: pass data
  Column(modifier = modifier) {
    ChildWithData(data = data)
  }
}

@Composable
fun ChildWithData(data: String, modifier: Modifier = Modifier) {
  Text(data, modifier = modifier)
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSABLE RULES (9)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 24: ContentEmission - Emit OR return, not both
@Composable
fun EmitsAndReturns(): String { // BAD: both emit and return
  Column { Text("Hello") }
  return "World"
}

@Composable
fun OnlyEmits(modifier: Modifier = Modifier) { // GOOD: only emit
  Column(modifier = modifier) { Text("Hello") }
}

// Rule 25: MultipleContent - Single root
@Composable
fun MultipleRoots() { // BAD: multiple roots
  Text("First")
  Text("Second")
}

@Composable
fun SingleRootElement(modifier: Modifier = Modifier) { // GOOD
  Column(modifier = modifier) {
    Text("First")
    Text("Second")
  }
}

// Rule 26: ContentSlotReused - Don't invoke slot twice
@Composable
fun SlotInvokedTwice(
  flag: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) { // BAD
  Column(modifier = modifier) {
    if (flag) content() else content()
  }
}

// Rule 27: EffectKeys - Use proper keys
@Composable
fun EffectWithUnitKey(id: String, modifier: Modifier = Modifier) { // BAD: Unit key
  LaunchedEffect(Unit) { println("never restarts") }
  Box(modifier = modifier) { Text(id) }
}

@Composable
fun EffectWithProperKey(id: String, modifier: Modifier = Modifier) { // GOOD
  LaunchedEffect(id) { println("restarts on change") }
  Box(modifier = modifier) { Text(id) }
}

// Rule 28: LazyListMissingKey - items() should have key
@Composable
fun LazyListNoKey(users: List<UserData>, modifier: Modifier = Modifier) { // BAD
  LazyColumn(modifier = modifier) {
    items(users) { Text(it.name) }
  }
}

@Composable
fun LazyListWithKey(users: List<UserData>, modifier: Modifier = Modifier) { // GOOD
  LazyColumn(modifier = modifier) {
    items(users, key = { it.id }) { Text(it.name) }
  }
}

// Rule 29: LambdaParameterInEffect - Key lambda params
@Composable
fun LambdaNotInEffectKey(onComplete: () -> Unit, modifier: Modifier = Modifier) { // BAD
  LaunchedEffect(Unit) {
    delay(1000)
    onComplete()
  }
  Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun LambdaInEffectKey(onComplete: () -> Unit, modifier: Modifier = Modifier) { // GOOD
  LaunchedEffect(onComplete) {
    delay(1000)
    onComplete()
  }
  Box(modifier = modifier) { Text("Effect") }
}

// Rule 30: MovableContent - Should be remembered
@Composable
fun MovableContentNotRemembered(modifier: Modifier = Modifier) { // BAD
  val content = movableContentOf { Text("Movable") }
  Box(modifier = modifier) { content() }
}

@Composable
fun MovableContentRemembered(modifier: Modifier = Modifier) { // GOOD
  val content = remember { movableContentOf { Text("Movable") } }
  Box(modifier = modifier) { content() }
}

// Rule 31: PreviewVisibility - Preview should be private
@Preview
@Composable
fun PublicPreviewFunction() { Text("Preview") } // BAD: public

@Preview
@Composable
private fun PrivatePreviewFunction() { Text("Preview") } // GOOD: private

// Rule 32: LazyListContentType - Use contentType for heterogeneous lists
@Composable
fun LazyListNoContentType(users: List<UserData>, modifier: Modifier = Modifier) { // BAD
  LazyColumn(modifier = modifier) {
    item(contentType = "contentType1") { Text("Header") }
    items(items = users, key = { it.id }, contentType = { _ -> "contentType2" }) { Text(it.name) }
    item { Text("Footer") }
  }
}

@Composable
fun LazyListWithContentType(users: List<UserData>, modifier: Modifier = Modifier) { // GOOD
  LazyColumn(modifier = modifier) {
    item(contentType = "header") { Text("Header") }
    items(
      items = users,
      key = { user -> user.id },
      contentType = { _ -> "contentType2" }) { user -> Text(user.name) }
    item(contentType = "footer") { Text("Footer") }
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STRICTER RULES (2)
// ═══════════════════════════════════════════════════════════════════════════════

// Rule 33: Material2Usage - Prefer Material3
// (This file uses Material3, so no violation here)

// Rule 34: UnstableCollections - Use ImmutableList
@Composable
fun UnstableListParam(items: List<String>, modifier: Modifier = Modifier) { // INFO
  Column(modifier = modifier) { items.forEach { Text(it) } }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HELPER CLASSES
// ═══════════════════════════════════════════════════════════════════════════════

class AllRulesViewModel : ViewModel()
data class UserData(val id: String, val name: String)
