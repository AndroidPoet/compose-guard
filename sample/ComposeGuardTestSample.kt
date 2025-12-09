/*
 * ComposeGuard Test Sample
 *
 * This file contains examples that should trigger all 27 ComposeGuard rules.
 * Open this file in Android Studio with ComposeGuard installed to verify all rules are working.
 *
 * Each section is labeled with the rule it should trigger.
 */

package sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

// =============================================================================
// NAMING RULES
// =============================================================================

// Rule 1: ComposableNamingRule - Composable should start with uppercase
// VIOLATION: lowercase name
@Composable
fun myButton() { // ❌ Should be "MyButton"
    Button(onClick = {}) {
        Text("Click")
    }
}

// Rule 2: CompositionLocalNamingRule - CompositionLocal should start with "Local"
// VIOLATION: doesn't start with "Local"
val CurrentUser = compositionLocalOf { "Guest" } // ❌ Should be "LocalCurrentUser"

// Rule 3: PreviewNamingRule - Preview should end with "Preview"
// VIOLATION: doesn't end with "Preview"
@Preview
@Composable
fun ButtonTest() { // ❌ Should be "ButtonPreview"
    Button(onClick = {}) {
        Text("Test")
    }
}

// Rule 4: MultipreviewNamingRule - Multipreview annotation should end with "Previews"
// VIOLATION: doesn't end with "Previews"
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class ThemePreview // ❌ Should be "ThemePreviews"

// Rule 5: ComposableAnnotationNamingRule - Composable annotation should end with proper suffix
@Composable
annotation class MyComposable // ❌ Custom composable annotations need proper naming

// Rule 6: EventParameterNamingRule - Event parameters should start with "on"
// VIOLATION: event callback doesn't start with "on"
@Composable
fun ButtonWithCallback(
    clickHandler: () -> Unit, // ❌ Should be "onClick"
    textChanged: (String) -> Unit, // ❌ Should be "onTextChanged"
) {
    Button(onClick = clickHandler) {
        Text("Click")
    }
}

// =============================================================================
// MODIFIER RULES
// =============================================================================

// Rule 7: ModifierRequiredRule - Top-level composable should have modifier parameter
// VIOLATION: missing modifier parameter
@Composable
fun CardContent() { // ❌ Should have modifier: Modifier = Modifier parameter
    Column {
        Text("Title")
        Text("Content")
    }
}

// Rule 8: ModifierDefaultValueRule - Modifier should have default value
// VIOLATION: modifier without default value
@Composable
fun CardWithModifier(
    modifier: Modifier, // ❌ Should be: modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Card")
    }
}

// Rule 9: ModifierNamingRule - Modifier parameter should be named "modifier"
// VIOLATION: wrong parameter name
@Composable
fun CardWithWrongName(
    cardModifier: Modifier = Modifier, // ❌ Should be named "modifier"
) {
    Column(modifier = cardModifier) {
        Text("Card")
    }
}

// Rule 10: ModifierTopMostRule - Modifier should be the first optional parameter
// VIOLATION: modifier not in correct position
@Composable
fun CardWithWrongOrder(
    title: String,
    enabled: Boolean = true, // ❌ Optional param before modifier
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(title)
    }
}

// Rule 11: ModifierReuseRule - Don't reuse modifier on multiple children
// VIOLATION: same modifier used on multiple elements
@Composable
fun CardWithModifierReuse(modifier: Modifier = Modifier) {
    Column {
        Text("First", modifier = modifier) // ❌ Modifier reused
        Text("Second", modifier = modifier) // ❌ Modifier reused
    }
}

// Rule 12: ModifierOrderRule - Modifier chain order matters
// VIOLATION: clickable should come before padding for proper touch targets
@Composable
fun CardWithWrongModifierOrder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(16.dp) // ❌ padding before clickable reduces touch target
            .clickable { }
    ) {
        Text("Card")
    }
}

// Rule 13: AvoidComposedRule - Prefer Modifier.Node over composed
// VIOLATION: using composed modifier
fun Modifier.customBorder() = composed { // ❌ Use Modifier.Node instead
    this.border(1.dp, Color.Black)
}

// =============================================================================
// STATE RULES
// =============================================================================

// Rule 14: RememberStateRule - State should be remembered
// VIOLATION: mutableStateOf without remember
@Composable
fun CounterWithoutRemember() {
    var count by mutableStateOf(0) // ❌ Should be: remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// Rule 15: TypeSpecificStateRule - Use type-specific state functions
// VIOLATION: using generic mutableStateOf for primitives
@Composable
fun CounterWithGenericState() {
    var count by remember { mutableStateOf(0) } // ❌ Should use mutableIntStateOf(0)
    var price by remember { mutableStateOf(0.0f) } // ❌ Should use mutableFloatStateOf(0.0f)
    var total by remember { mutableStateOf(0L) } // ❌ Should use mutableLongStateOf(0L)
    var enabled by remember { mutableStateOf(true) } // This is OK for Boolean in some cases

    Text("Count: $count, Price: $price, Total: $total")
}

// Rule 16: MutableStateParameterRule - Don't pass MutableState as parameter
// VIOLATION: MutableState in function parameter
@Composable
fun CounterDisplay(
    count: MutableState<Int>, // ❌ Should use value + callback pattern
) {
    Text("Count: ${count.value}")
}

// Rule 17: HoistStateRule - State should be hoisted properly
// VIOLATION: state created inside composable that should be hoisted
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") } // ❌ Consider hoisting state
    TextField(
        value = query,
        onValueChange = { query = it }
    )
}

// =============================================================================
// PARAMETER RULES
// =============================================================================

// Rule 18: ParameterOrderingRule - Parameters should be ordered correctly
// VIOLATION: wrong parameter order (required, optional with defaults, optional trailing lambdas)
@Composable
fun BadParameterOrder(
    onClick: () -> Unit, // ❌ Lambda should be last
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(title)
    }
}

// Rule 19: TrailingLambdaRule - Single lambda should be trailing
// VIOLATION: lambda not in trailing position
@Composable
fun ButtonWithNonTrailingLambda(
    onClick: () -> Unit, // ❌ Should be trailing (last) parameter
    text: String,
) {
    Button(onClick = onClick) {
        Text(text)
    }
}

// Rule 20: MutableParameterRule - Don't use mutable types as parameters
// VIOLATION: mutable types in parameters
@Composable
fun ListDisplay(
    items: MutableList<String>, // ❌ Should be List<String>
    map: MutableMap<String, Int>, // ❌ Should be Map<String, Int>
    set: MutableSet<String>, // ❌ Should be Set<String>
    array: Array<String>, // ❌ Arrays are mutable, consider List
) {
    Column {
        items.forEach { Text(it) }
    }
}

// Rule 21: ExplicitDependenciesRule - LaunchedEffect should have explicit keys
// VIOLATION: using Unit or missing keys
@Composable
fun EffectWithoutKeys(userId: String) {
    LaunchedEffect(Unit) { // ❌ Should use explicit key like userId
        // fetch user data
    }
}

// =============================================================================
// COMPOSABLE STRUCTURE RULES
// =============================================================================

// Rule 22: ContentEmissionRule - Composable should either emit content or return value
// VIOLATION: composable that neither emits nor returns
@Composable
fun EmptyComposable() { // ❌ Should emit content or return a value
    // Does nothing
}

// Rule 23: MultipleContentRule - Composable with multiple content slots should use named params
// VIOLATION: multiple @Composable lambdas without clear naming
@Composable
fun CardWithMultipleSlots(
    content1: @Composable () -> Unit, // ❌ Unclear naming
    content2: @Composable () -> Unit, // ❌ Unclear naming
) {
    Column {
        content1()
        content2()
    }
}

// Rule 24: EffectKeysRule - Effects should have proper keys
// VIOLATION: missing or incorrect effect keys
@Composable
fun DataLoader(id: String, name: String) {
    LaunchedEffect(id) { // ❌ Missing 'name' in keys if it affects the effect
        // load data based on id AND name
        println("Loading $id $name")
    }
}

// Rule 25: MovableContentRule - movableContentOf should be remembered
// VIOLATION: movableContentOf without remember
@Composable
fun MovableContent() {
    val content = movableContentOf { // ❌ Should be: remember { movableContentOf { ... } }
        Text("Movable")
    }
}

// Rule 26: PreviewVisibilityRule - Preview functions should be private or internal
// VIOLATION: public preview function
@Preview
@Composable
public fun PublicButtonPreview() { // ❌ Should be private
    Button(onClick = {}) {
        Text("Preview")
    }
}

// =============================================================================
// STRICTER RULES
// =============================================================================

// Rule 27: Material2Rule - Prefer Material3 over Material2
// VIOLATION: using Material2 components
@Composable
fun Material2Usage() {
    androidx.compose.material.Button( // ❌ Use androidx.compose.material3.Button
        onClick = {}
    ) {
        androidx.compose.material.Text("Material 2") // ❌ Use material3.Text
    }
}

// Rule 28: UnstableCollectionsRule - Use immutable collections for stability
// VIOLATION: using standard mutable collections
@Composable
fun ListWithUnstableCollections(
    items: List<String>, // ❌ Should use ImmutableList<String>
    itemMap: Map<String, Int>, // ❌ Should use ImmutableMap<String, Int>
) {
    Column {
        items.forEach { Text(it) }
    }
}

// =============================================================================
// CORRECT EXAMPLES (For Reference)
// =============================================================================

// ✅ Correct: Proper naming
@Composable
fun MyButton(modifier: Modifier = Modifier) {
    Button(onClick = {}, modifier = modifier) {
        Text("Click")
    }
}

// ✅ Correct: Proper CompositionLocal naming
val LocalTheme = compositionLocalOf { "Light" }

// ✅ Correct: Proper preview naming
@Preview
@Composable
private fun MyButtonPreview() {
    MyButton()
}

// ✅ Correct: Proper parameter order and modifier
@Composable
fun ProperCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(title)
    }
}

// ✅ Correct: Proper state handling
@Composable
fun ProperCounter() {
    var count by remember { mutableIntStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// ✅ Correct: State hoisting pattern
@Composable
fun ProperSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
    )
}

// ✅ Correct: Using Material3
@Composable
fun Material3Usage(modifier: Modifier = Modifier) {
    androidx.compose.material3.Button(
        onClick = {},
        modifier = modifier
    ) {
        androidx.compose.material3.Text("Material 3")
    }
}
