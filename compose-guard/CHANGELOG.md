# ComposeGuard IntelliJ Plugin - Changelog

All notable changes to the IntelliJ IDEA plugin will be documented in this file.

## [Unreleased]

### Fixed (False Positives)
- **ModifierOrder** - `offset` is no longer treated as a touch-target-reducing modifier. `Modifier.offset(...).clickable { }` translates the element without shrinking its tappable area, so it is no longer reported; only `padding` before an interaction modifier is flagged.
- **LazyListContentType** - Nested lazy lists no longer contaminate each other's item counts. A `LazyColumn { items(...) { LazyRow { items(...) } } }` is no longer reported as heterogeneous because the inner `LazyRow`'s items are now attributed to the inner list, not the outer one.

- **FrequentRecomposition** - Custom collectors whose names start with `collectAsState` (e.g. `collectAsStateList()`, `collectAsStateMap()`) are no longer flagged. The lifecycle-aware-collection suggestion now matches the `collectAsState` callee exactly instead of by substring.
- **HoistState** - State-usage detection is now identifier-aware instead of substring-based. A local state named `value` is no longer flagged because a child has a `value =` parameter (`Slider(value = 0f)`), and a state named `count` is no longer flagged because child names contain it as a substring (`AccountBadge`). State genuinely read by, passed to, or reassigned for children is still reported.
- **DerivedStateOfCandidate** - Lambda-valued properties such as `val onSave = { items.sortedBy { ... } }` are no longer flagged. The expensive operation inside a function value runs when the lambda is invoked, not during composition, so it is not a `remember`/`derivedStateOf` candidate. Computations evaluated directly at composition time are still reported.
- **MultipleContentEmitters** - Side effects (`LaunchedEffect`, `DisposableEffect`, `SideEffect`) are no longer counted as content emitters. A composable with one effect and one real emitter (e.g. `LaunchedEffect(Unit) { }` next to a `Box { ... }`) is no longer reported as emitting multiple pieces of content.

### Tests
- Added a **dead-rule sweep** that exercises every default-enabled rule against a canonical violating corpus and asserts each one fires — a regression guard against a rule silently going dead (as `LazyListMissingKey` had).
- Added a **clean-code false-positive sweep** that runs every default-enabled rule over idiomatic, rule-following Compose and asserts nothing fires — a regression guard against new false positives.
- Added **behavioral quick-fix tests** that actually apply `AddModifierParameter`, `HoistState`, and `ReorderParameters` fixes to real PSI and assert the result is syntactically valid and correctly ordered (previously these fixes had only string-comparison stubs).

### Fixed (Detection)
- **LazyListMissingKey** - The rule was silently non-functional: the trailing content lambda was mistaken for a positional `key` argument, so `items(list) { ... }` never reported. Detection now inspects only the in-parentheses arguments, so keyless `items`/`itemsIndexed` are correctly flagged while named and positional `key` forms stay clean. Severity lowered from Warning to **Info** so the now-working rule surfaces as a gentle hint rather than a noisy warning on every list.

---

## [1.2.4] - 2026-06-11

### Fixed (False Positives — second pass)
- **DeferStateReads** - Dropped the property-name substring heuristic that matched `x`/`y` inside ordinary identifiers (`text`, `index`, `expanded`, `progress`). Frequently-changing state is now detected only from animation/scroll/derived-state builder initializers, including delegated `by` properties.
- **TypeSpecificState** - Removed the non-canonical collection-factory branch that flagged plain `mutableListOf<Int>()` and suggested the unrelated androidx `mutableIntListOf`. The rule now only covers `mutableStateOf` primitive variants.
- **ModifierReuse** / **ContentSlotReused** - A modifier or content slot used once per mutually-exclusive `if`/`when` branch is no longer reported; only usages reachable on the same composition pass are treated as reuse.
- **ModifierTopMost** - Modifiers applied to content nested inside scope-providing emitters (`Scaffold`, `BoxWithConstraints`) or any content slot that exposes a scope parameter are no longer flagged.
- **AvoidComposed** - Only the real `Modifier.composed { ... }` factory (invoked with a lambda) is flagged, not unrelated `composed()` member calls.
- **MovableContent** - Accepts the whole `remember` family (`rememberSaveable`, custom `rememberRetained`, …) instead of only the literal `remember`.

### Tests
- Added `*FalsePositiveTest` behavioral suites covering each of the above (768 tests total).

---

## [1.2.3] - 2026-06-11

### Fixed (False Positives)
- **Content emission detection** - `ModifierRequired`, `ContentEmission` and `MultipleContentEmitters` no longer treat value factories such as `Color(...)`, `TextStyle(...)` or `PaddingValues(...)` as emitted UI. Only calls whose result is discarded (statement position) are counted as content emission.
- **Modifier overrides** - `ModifierDefaultValue` and `ModifierNaming` now skip `override`/`abstract` composables, whose inherited parameters cannot legally be renamed or given default values (the previous quick fixes produced non-compiling code).
- **Mutable type matching** - `MutableParameter` and `MutableStateParameter` now match the parameter's own outer type instead of a substring, so function types (`() -> MutableList<T>`), wrappers (`Holder<MutableState<T>>`) and observable holders (`MutableStateFlow`/`MutableSharedFlow`) are no longer flagged.
- **ViewModelForwarding** - Only flags ViewModels forwarded into another composable, not those passed to ordinary helpers, builders or effects.
- **EffectKeys** - `LaunchedEffect(Unit)` run-once effects are allowed; a constant key is only flagged when the effect captures parameters that should be keys.
- **EventParameterNaming** - Present-tense verbs and nouns ending in `-ed` (`onSpeed`, `onProceed`, `onFeed`, `onNeed`) are no longer reported as past tense.
- **Material 2** - The shared `androidx.compose.material.ripple` package and `androidx.compose.material.icons` star-imports are no longer reported as Material 2 usage.
- **TrailingLambda** - Multi-slot composables (e.g. Scaffold-style) no longer require a particular content slot to be trailing.
- **ComposableAnnotationNaming** - Targets `@ComposableTargetMarker` applier annotations instead of any `@Composable`-annotated class.
- **MultipreviewNaming** - Applies to multipreview annotation classes; ordinary composables that stack multiple `@Preview` annotations are no longer flagged.

### Tests
- Added `*FalsePositiveTest` behavioral suites (parsing real Kotlin via the IntelliJ test fixture) that assert each of the above is no longer flagged while genuine violations still are.

---

## [1.2.1] - 2026-05-15

### Fixed
- **DerivedStateOfCandidate Event Handler False Positive** - Computed values inside event callbacks such as `onClick` and `onRemove` are no longer reported as recomposition-time `remember` candidates.

---

## [1.2.0] - 2026-05-13

### Fixed
- **DerivedStateOfCandidate False Positives** - Expensive computations inside named event callbacks such as `onClick` and `onRemove` no longer trigger remember suggestions
- **ModifierTopMost Root Detection** - `CompositionLocalProvider` is now treated as a transparent wrapper instead of a root layout
- **MoveModifierToRootFix Targeting** - The quick fix now moves modifiers to the nearest real root layout instead of wrapper composables
- **TrailingLambda Event Detection** - Custom event callbacks following the `onX` naming pattern are now recognized consistently

### Improved
- **Composable Emission Detection** - `ModifierRequired` and `ContentEmission` now use PSI-based detection instead of broad text matching
- **MultipleContent Rule Accuracy** - Top-level custom composables with trailing lambdas are now counted correctly
- **Release Metadata Consistency** - Version notes, rule counts, and marketplace-facing descriptions are now aligned for release

---

## [1.0.7] - 2025-12-13

### Added
- **All Rules Enabled by Default** - All rules including experimental are now enabled out of the box
- **Simplified Settings UX** - Completely redesigned settings panel:
  - All checkboxes are now always interactive (no more grayed-out states)
  - "Enable All Rules" master switch to select/deselect all rules at once
  - Category checkboxes act as "select all/none" for rules in that category
  - Parent checkbox states automatically derived from child selections
  - Standard IDE settings pattern (like IntelliJ inspections panel)
- **ExplicitDependencies Quick Fix** - Added "Add as parameter" quick fix for implicit dependencies
- **Comprehensive Test Coverage** - 45+ new tests for settings and enable/disable functionality

### Fixed
- **Critical Settings Bug** - Disabling one rule no longer disables ALL rules
- **Rule Category Mismatch** - LambdaParameterInEffect moved to correct STATE category in UI
- **AddContentTypeFix Bug** - Lambda arguments now correctly recognized as content
- **ExplicitDependencies Visibility** - Changed severity for proper wavy underline highlighting

### Changed
- **Rule Package Reorganization** - Experimental rules moved to proper categories
  - `LazyListMissingKey` and `LazyListContentType` → COMPOSABLE category
  - `DerivedStateOfCandidate` and `FrequentRecomposition` → STATE category

### Improved
- **LazyListContentType Rule Enhancement** - Now highlights both lazy containers and individual item() calls
- Better hierarchical rule control with proper Master → Category → Rule hierarchy

---

## [1.0.6] - 2025-12-12

### Added
- **4 New Experimental Rules** (disabled by default, enable in settings):
  - `LazyListMissingKey` - items() should have key parameter
  - `LazyListContentType` - heterogeneous items need contentType
  - `DerivedStateOfCandidate` - computed values should use remember with keys
  - `FrequentRecomposition` - suggest collectAsStateWithLifecycle for flows

- **Statistics Dashboard** - New tool window showing:
  - Real-time violation statistics across your project
  - Rule category breakdown and trends
  - Visual dashboard for tracking code quality

- **Quick Fixes** for experimental rules:
  - `AddKeyParameterFix` - Adds key parameter to lazy list items
  - `AddContentTypeFix` - Adds contentType to lazy list items
  - `UseLifecycleAwareCollectorFix` - Replaces collectAsState with collectAsStateWithLifecycle

---

## [1.0.5] - 2025-12-10

### Added
- **AvoidComposed rule** - Detects usage of `Modifier.composed {}` and suggests using `Modifier.Node` instead
- **Disable rules feature** - Added settings UI to enable/disable individual rules or entire categories
- **Documentation** - Added disable rules screenshot and documentation

---

## [1.0.4] - 2025-12-10

### Fixed
- Fixed Suppress action not working for ModifierRequired, MultipleContentEmitters, ParameterOrdering rules
- SuppressComposeRuleFix now correctly handles name identifier elements

### Added
- AddLambdaAsEffectKeyFix and UseRememberUpdatedStateFix quick fixes

### Improved
- ModifierReuseRule, ParameterOrderingRule, and LambdaParameterInEffectRule

---

## [1.0.3] - 2025-12-10

### Improved
- Quick fix behavior - only actionable fixes provided

---

## [1.0.2] - 2025-12-09

### Added
- Smart rules improvements

---

## [1.0.1] - 2025-12-09

### Fixed
- Bug fixes and stability improvements

---

## [1.0.0] - 2025-12-09

### Added
- **Initial release of ComposeGuard**
- **27 Compose Rules** covering:
  - **Naming**: Composable naming, modifier naming, CompositionLocal naming, preview naming
  - **Modifiers**: Required modifiers, default values, ordering, reuse
  - **State**: Remember state, type-specific state, state hoisting
  - **Parameters**: Ordering, trailing lambdas, mutable types
  - **Composables**: Content emission, preview visibility, effect keys
  - **Stricter**: Material 2 usage, unstable collections
- **Real-time detection** with inspections and annotators
- **Gutter icons** showing rule violation status
- **Inline hints** for parameter-level rule violations
- **Quick fixes** for common issues
- **K2 Analysis API support** for accurate and fast analysis
- Based on [Compose Rules](https://mrmans0n.github.io/compose-rules/) documentation

---

## Legend

- **Added** - New features
- **Changed** - Changes in existing functionality
- **Deprecated** - Soon-to-be removed features
- **Removed** - Removed features
- **Fixed** - Bug fixes
- **Improved** - Enhancements to existing features
- **Security** - Security-related changes

## Links

- [Plugin Repository](https://github.com/androidpoet/compose-guard)
- [Issue Tracker](https://github.com/androidpoet/compose-guard/issues)
- [Compose Rules Documentation](https://mrmans0n.github.io/compose-rules/)
