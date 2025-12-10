# ComposeGuard Documentation

**ComposeGuard** is a real-time Jetpack Compose best practices inspector for IntelliJ IDEA and Android Studio. It provides instant feedback as you write code with visual indicators, quick fixes, and detailed explanations for Compose rule violations.

---

## Table of Contents

- [Installation](#installation)
- [Features](#features)
- [Rules Reference](#rules-reference)
  - [Naming Rules](#naming-rules)
  - [Modifier Rules](#modifier-rules)
  - [State Rules](#state-rules)
  - [Parameter Rules](#parameter-rules)
  - [Composable Rules](#composable-rules)
  - [Stricter Rules](#stricter-rules)
- [Quick Fixes](#quick-fixes)
- [Configuration](#configuration)
- [Suppressing Rules](#suppressing-rules)
- [Examples](#examples)

---

## Installation

### Requirements

- **IDE:** IntelliJ IDEA 2024.2+ or Android Studio Ladybug+
- **Kotlin Plugin:** Required
- **K2 Mode:** Fully supported

### Install from JetBrains Marketplace

1. Open **Settings/Preferences** → **Plugins**
2. Search for "ComposeGuard"
3. Click **Install**
4. Restart your IDE

### Install from Disk

1. Download the plugin `.zip` file
2. Open **Settings/Preferences** → **Plugins**
3. Click the gear icon → **Install Plugin from Disk...**
4. Select the downloaded file
5. Restart your IDE

---

## Features

### Real-Time Code Analysis

ComposeGuard analyzes your Compose code as you type and provides:

- **Inline Highlighting** - Colored underlines for violations
- **Gutter Icons** - Visual indicators in the left margin
- **Inline Hints** - Small badges next to function names
- **Hover Tooltips** - Detailed explanations on mouse hover

### Severity Levels

| Color | Severity | Description |
|-------|----------|-------------|
| Red | Error | Critical issues that will cause bugs |
| Orange | Warning | Best practice violations |
| Gray | Weak Warning | Minor style issues |
| Blue | Info | Informational suggestions |

### Quick Fixes

Press `Alt+Enter` (Windows/Linux) or `Cmd+Enter` (macOS) on any highlighted issue to see available quick fixes.

---

## Rules Reference

ComposeGuard includes **29 rules** organized into 6 categories.

### Naming Rules

#### ComposableNaming
**Severity:** Warning

Composable functions that return `Unit` should use PascalCase. Functions that return a value should use camelCase.

```kotlin
// Wrong
@Composable
fun userCard(user: User) { }  // Should be PascalCase

// Correct
@Composable
fun UserCard(user: User) { }

// Value-returning composables use camelCase
@Composable
fun rememberScrollState(): ScrollState { }
```

#### CompositionLocalNaming
**Severity:** Warning

CompositionLocal properties must be prefixed with `Local`.

```kotlin
// Wrong
val CurrentTheme = compositionLocalOf { Theme.Light }

// Correct
val LocalTheme = compositionLocalOf { Theme.Light }
```

#### PreviewNaming
**Severity:** Warning

Preview functions should contain "Preview" in their name.

```kotlin
// Wrong
@Preview
@Composable
private fun UserCard() { }

// Correct
@Preview
@Composable
private fun UserCardPreview() { }
```

#### MultipreviewNaming
**Severity:** Warning

Multipreview annotations should start with `Previews`.

```kotlin
// Wrong
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class LightDarkPreview

// Correct
@Preview(name = "Light")
@Preview(name = "Dark")
annotation class PreviewsLightDark
```

#### EventParameterNaming
**Severity:** Warning

Event callback parameters should follow the `onX` pattern with present tense.

```kotlin
// Wrong
@Composable
fun Button(onClicked: () -> Unit) { }  // Past tense

// Correct
@Composable
fun Button(onClick: () -> Unit) { }
```

#### ComposableAnnotationNaming
**Severity:** Warning

Custom composable annotations should end with "Composable".

```kotlin
// Wrong
@Composable
annotation class GoogleMap

// Correct
@Composable
annotation class GoogleMapComposable
```

---

### Modifier Rules

#### ModifierRequired
**Severity:** Warning

Public composables that emit UI should accept a `modifier` parameter.

```kotlin
// Wrong
@Composable
fun ProductCard(product: Product) {
    Card { /* ... */ }
}

// Correct
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) { /* ... */ }
}
```

#### ModifierDefaultValue
**Severity:** Warning

Modifier parameters should have `= Modifier` as the default value.

```kotlin
// Wrong
@Composable
fun Card(modifier: Modifier) { }

// Correct
@Composable
fun Card(modifier: Modifier = Modifier) { }
```

#### ModifierTopMost
**Severity:** Warning

The modifier parameter should be applied to the root/top-most layout.

```kotlin
// Wrong
@Composable
fun Card(modifier: Modifier = Modifier) {
    Column {
        Text("Title", modifier = modifier)  // Applied to child, not root
    }
}

// Correct
@Composable
fun Card(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Title")
    }
}
```

#### ModifierReuse
**Severity:** Warning

Don't reuse the same modifier instance on multiple children.

```kotlin
// Wrong
@Composable
fun Card(modifier: Modifier = Modifier) {
    Column {
        Text("First", modifier = modifier)
        Text("Second", modifier = modifier)  // Reused!
    }
}

// Correct
@Composable
fun Card(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("First")
        Text("Second")
    }
}
```

#### ModifierOrder
**Severity:** Warning

Modifier chain order matters. Certain modifiers must come before others.

```kotlin
// Wrong - clickable area won't be clipped
Box(
    modifier = Modifier
        .clickable { }
        .clip(CircleShape)
)

// Correct - click area is properly clipped
Box(
    modifier = Modifier
        .clip(CircleShape)
        .clickable { }
)
```

#### AvoidComposed
**Severity:** Warning

Avoid `Modifier.composed { }`. Use `Modifier.Node` for better performance.

```kotlin
// Wrong
fun Modifier.fade() = composed {
    val alpha by animateFloatAsState(1f)
    this.alpha(alpha)
}

// Correct - Use Modifier.Node pattern instead
```

---

### State Rules

#### RememberState
**Severity:** Error

State creators must be wrapped in `remember { }` to survive recomposition.

```kotlin
// Wrong - State is recreated on every recomposition!
@Composable
fun Counter() {
    val count = mutableStateOf(0)
}

// Correct
@Composable
fun Counter() {
    val count = remember { mutableStateOf(0) }
    // or using delegate syntax:
    var count by remember { mutableStateOf(0) }
}
```

#### TypeSpecificState
**Severity:** Warning

Use type-specific state functions for primitives.

```kotlin
// Wrong
val count = remember { mutableStateOf(0) }

// Correct - Better performance
val count = remember { mutableIntStateOf(0) }

// Available type-specific functions:
// - mutableIntStateOf()
// - mutableLongStateOf()
// - mutableFloatStateOf()
// - mutableDoubleStateOf()
```

#### HoistState
**Severity:** Warning

State should be hoisted to the appropriate parent level.

```kotlin
// Wrong - State is too low in the hierarchy
@Composable
fun Parent() {
    val state = remember { mutableStateOf("") }
    Child(state)  // Passing state down
}

// Correct - Hoist state and pass value + callback
@Composable
fun Parent() {
    var value by remember { mutableStateOf("") }
    Child(value = value, onValueChange = { value = it })
}
```

#### MutableStateParameter
**Severity:** Warning

Don't pass `MutableState<T>` as a parameter. Use value + callback pattern.

```kotlin
// Wrong
@Composable
fun TextField(state: MutableState<String>) { }

// Correct
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit
) { }
```

---

### Parameter Rules

#### ParameterOrdering
**Severity:** Warning

Parameters should be ordered: required params → modifier → optional params → content slot.

```kotlin
// Wrong
@Composable
fun Card(
    content: @Composable () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) { }

// Correct
@Composable
fun Card(
    title: String,                          // Required first
    modifier: Modifier = Modifier,          // Modifier after required
    subtitle: String = "",                  // Optional params
    content: @Composable () -> Unit         // Content slot last
) { }
```

#### TrailingLambda
**Severity:** Warning

Content slot parameters should be last to enable trailing lambda syntax.

```kotlin
// Wrong - Cannot use trailing lambda syntax
@Composable
fun Card(
    content: @Composable () -> Unit,
    title: String,
) { }

// Correct - Enables: Card("Title") { /* content */ }
@Composable
fun Card(
    title: String,
    content: @Composable () -> Unit
) { }
```

#### MutableParameter
**Severity:** Warning

Don't use mutable collection types as parameters.

```kotlin
// Wrong
@Composable
fun ItemList(items: MutableList<Item>) { }

// Correct
@Composable
fun ItemList(items: List<Item>) { }

// Best - Use immutable collections for stability
@Composable
fun ItemList(items: ImmutableList<Item>) { }
```

#### ExplicitDependencies
**Severity:** Info

ViewModel and DI dependencies should be explicit parameters.

```kotlin
// Wrong - Implicit dependency
@Composable
fun UserScreen() {
    val viewModel: UserViewModel = viewModel()
}

// Correct - Explicit dependency
@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel()
) { }
```

#### ViewModelForwarding
**Severity:** Warning

Don't pass ViewModels through composable layers.

```kotlin
// Wrong - ViewModel passed to children
@Composable
fun Parent(viewModel: MainViewModel) {
    Child(viewModel = viewModel)
}

// Correct - Pass only necessary data and callbacks
@Composable
fun Parent(viewModel: MainViewModel) {
    Child(
        data = viewModel.data,
        onAction = viewModel::handleAction
    )
}
```

---

### Composable Rules

#### ContentEmission
**Severity:** Warning

Composables should either emit UI content OR return a value, not both.

```kotlin
// Wrong - Both emits content and returns value
@Composable
fun BadComposable(): State<Int> {
    Text("Hello")
    return remember { mutableIntStateOf(0) }
}

// Correct - Emit only
@Composable
fun GoodComposable() {
    Text("Hello")
}

// Correct - Return only
@Composable
fun rememberCounter(): State<Int> {
    return remember { mutableIntStateOf(0) }
}
```

#### MultipleContent
**Severity:** Warning

Don't emit multiple top-level layout nodes.

```kotlin
// Wrong - Multiple top-level nodes
@Composable
fun Card() {
    Text("First")
    Text("Second")
}

// Correct - Single root container
@Composable
fun Card() {
    Column {
        Text("First")
        Text("Second")
    }
}
```

#### ContentSlotReused
**Severity:** Warning

Content slots shouldn't be invoked multiple times in branching code.

```kotlin
// Wrong - Content invoked in multiple branches
@Composable
fun Container(content: @Composable () -> Unit) {
    if (condition) {
        content()
    } else {
        content()  // Reused in another branch
    }
}

// Correct - Use movableContentOf
@Composable
fun Container(content: @Composable () -> Unit) {
    val movableContent = remember { movableContentOf(content) }
    if (condition) {
        movableContent()
    } else {
        movableContent()
    }
}
```

#### EffectKeys
**Severity:** Warning

Effects must have proper restart keys.

```kotlin
// Wrong - Missing key
@Composable
fun Timer(duration: Int) {
    LaunchedEffect(Unit) {
        delay(duration.toLong())  // duration not in keys!
    }
}

// Correct - Include all dependencies as keys
@Composable
fun Timer(duration: Int) {
    LaunchedEffect(duration) {
        delay(duration.toLong())
    }
}
```

#### LambdaParameterInEffect
**Severity:** Warning

Lambda parameters used in effects must be keys or use `rememberUpdatedState`.

```kotlin
// Wrong
@Composable
fun Button(onClick: () -> Unit) {
    LaunchedEffect(Unit) {
        onClick()  // Lambda not in keys
    }
}

// Correct - Use rememberUpdatedState
@Composable
fun Button(onClick: () -> Unit) {
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(Unit) {
        currentOnClick()
    }
}
```

#### MovableContent
**Severity:** Error

`movableContentOf` must be wrapped in `remember`.

```kotlin
// Wrong - Recreated on every recomposition
@Composable
fun Container(content: @Composable () -> Unit) {
    val movable = movableContentOf(content)
}

// Correct
@Composable
fun Container(content: @Composable () -> Unit) {
    val movable = remember { movableContentOf(content) }
}
```

#### PreviewVisibility
**Severity:** Warning

Preview functions should be `private`.

```kotlin
// Wrong
@Preview
@Composable
fun CardPreview() { }

// Correct
@Preview
@Composable
private fun CardPreview() { }
```

---

### Stricter Rules

These rules are enabled by default but can be disabled if they don't fit your project.

#### Material2
**Severity:** Info

Detects Material 2 usage and suggests migration to Material 3.

```kotlin
// Detected - Material 2 import
import androidx.compose.material.Button

// Suggested - Material 3
import androidx.compose.material3.Button
```

#### UnstableCollections
**Severity:** Warning

Use immutable collections for Compose stability.

```kotlin
// Wrong - Unstable collection
@Composable
fun ItemList(items: List<Item>) { }

// Correct - Stable immutable collection
@Composable
fun ItemList(items: ImmutableList<Item>) { }
```

---

## Quick Fixes

ComposeGuard provides automatic fixes for most violations. Press `Alt+Enter` (Windows/Linux) or `Cmd+Enter` (macOS) to see available fixes.

| Quick Fix | Description |
|-----------|-------------|
| Rename Composable | Fix naming convention violations |
| Add Modifier Parameter | Add missing modifier parameter |
| Add Default Value | Add `= Modifier` default |
| Wrap in Remember | Wrap state in `remember { }` |
| Use Type-Specific State | Convert to `mutableIntStateOf`, etc. |
| Make Preview Private | Add `private` modifier to preview |
| Use Immutable Collection | Replace with `ImmutableList`, etc. |
| Migrate to Material 3 | Update imports to Material 3 |
| Reorder Parameters | Fix parameter ordering |
| Reorder Modifiers | Fix modifier chain order |
| Move to Trailing Lambda | Move content parameter to last position |
| Move Modifier to Root | Apply modifier to root layout |
| Add Local Prefix | Add "Local" prefix to CompositionLocal |
| Suppress Rule | Add @Suppress annotation |

---

## Configuration

### Settings Location

**Settings/Preferences** → **Tools** → **ComposeGuard**

### Available Options

- Enable/disable entire rule categories
- Toggle individual rules on/off
- Settings persist between sessions

---

## Suppressing Rules

### Using @Suppress Annotation

```kotlin
@Suppress("ComposableNaming")
@Composable
fun myComposable() { }

// Suppress multiple rules
@Suppress("ComposableNaming", "ModifierRequired")
@Composable
fun myComposable() { }
```

### Using IntelliJ Comment

```kotlin
// noinspection ComposableNaming
@Composable
fun myComposable() { }
```

### Rule IDs for Suppression

| Category | Rule IDs |
|----------|----------|
| Naming | `ComposableNaming`, `CompositionLocalNaming`, `PreviewNaming`, `MultipreviewNaming`, `EventParameterNaming`, `ComposableAnnotationNaming` |
| Modifier | `ModifierRequired`, `ModifierDefaultValue`, `ModifierTopMost`, `ModifierReuse`, `ModifierOrder`, `AvoidComposed` |
| State | `RememberState`, `TypeSpecificState`, `HoistState`, `MutableStateParameter` |
| Parameter | `ParameterOrdering`, `TrailingLambda`, `MutableParameter`, `ExplicitDependencies`, `ViewModelForwarding` |
| Composable | `ContentEmission`, `MultipleContent`, `ContentSlotReused`, `EffectKeys`, `LambdaParameterInEffect`, `MovableContent`, `PreviewVisibility` |
| Stricter | `Material2`, `UnstableCollections` |

---

## Examples

### Before ComposeGuard

```kotlin
@Composable
fun userProfile(
    content: @Composable () -> Unit,
    user: User,
    onClicked: () -> Unit,
) {
    val expanded = mutableStateOf(false)
    val items = mutableListOf<Item>()

    Column {
        Text(user.name)
        Button(onClick = onClicked) {
            Text("Click")
        }
    }
    Text("Footer")
}
```

### After ComposeGuard Fixes

```kotlin
@Composable
fun UserProfile(                              // PascalCase naming
    user: User,                               // Required params first
    modifier: Modifier = Modifier,            // Modifier parameter added
    onClick: () -> Unit = {},                 // Present tense naming
    content: @Composable () -> Unit           // Content slot last
) {
    var expanded by remember { mutableStateOf(false) }  // Wrapped in remember
    val items: ImmutableList<Item> = persistentListOf() // Immutable collection

    Column(modifier = modifier) {             // Modifier applied to root
        Text(user.name)
        Button(onClick = onClick) {
            Text("Click")
        }
        Text("Footer")                        // Single root container
        content()
    }
}
```

---

## Resources

- [Compose Rules Documentation](https://mrmans0n.github.io/compose-rules/)
- [Jetpack Compose API Guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)

---

## Support

- **Issues:** [GitHub Issues](https://github.com/user/compose-guard/issues)
- **Author:** androidpoet (Ranbir Singh)

---

## License

Apache License 2.0
