/*
 * ComposeGuard Test Sample
 *
 * This file contains one example for each of the 31 ComposeGuard rules.
 * Open this file in Android Studio with ComposeGuard installed to verify all rules are working.
 *
 * Rules by Category:
 * - NAMING: 6 rules (1-6)
 * - MODIFIER: 7 rules (7-13)
 * - STATE: 5 rules (14-18)
 * - PARAMETER: 5 rules (19-23)
 * - COMPOSABLE: 6 rules (24-29)
 * - STRICTER: 2 rules (30-31)
 */

@file:Suppress("unused", "UNUSED_PARAMETER", "RedundantNullableReturnType")

package sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// =============================================================================
// NAMING RULES (6 Rules)
// =============================================================================

// Rule 1: ComposableNaming - Composable should start with uppercase
@Composable
fun myButton() { // WARNING: Should be "MyButton"
    Button(onClick = {}) { Text("Click") }
}

// Rule 2: CompositionLocalNaming - CompositionLocal should start with "Local"
val CurrentUser = compositionLocalOf { "Guest" } // WARNING: Should be "LocalCurrentUser"

// Rule 3: PreviewNaming - Preview should contain "Preview" in name
@Preview
@Composable
fun ButtonTest() { // WARNING: Should contain "Preview"
    Button(onClick = {}) { Text("Test") }
}

// Rule 4: MultipreviewNaming - Multipreview annotation should start with "Previews"
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemePreview // WARNING: Should be "PreviewsTheme"

// Rule 5: ComposableAnnotationNaming - Composable annotation should end with "Composable"
@Composable
annotation class MyScreen // WARNING: Should be "MyScreenComposable"

// Rule 6: EventParameterNaming - Event parameters should NOT use past tense
@Composable
fun ButtonWithPastTense(
    onClicked: () -> Unit, // WARNING: Should be "onClick"
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClicked, modifier = modifier) { Text("Click") }
}

// =============================================================================
// MODIFIER RULES (7 Rules)
// =============================================================================

// Rule 7: ModifierRequired - Top-level composable should have modifier parameter
@Composable
fun CardContent() { // WARNING: Should have modifier parameter
    Column { Text("Content") }
}

// Rule 8: ModifierDefaultValue - Modifier should have default value
@Composable
fun CardWithModifier(
    modifier: Modifier, // WARNING: Should be modifier: Modifier = Modifier
) {
    Column(modifier = modifier) { Text("Card") }
}

// Rule 9: ModifierNaming - Modifier parameter should be named "modifier"
@Composable
fun CardWithWrongName(
    cardModifier: Modifier = Modifier, // WARNING: Should be named "modifier"
) {
    Column(modifier = cardModifier) { Text("Card") }
}

// Rule 10: ModifierTopMost - Modifier should be applied to root layout
@Composable
fun ModifierNotOnRoot(modifier: Modifier = Modifier) {
    Column { // WARNING: modifier should be here
        Box(modifier = modifier) { Text("Content") }
    }
}

// Rule 11: ModifierReuse - Don't reuse modifier on multiple children
@Composable
fun CardWithModifierReuse(modifier: Modifier = Modifier) {
    Column {
        Text("First", modifier = modifier)  // WARNING: Modifier reused
        Text("Second", modifier = modifier)
    }
}

// Rule 11b: ModifierReuse with multiline chained modifiers
@Composable
fun NoItemsPlaceHolder(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, // WARNING: Modifier reused (used again in Text below)
    ) {
        Text(
            modifier = modifier  // Reused here with multiline chain
                .fillMaxWidth()
                .height(162.dp),
            text = ""
        )
        Text(text = text)
    }
}

// Rule 12: ModifierOrder - Modifier chain order matters
@Composable
fun CardWithWrongModifierOrder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(16.dp) // WARNING: padding before clickable reduces touch target
            .clickable { }
    ) {
        Text("Card")
    }
}

// Rule 13: AvoidComposed - Prefer Modifier.Node over composed
fun Modifier.customBorder() = composed { // WARNING: Use Modifier.Node instead
    this.border(1.dp, Color.Black)
}

// =============================================================================
// STATE RULES (5 Rules)
// =============================================================================

// Rule 14: RememberState - State should be remembered
@Composable
fun CounterWithoutRemember() {
    var count by mutableStateOf(0) // ERROR: Should be remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("Count: $count") }
}

// Rule 15: TypeSpecificState - Use type-specific state functions for primitives
@Composable
fun CounterWithGenericState() {
    var count by remember { mutableStateOf(0) } // WARNING: Should use mutableIntStateOf(0)
    Text("Count: $count")
}

// Rule 16: MutableStateParameter - Don't use MutableState as parameter
@Composable
fun MutableStateAsParam(
    count: MutableState<Int>, // WARNING: Should use value + onValueChange
    modifier: Modifier = Modifier,
) {
    Text("Count: ${count.value}", modifier = modifier)
}

// Rule 17: HoistState - Smart state hoisting detection
// Case A: State shared between children - SHOULD FLAG
@Composable
fun ExpandableCard() {
    var isExpanded by remember { mutableStateOf(false) } // WARNING: Shared between children
    CardHeader(isExpanded = isExpanded, onToggle = { isExpanded = !isExpanded })
    if (isExpanded) CardContent()
}

// Case B: State passed to child - SHOULD FLAG
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") } // WARNING: Passed to child
    SearchInput(query = query, onQueryChange = { query = it })
}

// Case C: Screen composable - SHOULD NOT FLAG (state is appropriate here)
@Composable
fun HomeScreen(viewModel: SampleViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // NO WARNING: Screen-level state OK
    Column { Text("Tab: $selectedTab") }
}

// Case D: Simple single-element wrapper - SHOULD NOT FLAG
@Composable
fun CounterButton(modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(0) } // NO WARNING: Simple element wrapper
    Button(onClick = { count++ }, modifier = modifier) { Text("Count: $count") }
}

// Case E: Internal UI state only - SHOULD NOT FLAG
@Composable
fun ToggleSection(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(true) } // NO WARNING: Internal UI state
    Column(modifier = modifier) {
        if (isVisible) Text("Content visible")
    }
}

// Case F: UI element state holder - SHOULD NOT FLAG (per official guidance)
@Composable
fun ScrollableList(items: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState() // NO WARNING: UI element state holder is fine
    LazyColumn(state = listState, modifier = modifier) {
        items.forEach { item -> Text(item) }
    }
}

// Helper for Case F
@Composable
fun rememberLazyListState() = remember { LazyListState() }
class LazyListState
@Composable
fun LazyColumn(state: LazyListState, modifier: Modifier = Modifier, content: () -> Unit) {
    Column(modifier = modifier) { content() }
}

// Helper composables for HoistState tests
@Composable
fun CardHeader(isExpanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) { Text("Expanded: $isExpanded") }
}

@Composable
fun CardContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier) { Text("Content") }
}

@Composable
fun SearchInput(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(value = query, onValueChange = onQueryChange, modifier = modifier)
}

// Rule 18: LambdaParameterInEffect - Lambda parameters in effects should be keyed
@Composable
fun EffectWithUnkeyedLambda(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        onComplete() // WARNING: Lambda used without being keyed
    }
    Box(modifier = modifier) { Text("Effect") }
}

// =============================================================================
// PARAMETER RULES (5 Rules)
// =============================================================================

// Rule 19: ParameterOrdering - Parameters should be ordered correctly
@Composable
fun WrongParameterOrder(
    enabled: Boolean = true, // WARNING: Optional before required
    modifier: Modifier = Modifier,
    title: String,
) {
    Column(modifier = modifier) { Text(title) }
}

// Rule 20: TrailingLambda - Content slots should be trailing
@Composable
fun ContentNotTrailing(
    content: @Composable () -> Unit, // WARNING: Content should be last
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) { Text(title); content() }
}

// Rule 21: MutableParameter - Don't use mutable types as parameters
@Composable
fun ListDisplay(
    items: MutableList<String>, // WARNING: Should be List or ImmutableList
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) { items.forEach { Text(it) } }
}

// Rule 22: ExplicitDependencies - Make dependencies explicit
@Composable
fun ImplicitDependencies() {
    val vm = viewModel<SampleViewModel>() // INFO: Should be explicit parameter
    Text("ViewModel acquired implicitly")
}

// Rule 23: ViewModelForwarding - Don't pass ViewModels through layers
@Composable
fun ScreenWithForwardedViewModel(
    viewModel: SampleViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ChildWithViewModel(viewModel = viewModel) // WARNING: Forwarding ViewModel
    }
}

@Composable
fun ChildWithViewModel(
    viewModel: SampleViewModel,
    modifier: Modifier = Modifier,
) {
    Text("Using forwarded ViewModel", modifier = modifier)
}

// =============================================================================
// COMPOSABLE RULES (6 Rules)
// =============================================================================

// Rule 24: ContentEmission - Composable should either emit or return, not both
@Composable
fun EmitsAndReturns(): String { // WARNING: Returns value AND emits content
    Column { Text("Hello") }
    return "World"
}

// Rule 25: MultipleContentEmitters - Do not emit multiple pieces of content
@Composable
fun MultipleEmitters() { // WARNING: Emits multiple content nodes
    Text("First")
    Text("Second")
}

// Rule 26: ContentSlotReused - Content slots should not be invoked multiple times
@Composable
fun ConditionalContent(
    isExpanded: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isExpanded) {
            content() // WARNING: Content invoked multiple times
        } else {
            content()
        }
    }
}

// Rule 27: EffectKeys - Effects should have proper keys
@Composable
fun EffectsWithConstantKeys(id: String, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { // WARNING: Using constant key
        println("This never restarts")
    }
    Box(modifier = modifier) { Text("id: $id") }
}

// Rule 28: MovableContent - movableContentOf should be remembered
@Composable
fun UnrememberedMovableContent(modifier: Modifier = Modifier) {
    val content = movableContentOf { // ERROR: Should be remember { movableContentOf }
        Text("Movable")
    }
    Box(modifier = modifier) { content() }
}

// Rule 29: PreviewVisibility - Preview functions should be private
@Preview
@Composable
fun PublicPreview() { // WARNING: Should be private
    Text("Preview")
}

// =============================================================================
// STRICTER RULES (2 Rules)
// =============================================================================

// Rule 30: Material2Usage - Prefer Material3 over Material2
@Composable
fun Material2Usage(modifier: Modifier = Modifier) {
    Button(onClick = {}, modifier = modifier) { // INFO: Using Material 2
        Text("Material 2")
    }
}

// Rule 31: UnstableCollections - Use immutable collections for stability
@Composable
fun UnstableCollectionParams(
    items: List<String>, // WARNING: Should use ImmutableList
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) { items.forEach { Text(it) } }
}

// =============================================================================
// HELPER CLASSES AND FUNCTIONS
// =============================================================================

class SampleViewModel : ViewModel()

interface ColumnScope

fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color): Modifier = this

// =============================================================================
// CORRECT EXAMPLES (No warnings expected)
// =============================================================================

// Correct naming
@Composable
fun MyButton(modifier: Modifier = Modifier) {
    Button(onClick = {}, modifier = modifier) { Text("Click") }
}

// Correct CompositionLocal naming
val LocalTheme = compositionLocalOf { "Light" }

// Correct preview naming and visibility
@Preview
@Composable
private fun MyButtonPreview() {
    MyButton()
}

// Correct multipreview naming
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewsTheme

// Correct parameter order
@Composable
fun ProperCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    content: @Composable () -> Unit = {},
) {
    Column(modifier = modifier) { Text(title); Text(subtitle); content() }
}

// Correct state handling
@Composable
fun ProperCounter(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(0) }
    Button(onClick = { count++ }, modifier = modifier) { Text("Count: $count") }
}

// Correct modifier order
@Composable
fun ProperModifierOrder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("Correct") }
}

// Correct effect key
@Composable
fun ProperEffectKeys(userId: String, modifier: Modifier = Modifier) {
    LaunchedEffect(userId) { println("Restarts when userId changes") }
    Box(modifier = modifier) { Text("User: $userId") }
}

// Correct movable content
@Composable
fun ProperMovableContent(modifier: Modifier = Modifier) {
    val content = remember { movableContentOf { Text("Remembered") } }
    Box(modifier = modifier) { content() }
}

// Correct event naming
@Composable
fun ProperEventNaming(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClick, modifier = modifier) { Text("Click") }
}

// Placeholder
fun mutableIntStateOf(value: Int): Any = mutableStateOf(value)
