/*
 * ComposeGuard Test Sample - All 36 Rules
 *
 * NAMING (6)       | MODIFIER (7)     | STATE (7)        | PARAMETER (5)    | COMPOSABLE (9)    | STRICTER (2)
 * -----------------|------------------|------------------|------------------|-------------------|-------------
 * ComposableNaming | ModifierRequired | RememberState    | ParameterOrder   | ContentEmission   | Material2
 * LocalNaming      | ModifierDefault  | TypeSpecific     | TrailingLambda   | MultipleContent   | Unstable
 * PreviewNaming    | ModifierNaming   | DerivedState     | MutableParam     | SlotReused        |
 * MultipreviewName | ModifierTopMost  | FrequentRecomp   | ExplicitDeps     | EffectKeys        |
 * AnnotationNaming | ModifierReuse    | MutableStateParam| ViewModelFwd     | LambdaInEffect    |
 * EventNaming      | ModifierOrder    | HoistState       |                  | MovableContent    |
 *                  | AvoidComposed    | DeferStateReads  |                  | PreviewVisibility |
 *                  |                  |                  |                  | LazyContentType   |
 *                  |                  |                  |                  | LazyMissingKey    |
 */

@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE")

package sample

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
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
import kotlinx.coroutines.flow.Flow

// ═══════════════════════════════════════════════════════════════════════════════
// NAMING RULES (6)
// ═══════════════════════════════════════════════════════════════════════════════

/** 1. ComposableNaming - Should start with uppercase */
@Composable
fun badButton() { Button(onClick = {}) { Text("Click") } } // BAD

@Composable
fun GoodButton(modifier: Modifier = Modifier) { // GOOD
    Button(onClick = {}, modifier = modifier) { Text("Click") }
}

/** 2. CompositionLocalNaming - Should start with "Local" */
val CurrentUser = compositionLocalOf { "Guest" } // BAD
val LocalUser = compositionLocalOf { "Guest" }   // GOOD

/** 3. PreviewNaming - Should contain "Preview" */
@Preview @Composable
fun ButtonTest() { Text("Test") } // BAD

@Preview @Composable
private fun ButtonPreview() { Text("Test") } // GOOD

/** 4. MultipreviewNaming - Should start with "Previews" */
@Preview(name = "Light") @Preview(name = "Dark")
annotation class ThemePreview // BAD

@Preview(name = "Light") @Preview(name = "Dark")
annotation class PreviewsTheme // GOOD

/** 5. ComposableAnnotationNaming - Should end with "Composable" */
@Composable
annotation class MyScreen // BAD: should be MyScreenComposable

/** 6. EventParameterNaming - No past tense */
@Composable
fun BadEvent(onClicked: () -> Unit, modifier: Modifier = Modifier) { // BAD
    Button(onClick = onClicked, modifier = modifier) { Text("Click") }
}

@Composable
fun GoodEvent(onClick: () -> Unit, modifier: Modifier = Modifier) { // GOOD
    Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODIFIER RULES (7)
// ═══════════════════════════════════════════════════════════════════════════════

/** 7. ModifierRequired - Should have modifier parameter */
@Composable
fun NoModifier() { Column { Text("Content") } } // BAD

@Composable
fun WithModifier(modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { Text("Content") }
}

/** 8. ModifierDefaultValue - Modifier should have default */
@Composable
fun NoDefault(modifier: Modifier) { // BAD
    Column(modifier = modifier) { Text("Card") }
}

/** 9. ModifierNaming - Should be named "modifier" */
@Composable
fun WrongName(cardModifier: Modifier = Modifier) { // BAD
    Column(modifier = cardModifier) { Text("Card") }
}

/** 10. ModifierTopMost - Modifier on root element */
@Composable
fun NotOnRoot(modifier: Modifier = Modifier) { // BAD
    Column { Box(modifier = modifier) { Text("Content") } }
}

@Composable
fun OnRoot(modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { Box { Text("Content") } }
}

/** 11. ModifierReuse - Don't reuse on multiple elements */
@Composable
fun Reused(modifier: Modifier = Modifier) { // BAD
    Column {
        Text("A", modifier = modifier)
        Text("B", modifier = modifier)
    }
}

/** 12. ModifierOrder - Chain order matters */
@Composable
fun BadOrder(modifier: Modifier = Modifier) { // BAD
    Box(modifier = modifier.padding(16.dp).clickable { }) { Text("Card") }
}

@Composable
fun GoodOrder(modifier: Modifier = Modifier) { // GOOD
    Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Card") }
}

/** 13. AvoidComposed - Use Modifier.Node */
fun Modifier.badBorder() = composed { this.border(1.dp, Color.Black) } // BAD

// ═══════════════════════════════════════════════════════════════════════════════
// STATE RULES (6)
// ═══════════════════════════════════════════════════════════════════════════════

/** 14. RememberState - State should be remembered */
@Composable
fun NotRemembered() { // BAD
    var count by mutableStateOf(0)
    Button(onClick = { count++ }) { Text("$count") }
}

@Composable
fun Remembered(modifier: Modifier = Modifier) { // GOOD
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }, modifier = modifier) { Text("$count") }
}

/** 15. TypeSpecificState - Use mutableIntStateOf for primitives */
@Composable
fun GenericState(modifier: Modifier = Modifier) { // BAD
    var count by remember { mutableStateOf(0) }
    Text("$count", modifier = modifier)
}

@Composable
fun SpecificState(modifier: Modifier = Modifier) { // GOOD
    var count by remember { mutableIntStateOf(0) }
    Text("$count", modifier = modifier)
}

/** 16. DerivedStateOfCandidate - Use remember for computed values */
@Composable
fun NoRemember(items: List<String>, query: String, modifier: Modifier = Modifier) {
    // WARNING: Consider using remember with keys for 'filtered'
    val filtered = items.filter { it.contains(query) } // BAD
    Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

@Composable
fun NoRememberSorted(items: List<String>, modifier: Modifier = Modifier) {
    // WARNING: Consider using remember with keys for 'sorted'
    val sorted = items.sortedBy { it.length } // BAD
    Column(modifier = modifier) { sorted.forEach { Text(it) } }
}

@Composable
fun NoRememberMapped(items: List<String>, modifier: Modifier = Modifier) {
    // WARNING: Consider using remember with keys for 'mapped'
    val mapped = items.map { it.uppercase() } // BAD
    Column(modifier = modifier) { mapped.forEach { Text(it) } }
}

@Composable
fun WithRemember(items: List<String>, query: String, modifier: Modifier = Modifier) { // GOOD
    val filtered = remember(items, query) { items.filter { it.contains(query) } }
    Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

@Composable
fun WithDerived(items: List<String>, query: String, modifier: Modifier = Modifier) { // GOOD

    val filtered by remember(items, query) {
        derivedStateOf { items.filter { it.contains(query) } }
    }
    Column(modifier = modifier) { filtered.forEach { Text(it) } }
}

// derivedStateOf - KEY INSIGHT: Use when input changes MORE frequently than output
@Composable
fun ScrollThresholdBad(lazyListState: LazyListState, modifier: Modifier = Modifier) { // BAD
    // Recomposes on EVERY scroll frame!
    val showButton = lazyListState.firstVisibleItemIndex > 0
    Box(modifier = modifier) { if (showButton) Text("Scroll to top") }
}

@Composable
fun ScrollThresholdGood(lazyListState: LazyListState, modifier: Modifier = Modifier) { // GOOD
    // Only recomposes when showButton changes (true ↔ false)
    val showButton by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
    Box(modifier = modifier) { if (showButton) Text("Scroll to top") }
}

/** 17. FrequentRecomposition - Remember expensive objects */
@Composable
fun ObjectEveryTime(modifier: Modifier = Modifier) { // BAD
    val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text("Hello", style = style, modifier = modifier)
}

@Composable
fun ObjectRemembered(modifier: Modifier = Modifier) { // GOOD
    val style = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    Text("Hello", style = style, modifier = modifier)
}

@Composable
fun FlowNoLifecycle(flow: Flow<String>, modifier: Modifier = Modifier) { // BAD
    val state by flow.collectAsState(initial = "")
    Text(state, modifier = modifier)
}

/** 18. MutableStateParameter - Use value + callback pattern */
@Composable
fun MutableParam(count: MutableState<Int>, modifier: Modifier = Modifier) { // BAD
    Text("${count.value}", modifier = modifier)
}

@Composable
fun ValueCallback(count: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) { // GOOD
    Button(onClick = { onChange(count + 1) }, modifier = modifier) { Text("$count") }
}

/** 19. HoistState - Hoist shared state */
@Composable
fun SharedState() { // BAD
    var expanded by remember { mutableStateOf(false) }
    Header(expanded = expanded, onToggle = { expanded = !expanded })
    if (expanded) Content()
}

@Composable
fun PassedToChild() { // BAD
    var query by remember { mutableStateOf("") }
    SearchField(query = query, onQueryChange = { query = it })
}

@Composable
fun ScreenLevel(modifier: Modifier = Modifier) { // GOOD: screen-level OK
    var tab by remember { mutableStateOf(0) }
    Column(modifier = modifier) { Text("Tab: $tab") }
}

/** 20. DeferStateReads - Use lambda modifiers for animated/frequently changing state */
@Composable
fun DirectStateRead(scrollOffset: Float, modifier: Modifier = Modifier) { // BAD
    // State read during composition causes recomposition on every scroll
    Box(modifier = modifier.offset(y = scrollOffset.dp))
}

@Composable
fun DeferredStateRead(scrollOffset: () -> Float, modifier: Modifier = Modifier) { // GOOD
    // State read deferred to layout phase
    Box(modifier = modifier.offset { IntOffset(0, scrollOffset().toInt()) })
}

@Composable
fun DirectAlpha(alpha: Float, modifier: Modifier = Modifier) { // BAD
    // Alpha read during composition
    Box(modifier = modifier.alpha(alpha))
}

@Composable
fun DeferredAlpha(alpha: () -> Float, modifier: Modifier = Modifier) { // GOOD
    // Alpha read deferred to draw phase
    Box(modifier = modifier.graphicsLayer { this.alpha = alpha() })
}

@Composable
fun AnimatedOffsetBad(modifier: Modifier = Modifier) { // BAD
    val offset by animateFloatAsState(targetValue = 100f, label = "offset")
    // Causes recomposition on every animation frame
    Box(modifier = modifier.offset(x = offset.dp))
}

@Composable
fun AnimatedOffsetGood(modifier: Modifier = Modifier) { // GOOD
    val offset by animateFloatAsState(targetValue = 100f, label = "offset")
    // Defers read to layout phase, skips recomposition
    Box(modifier = modifier.offset { IntOffset(offset.toInt(), 0) })
}

// ═══════════════════════════════════════════════════════════════════════════════
// PARAMETER RULES (5)
// ═══════════════════════════════════════════════════════════════════════════════

/** 21. ParameterOrdering - Follow Compose API guidelines */
// Correct order: required → modifier (FIRST optional) → optional → content

// BAD: Multiple ordering issues - click "Reorder parameters" to fix all at once
@Composable
fun BadMultipleIssues(
    title: String,                              // required ✓
    items: List<String>,                        // required ✓
    content: @Composable () -> Unit,            // content - should be LAST!
    isEnabled: Boolean = true,                  // optional - should be AFTER modifier
    style: TextStyle = LocalTextStyle.current,  // optional - should be AFTER modifier
    modifier: Modifier = Modifier               // modifier - should be FIRST optional!
) {
    Column(modifier = modifier) { Text(title); content() }
}

// GOOD: After applying "Reorder parameters" quick fix
@Composable
fun GoodReorderedParams(
    title: String,                              // 1. Required params
    items: List<String>,                        // 1. Required params
    modifier: Modifier = Modifier,              // 2. Modifier (FIRST optional)
    isEnabled: Boolean = true,                  // 3. Other optional params
    style: TextStyle = LocalTextStyle.current,  // 3. Other optional params
    content: @Composable () -> Unit             // 4. Content lambda (trailing)
) {
    Column(modifier = modifier) { Text(title); content() }
}

// BAD: State and callback separated
@Composable
fun BadStateCallbackSeparated(
    value: String,
    label: String,  // Separates state from callback
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) { Text(value) }
}

// GOOD: State and callback paired together
@Composable
fun GoodStateCallbackPaired(
    value: String,
    onValueChange: (String) -> Unit,  // Immediately after state
    modifier: Modifier = Modifier,    // FIRST optional
    label: String = ""
) {
    Column(modifier = modifier) { Text(value) }
}

/** 22. TrailingLambda - Content should be last */
@Composable
fun NotTrailing(content: @Composable () -> Unit, title: String, modifier: Modifier = Modifier) { // BAD
    Column(modifier = modifier) { Text(title); content() }
}

@Composable
fun Trailing(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) { // GOOD
    Column(modifier = modifier) { Text(title); content() }
}

/** 23. MutableParameter - Use immutable collections */
@Composable
fun MutableList(items: MutableList<String>, modifier: Modifier = Modifier) { // BAD
    Column(modifier = modifier) { items.forEach { Text(it) } }
}

@Composable
fun ImmutableList(items: List<String>, modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { items.forEach { Text(it) } }
}

/** 24. ExplicitDependencies - Make dependencies explicit */
@Composable
fun ImplicitDep() { // BAD
    val vm = viewModel<SampleViewModel>()
    Text("Data")
}

@Composable
fun ExplicitDep(viewModel: SampleViewModel, modifier: Modifier = Modifier) { // GOOD
    Text("Data", modifier = modifier)
}

/** 25. ViewModelForwarding - Don't forward ViewModel */
@Composable
fun ParentForwards(viewModel: SampleViewModel, modifier: Modifier = Modifier) { // BAD
    Column(modifier = modifier) { ChildVM(viewModel = viewModel) }
}

@Composable
fun ChildVM(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
    Text("Forwarded", modifier = modifier)
}

@Composable
fun ParentData(data: String, modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { ChildData(data = data) }
}

@Composable
fun ChildData(data: String, modifier: Modifier = Modifier) {
    Text(data, modifier = modifier)
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSABLE RULES (9)
// ═══════════════════════════════════════════════════════════════════════════════

/** 26. ContentEmission - Emit OR return, not both */
@Composable
fun EmitAndReturn(): String { // BAD
    Column { Text("Hello") }
    return "World"
}

@Composable
fun EmitOnly(modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { Text("Hello") }
}

/** 27. MultipleContent - Single root */
@Composable
fun MultiRoot() { // BAD
    Text("First")
    Text("Second")
}

@Composable
fun SingleRoot(modifier: Modifier = Modifier) { // GOOD
    Column(modifier = modifier) { Text("First"); Text("Second") }
}

/** 28. ContentSlotReused - Don't invoke slot twice */
@Composable
fun SlotTwice(flag: Boolean, content: @Composable () -> Unit, modifier: Modifier = Modifier) { // BAD
    Column(modifier = modifier) { if (flag) content() else content() }
}

/** 29. EffectKeys - Use proper keys */
@Composable
fun ConstantKey(id: String, modifier: Modifier = Modifier) { // BAD
    LaunchedEffect(Unit) { println("never restarts") }
    Box(modifier = modifier) { Text(id) }
}

@Composable
fun ProperKey(id: String, modifier: Modifier = Modifier) { // GOOD
    LaunchedEffect(id) { println("restarts on change") }
    Box(modifier = modifier) { Text(id) }
}

/** 30. LambdaParameterInEffect - Key lambda params */
@Composable
fun LambdaNotKeyed(onDone: () -> Unit, modifier: Modifier = Modifier) { // BAD
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1000); onDone() }
    Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun LambdaKeyed(onDone: () -> Unit, modifier: Modifier = Modifier) { // GOOD
    LaunchedEffect(onDone) { kotlinx.coroutines.delay(1000); onDone() }
    Box(modifier = modifier) { Text("Effect") }
}

/** 31. MovableContent - Should be remembered */
@Composable
fun MovableNotRemembered(modifier: Modifier = Modifier) { // BAD
    val content = movableContentOf { Text("Movable") }
    Box(modifier = modifier) { content() }
}

@Composable
fun MovableRemembered(modifier: Modifier = Modifier) { // GOOD
    val content = remember { movableContentOf { Text("Movable") } }
    Box(modifier = modifier) { content() }
}

/** 32. PreviewVisibility - Preview should be private */
@Preview @Composable
fun PublicPreview() { Text("Preview") } // BAD

@Preview @Composable
private fun PrivatePreview() { Text("Preview") } // GOOD

/** 33. LazyListContentType - Use contentType for heterogeneous lists */
@Composable
fun NoContentType(users: List<User>, modifier: Modifier = Modifier) { // BAD
    LazyColumn(modifier = modifier) {
        item { Text("Header") }
        items(users, key = { it.id }) { Text(it.name) }
        item { Text("Footer") }
    }
}

@Composable
fun WithContentType(users: List<User>, modifier: Modifier = Modifier) { // GOOD
    LazyColumn(modifier = modifier) {
        item(contentType = "contentType1") { Text("Header") }
        items(users, key = { it.id }, contentType = { "contentType2" }) { Text(it.name) }
        item(contentType = "contentType3") { Text("Footer") }
    }
}

/** 34. LazyListMissingKey - items() should have key */
@Composable
fun NoKey(users: List<User>, modifier: Modifier = Modifier) { // BAD
    LazyColumn(modifier = modifier) { items(users) { Text(it.name) } }
}

@Composable
fun WithKey(users: List<User>, modifier: Modifier = Modifier) { // GOOD
    LazyColumn(modifier = modifier) { items(users, key = { it.id }) { Text(it.name) } }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STRICTER RULES (2)
// ═══════════════════════════════════════════════════════════════════════════════

/** 35. Material2Usage - Prefer Material3 */
@Composable
fun Material2(modifier: Modifier = Modifier) { // INFO
    Button(onClick = {}, modifier = modifier) { Text("M2 Button") }
}

/** 36. UnstableCollections - Use ImmutableList */
@Composable
fun Unstable(items: List<String>, modifier: Modifier = Modifier) { // BAD
    Column(modifier = modifier) { items.forEach { Text(it) } }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════════════════════

class SampleViewModel : ViewModel()
data class User(val id: String, val name: String)

@Composable fun Header(expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) { Text("$expanded") }
}
@Composable fun Content(modifier: Modifier = Modifier) { Box(modifier = modifier) { Text("Content") } }
@Composable fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(value = query, onValueChange = onQueryChange, modifier = modifier)
}

fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color): Modifier = this
fun mutableIntStateOf(value: Int) = mutableStateOf(value)

@Composable
fun LazyColumn(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    Column(modifier = modifier) { LazyListScopeImpl().content() }
}

interface LazyListScope {
    fun item(contentType: Any? = null, content: @Composable () -> Unit)
    fun items(items: List<Any>, key: ((Any) -> Any)? = null, contentType: ((Any) -> Any)? = null, itemContent: @Composable (Any) -> Unit)
}

private class LazyListScopeImpl : LazyListScope {
    override fun item(contentType: Any?, content: @Composable () -> Unit) {}
    override fun items(items: List<Any>, key: ((Any) -> Any)?, contentType: ((Any) -> Any)?, itemContent: @Composable (Any) -> Unit) {}
}
