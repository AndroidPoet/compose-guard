/*
 * ComposeGuard Test Sample
 *
 * This file contains examples for each of the 38 ComposeGuard rules.
 * Open this file in Android Studio with ComposeGuard installed to verify all rules are working.
 *
 * Rules by Category:
 * - NAMING: 6 rules (1-6)
 * - MODIFIER: 7 rules (7-13)
 * - STATE: 5 rules (14-18)
 * - PARAMETER: 5 rules (19-23)
 * - COMPOSABLE: 6 rules (24-29)
 * - STRICTER: 2 rules (30-31)
 * - EXPERIMENTAL: 7 rules (32-38) - Disabled by default
 *
 * Additional Experimental Tests:
 * - LazyRow/LazyVerticalGrid without key
 * - DerivedStateOf candidates (map, groupBy, joinToString, distinct)
 * - State in DisposableEffect
 * - Collection creation (mapOf, setOf, Pair, BorderStroke)
 * - Method reference candidates for code clarity
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
// EXPERIMENTAL RULES (7 Rules) - Disabled by default, enable in settings
// =============================================================================

// Rule 32: LazyListMissingKey - items() should have a key parameter
@Composable
fun LazyListWithoutKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(users) { user -> // WARNING: Missing key parameter
            Text(user.name)
        }
    }
}

// Rule 32b: LazyListMissingKey - itemsIndexed without key
@Composable
fun LazyListIndexedWithoutKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(users) { index, user -> // WARNING: Missing key parameter
            Text("$index: ${user.name}")
        }
    }
}

// Rule 33: UnstableLazyListItems - Mutable collection in LazyList
@Composable
fun LazyListWithMutableItems(
    users: MutableList<User>, // WARNING: Should use List or ImmutableList
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// Rule 34: LazyListContentType - Heterogeneous items without contentType
@Composable
fun HeterogeneousLazyList(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) { // WARNING: Missing contentType for heterogeneous items
        item { Text("Header") }
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
        item { Text("Footer") }
    }
}

// Rule 35: DerivedStateOfCandidate - Computed value should use derivedStateOf
@Composable
fun FilteredListWithoutDerivedState(
    items: List<String>,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    // WARNING: This filters on EVERY recomposition
    val filteredItems = items.filter { it.contains(searchQuery) }
    Column(modifier = modifier) {
        filteredItems.forEach { Text(it) }
    }
}

// Rule 36b: DerivedStateOfCandidate - Sorted list without derivedStateOf
@Composable
fun SortedListWithoutDerivedState(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    // WARNING: Consider using derivedStateOf for 'sortedItems'
    val sortedItems = items.sortedBy { it.length }
    Column(modifier = modifier) {
        sortedItems.forEach { Text(it) }
    }
}

// Rule 37: StateReadInComposition - State read inside remember without key
@Composable
fun StateReadInRemember(countState: State<Int>, modifier: Modifier = Modifier) {
    // WARNING: State 'countState' read inside remember without being a key
    val doubled = remember {
        countState.value * 2
    }
    Text("Doubled: $doubled", modifier = modifier)
}

// Rule 37b: StateReadInComposition - State read in SideEffect
@Composable
fun StateReadInSideEffect(countState: State<Int>, modifier: Modifier = Modifier) {
    SideEffect {
        // WARNING: State read in SideEffect - runs on every recomposition
        println("Count is: ${countState.value}")
    }
    Text("Count: ${countState.value}", modifier = modifier)
}

// Rule 38: FrequentRecomposition - Object created every recomposition
@Composable
fun ObjectCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: New 'TextStyle' instance created on every recomposition
    val style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text("Hello", style = style, modifier = modifier)
}

// Rule 38b: FrequentRecomposition - Collection created every recomposition
@Composable
fun CollectionCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: Collection created with 'listOf' on every recomposition
    val items = listOf("Apple", "Banana", "Cherry")
    Column(modifier = modifier) {
        items.forEach { Text(it) }
    }
}

// Rule 38c: FrequentRecomposition - collectAsState without lifecycle awareness
@Composable
fun FlowCollectionWithoutLifecycle(
    flow: kotlinx.coroutines.flow.Flow<String>,
    modifier: Modifier = Modifier,
) {
    // INFO: Consider using collectAsStateWithLifecycle for lifecycle awareness
    val state by flow.collectAsState(initial = "")
    Text(state, modifier = modifier)
}

// =============================================================================
// ADDITIONAL EXPERIMENTAL TESTS
// =============================================================================

// Rule 32c: LazyListMissingKey - LazyRow without key
@Composable
fun LazyRowWithoutKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier) {
        items(users) { user -> // WARNING: Missing key parameter
            Text(user.name)
        }
    }
}

// Rule 32d: LazyListMissingKey - LazyVerticalGrid without key
@Composable
fun LazyGridWithoutKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(modifier = modifier) {
        items(users) { user -> // WARNING: Missing key parameter
            Text(user.name)
        }
    }
}

// Rule 35c: DerivedStateOfCandidate - map operation without derivedStateOf
@Composable
fun MappedListWithoutDerivedState(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    // WARNING: Consider using derivedStateOf for 'mappedItems'
    val mappedItems = items.map { it.uppercase() }
    Column(modifier = modifier) {
        mappedItems.forEach { Text(it) }
    }
}

// Rule 36d: DerivedStateOfCandidate - groupBy operation without derivedStateOf
@Composable
fun GroupedListWithoutDerivedState(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    // WARNING: Consider using derivedStateOf for 'groupedItems'
    val groupedItems = items.groupBy { it.first() }
    Column(modifier = modifier) {
        groupedItems.forEach { (key, values) -> Text("$key: ${values.size}") }
    }
}

// Rule 36e: DerivedStateOfCandidate - joinToString without derivedStateOf
@Composable
fun JoinedStringWithoutDerivedState(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    // WARNING: Consider using derivedStateOf for 'joinedString'
    val joinedString = items.joinToString(", ")
    Text(joinedString, modifier = modifier)
}

// Rule 36f: DerivedStateOfCandidate - distinct without derivedStateOf
@Composable
fun DistinctListWithoutDerivedState(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    // WARNING: Consider using derivedStateOf for 'distinctItems'
    val distinctItems = items.distinct()
    Column(modifier = modifier) {
        distinctItems.forEach { Text(it) }
    }
}

// Rule 37c: StateReadInComposition - State in DisposableEffect
@Composable
fun StateInDisposableEffect(enabledState: State<Boolean>, modifier: Modifier = Modifier) {
    DisposableEffect(Unit) {
        // Note: This pattern may need proper key handling
        val isEnabled = enabledState.value
        println("Enabled: $isEnabled")
        onDispose { }
    }
    Box(modifier = modifier) { Text("DisposableEffect test") }
}

// Rule 38d: FrequentRecomposition - Map created every recomposition
@Composable
fun MapCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: Collection created with 'mapOf' on every recomposition
    val config = mapOf("theme" to "dark", "language" to "en")
    Column(modifier = modifier) {
        config.forEach { (key, value) -> Text("$key: $value") }
    }
}

// Rule 38e: FrequentRecomposition - Set created every recomposition
@Composable
fun SetCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: Collection created with 'setOf' on every recomposition
    val tags = setOf("kotlin", "compose", "android")
    Column(modifier = modifier) {
        tags.forEach { Text(it) }
    }
}

// Rule 38f: FrequentRecomposition - Pair created every recomposition
@Composable
fun PairCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: New 'Pair' instance created on every recomposition
    val dimensions = Pair(100, 200)
    Text("${dimensions.first} x ${dimensions.second}", modifier = modifier)
}

// Rule 38g: FrequentRecomposition - BorderStroke created every recomposition
@Composable
fun BorderStrokeCreationInComposition(modifier: Modifier = Modifier) {
    // WARNING: New 'BorderStroke' instance created on every recomposition
    val stroke = BorderStroke(2.dp, Color.Red)
    Box(modifier = modifier) { Text("Bordered: $stroke") }
}

// Rule 39: MethodReferenceCandidate - Lambda can be replaced with method reference
@Composable
fun MethodReferenceCandidateExample(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
    Button(
        onClick = { viewModel.doSomething() }, // INFO: Can be simplified to viewModel::doSomething
        modifier = modifier,
    ) {
        Text("Click Me")
    }
}

// Rule 39b: MethodReferenceCandidate - onValueChange with single method call
@Composable
fun MethodReferenceCandidateTextField(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
    TextField(
        value = "",
        onValueChange = { viewModel.onTextChanged(it) }, // INFO: Can be viewModel::onTextChanged
        modifier = modifier,
    )
}

// =============================================================================
// CORRECT EXPERIMENTAL EXAMPLES (No warnings expected)
// =============================================================================

// Correct: LazyList with key parameter
@Composable
fun ProperLazyListWithKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// Correct: LazyList with immutable collection
@Composable
fun ProperLazyListWithImmutableItems(
    users: List<User>, // Using List, not MutableList
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// Correct: Heterogeneous LazyList with contentType
@Composable
fun ProperHeterogeneousLazyList(users: List<User>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item(contentType = "header") { Text("Header") }
        items(users, key = { it.id }, contentType = { "user" }) { user ->
            Text(user.name)
        }
        item(contentType = "footer") { Text("Footer") }
    }
}

// Correct: Using derivedStateOf for computed values
@Composable
fun ProperDerivedState(
    items: List<String>,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    val filteredItems by remember(items, searchQuery) {
        derivedStateOf { items.filter { it.contains(searchQuery) } }
    }
    Column(modifier = modifier) {
        filteredItems.forEach { Text(it) }
    }
}

// Correct: State as remember key
@Composable
fun ProperStateInRemember(countState: State<Int>, modifier: Modifier = Modifier) {
    val doubled = remember(countState.value) {
        countState.value * 2
    }
    Text("Doubled: $doubled", modifier = modifier)
}

// Correct: Remembered object
@Composable
fun ProperRememberedObject(modifier: Modifier = Modifier) {
    val style = remember {
        TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
    Text("Hello", style = style, modifier = modifier)
}

// Correct: LazyRow with key parameter
@Composable
fun ProperLazyRowWithKey(users: List<User>, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier) {
        items(users, key = { it.id }) { user ->
            Text(user.name)
        }
    }
}

// Correct: Using derivedStateOf for map operation
@Composable
fun ProperMappedList(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    val mappedItems by remember(items) {
        derivedStateOf { items.map { it.uppercase() } }
    }
    Column(modifier = modifier) {
        mappedItems.forEach { Text(it) }
    }
}

// Correct: Remembered map collection
@Composable
fun ProperRememberedMap(modifier: Modifier = Modifier) {
    val config = remember {
        mapOf("theme" to "dark", "language" to "en")
    }
    Column(modifier = modifier) {
        config.forEach { (key, value) -> Text("$key: $value") }
    }
}

// Correct: Remembered BorderStroke
@Composable
fun ProperRememberedBorderStroke(modifier: Modifier = Modifier) {
    val stroke = remember {
        BorderStroke(2.dp, Color.Red)
    }
    Box(modifier = modifier) { Text("Bordered: $stroke") }
}

// Correct: Using method reference instead of lambda
@Composable
fun ProperMethodReference(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
    Button(
        onClick = viewModel::doSomething, // Clean method reference
        modifier = modifier,
    ) {
        Text("Click Me")
    }
}

// =============================================================================
// HELPER CLASSES AND FUNCTIONS
// =============================================================================

class SampleViewModel : ViewModel() {
    fun doSomething() {}
    fun onTextChanged(text: String) {}
}

data class User(val id: String, val name: String)

interface ColumnScope

fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color): Modifier = this

// LazyColumn helpers
@Composable
fun LazyColumn(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier) { LazyListScopeImpl().content() }
}

@Composable
fun LazyRow(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier) { LazyListScopeImpl().content() }
}

@Composable
fun LazyVerticalGrid(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier) { LazyListScopeImpl().content() }
}

// Checkbox helper
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) { Text("Checkbox: $checked") }
}

// TextField helper (overload for sample)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) { Text("TextField: $value") }
}

// BorderStroke helper
data class BorderStroke(val width: androidx.compose.ui.unit.Dp, val color: Color)

interface LazyListScope {
    fun item(contentType: Any? = null, content: @Composable () -> Unit)
    fun items(
        items: List<Any>,
        key: ((Any) -> Any)? = null,
        contentType: ((Any) -> Any)? = null,
        itemContent: @Composable (Any) -> Unit,
    )
    fun itemsIndexed(
        items: List<Any>,
        key: ((Int, Any) -> Any)? = null,
        contentType: ((Int, Any) -> Any)? = null,
        itemContent: @Composable (Int, Any) -> Unit,
    )
}

private class LazyListScopeImpl : LazyListScope {
    override fun item(contentType: Any?, content: @Composable () -> Unit) {}
    override fun items(
        items: List<Any>,
        key: ((Any) -> Any)?,
        contentType: ((Any) -> Any)?,
        itemContent: @Composable (Any) -> Unit,
    ) {}
    override fun itemsIndexed(
        items: List<Any>,
        key: ((Int, Any) -> Any)?,
        contentType: ((Int, Any) -> Any)?,
        itemContent: @Composable (Int, Any) -> Unit,
    ) {}
}

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

// Placeholder functions
fun mutableIntStateOf(value: Int): Any = mutableStateOf(value)

fun Modifier.fillMaxWidth(): Modifier = this
fun Modifier.height(height: androidx.compose.ui.unit.Dp): Modifier = this
