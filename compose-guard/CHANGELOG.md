# ComposeGuard IntelliJ Plugin - Changelog

All notable changes to the IntelliJ IDEA plugin will be documented in this file.

## [Unreleased]

### Added
- **New rule: ComponentDefaults Visibility** - Implements the upstream Compose Rules check ["ComponentDefaults object should match the composable visibility"](https://mrmans0n.github.io/compose-rules/rules/#componentdefaults-object-should-match-the-composable-visibility), the one catalog rule compose-guard did not yet cover. A `<Component>Defaults` object is flagged when its visibility differs from the composable it accompanies (e.g. a public `MyComponent` with a `private object MyComponentDefaults`), because a more-restricted defaults object stops callers from reading and building on those defaults. Matching visibility (both public, both internal, …) and composables with no defaults object are left alone. Registered in the COMPOSABLE category with a behavioral test suite.
- **Quick fix: Match composable visibility** - The ComponentDefaults Visibility rule now offers a quick fix that rewrites the defaults object's visibility modifier to match the composable (dropping the mismatched modifier and adding the target one, or removing it entirely for `public`), making the violation one-click actionable instead of suppress-only.

### Documentation
- Brought the README and `DOCUMENTATION.md` in line with the full **37-rule** catalog. The README's three "36 rules" counts are corrected and the Rule Reference table now lists `ComponentDefaultsVisibility`. `DOCUMENTATION.md` (which still claimed "29 rules" and documented only 30) now states 37 and adds the seven previously-undocumented rules — `ModifierNaming`, `DerivedStateOfCandidate`, `FrequentRecomposition`, `DeferStateReads`, `ComponentDefaultsVisibility`, `LazyListMissingKey`, and `LazyListContentType` — each with wrong/correct code examples in its category.

### Fixed (False Negatives)
- **TypeSpecificState** - Float literals written without a decimal point — `mutableStateOf(0f)`, `1f`, `100F` — are now flagged. These are ubiquitous in Compose (alpha, progress, rotation), but the inferred-type regex required a decimal point, so only `1.5f`-style literals were caught. `mutableFloatStateOf` is now suggested for the no-dot form too; `Int`/`Long`/`Double` inference is unchanged.
- **ExplicitDependencies** - CompositionLocal reads are now detected. The rule scanned only call expressions for a callee starting with `Local`, but the canonical read `LocalFoo.current` is a property access (`LocalFoo` is a reference, not a call), so this half of the rule — its headline CompositionLocal case — never fired. Reads of a custom `Local….current` in the body are now flagged via the dot-qualified `.current` shape, while the platform CompositionLocals (`LocalContext`, `LocalDensity`, …) remain exempt. The ViewModel-factory half was unaffected.
- **MutableStateParameter** - A parameter whose type IS a `MutableState` holding a function — e.g. `MutableState<() -> Unit>` — is now correctly flagged. The "skip function types" guard (meant for factories like `() -> MutableState<T>`) matched a `->` anywhere in the type text, so the arrow nested inside the type argument made a genuine `MutableState` parameter slip through. It now exempts only top-level function types; `() -> MutableState<T>` and `Holder<MutableState<T>>` remain unflagged.
- **MutableParameter** - A mutable collection whose type arguments contain a function type — e.g. `MutableMap<String, () -> Unit>` — is now correctly flagged. The function-type exemption previously matched a `->` anywhere in the type text, so the nested lambda made the whole parameter slip through. It now exempts only top-level function types (`() -> MutableList<T>`, `MutableList<T>.() -> Unit`), which remain unflagged.
- **MultipleContentEmitters** - The rule now sees through transparent wrappers (`CompositionLocalProvider`, `key`) when counting top-level emissions. Because these introduce no layout node, multiple emitters placed directly inside one — e.g. `CompositionLocalProvider { Text(a); Text(b) }` — are genuinely multiple top-level emissions, but were previously skipped entirely and went unreported. A wrapper around a single grouping container (`CompositionLocalProvider { Column { … } }`) still counts as one emission.

### Fixed (False Positives)
- **UnstableCollections** - Overriding composables are no longer flagged for `List`/`Set`/`Map` parameters. The fix rewrites the parameter type (`List` → `ImmutableList`), a signature change an override cannot make — its parameter types are fixed by the supertype. The rule now skips overrides, matching the override exemption across the other rules whose quick-fix alters the signature. `ModifierDefaultValue` already excluded overrides (and abstract declarations) for the same reason; rules with only a Suppress fix (MutableParameter, MutableStateParameter) are intentionally left in place. Non-override composables are still checked.
- **HoistState** - Overriding composables are no longer flagged. HoistState's fix hoists local state into a new parameter — a signature change an override cannot make (its signature is fixed by the supertype). The rule now skips overrides, completing the override exemption across every rule whose quick-fix alters the function signature (ComposableNaming, ModifierNaming, ModifierRequired, ParameterOrdering, TrailingLambda). Non-override composables are still checked.
- **ParameterOrdering / TrailingLambda** - Overriding composables are no longer flagged for parameter order or content-slot position. An `override` inherits its signature from the supertype and cannot reorder parameters, so the reorder fixes these rules suggest are not actionable (and the violation is inherited, not introduced by the override). Both now skip overrides, completing the override exemption already present in ComposableNaming, ModifierNaming, and ModifierRequired. Non-override composables are still checked.
- **ModifierRequired** - Overriding composables are no longer flagged for a missing `Modifier` parameter. An `override` inherits its signature from the supertype, so adding a `modifier` parameter would break the override — the suggested fix is not actionable. The rule now skips overrides, matching ModifierNaming (which already did) and the upstream `ModifierMissing` behaviour. Non-override public emitters are still required to expose a modifier.
- **LambdaParameterInEffect** - Parameter detection no longer over-matches. The "is this a lambda parameter" check treated any type whose text contained `->` or `Function` as a function type, so a `Map<String, () -> Unit>` (arrow nested in a type argument) or an ordinary class like `FunctionRegistry` was wrongly flagged when referenced inside a `LaunchedEffect`/`DisposableEffect`. A parameter now counts only when its own type is a function type — a top-level arrow (`() -> Unit`, `@Composable () -> Unit`, `T.() -> Unit`) or the Kotlin `Function`/`FunctionN` interface. Genuine lambda parameters are still checked.
- **ParameterOrdering** - A parameter typed `List<@Composable () -> Unit>` (a collection of composable pages/slots) is no longer treated as a trailing content slot. The content-lambda check matched `->` anywhere in the type text, so the arrow nested inside the generic made the list look like a content lambda — which then wrongly reported "content lambda should be at the end" when an ordinary parameter followed it. The arrow must now appear at the top level; genuine `@Composable () -> Unit` slots are still required to trail.
- **TrailingLambda** - A parameter typed `List<@Composable () -> Unit>` (a collection of composable pages/slots, e.g. a pager's `pages`) is no longer misread as a content slot. The composable-lambda check matched `->` anywhere in the type text, so the arrow nested inside the generic made the whole list look like a content lambda — which then wrongly flagged a trailing `onX` event handler as "should not be trailing lambda" and could mis-suggest reordering. The arrow must now appear at the top level; genuine `@Composable () -> Unit` slots are still detected. Shared with ContentSlotReused, which uses the same helper.
- **EffectKeys** - Lambda (function-type) parameters captured in a constant-key effect are no longer flagged by EffectKeys with one-sided "pass as key" advice. That case is owned by LambdaParameterInEffect, which offers the correct choice between adding a key and `rememberUpdatedState`. Non-lambda captured parameters (including a `Map<String, () -> Unit>`, whose arrow is only inside its type arguments) are still flagged.
- **ModifierOrder** - A single bound-reducing modifier that precedes multiple interaction modifiers (e.g. `Modifier.padding(16.dp).clickable { }.toggleable(...) { }`) is now reported once instead of once per following interaction modifier, removing duplicate warnings pointing at the same `padding`.
- **RememberState** - A function-valued property whose lambda produces state — e.g. `val factory = { mutableStateOf(0) }` or `val make: () -> Any = { derivedStateOf { 1 } }` — is no longer flagged. The state builder runs when the lambda is invoked, not at composition, so wrapping it in `remember` is nonsensical. This matches the lambda-value guard already in DerivedStateOfCandidate, and is an ERROR-severity rule so the false positive was especially disruptive.
- **MultipleContentEmitters / ContentEmission / ModifierRequired** - Behavior-registering composables that emit no UI — `BackHandler`, `PredictiveBackHandler`, `LifecycleStartEffect`, `LifecycleResumeEffect`, `LifecycleEventEffect` — are no longer counted as content emitters. Previously only the three core effects (`LaunchedEffect`/`DisposableEffect`/`SideEffect`) were recognized as non-emitting, so a composable like `{ BackHandler { }; Column { … } }` was wrongly reported as emitting multiple pieces of content, a `BackHandler`-plus-`return` function was flagged by ContentEmission, and a `BackHandler`-only function was treated as UI-emitting by ModifierRequired. Both the shared non-emitter set and MultipleContentEmitters' local set now include these.
- **ContentSlotReused** - A content slot invoked exactly once through a safe call (`content?.invoke()`) is no longer reported as "invoked multiple times". The safe-qualified invocation was counted twice — once as the call and once again as its receiver reference — so a single invocation looked like two; this also restores the per-branch single-use logic for safe invokes. Added a regression test.
- **CompositionLocalNaming** - CompositionLocal detection now inspects the property's actual factory call instead of substring-matching the initializer text. A property whose initializer merely mentions the name — e.g. `val msg = "see compositionLocalOf docs"` (a string literal) or `val f = ::staticCompositionLocalOf` (a callable reference) — is no longer misclassified as a CompositionLocal and wrongly flagged for missing a `Local` prefix. This also corrects the gutter annotator and the statistics service, which share the same helper.
- **ContentEmission / ComposableNaming / ModifierRequired** - The shared `returnsUnit()` helper now recognizes the fully-qualified `kotlin.Unit` return type, not only the bare `Unit` spelling. Previously a composable written as `@Composable fun Foo(): kotlin.Unit { … }` was treated as value-returning, which produced a false ContentEmission report ("emits content but returns kotlin.Unit"), a false ComposableNaming report (expecting camelCase for a "value-returning" function), and a missed ModifierRequired check. All three now treat it correctly as a content emitter.
- **ModifierOrder** - `offset` is no longer treated as a touch-target-reducing modifier. `Modifier.offset(...).clickable { }` translates the element without shrinking its tappable area, so it is no longer reported; only `padding` before an interaction modifier is flagged.
- **LazyListContentType** - Nested lazy lists no longer contaminate each other's item counts. A `LazyColumn { items(...) { LazyRow { items(...) } } }` is no longer reported as heterogeneous because the inner `LazyRow`'s items are now attributed to the inner list, not the outer one.

- **FrequentRecomposition** - Custom collectors whose names start with `collectAsState` (e.g. `collectAsStateList()`, `collectAsStateMap()`) are no longer flagged. The lifecycle-aware-collection suggestion now matches the `collectAsState` callee exactly instead of by substring.
- **DeferStateReads** - The state-usage check inside a modifier's arguments is now identifier-aware instead of substring-based. An animated state with a short name — e.g. `val x by animateFloatAsState(...)` — no longer makes an unrelated modifier read like `Modifier.size(maxWidth)` look like a deferred-read candidate just because `maxWidth` contains the letter `x`. The author had already hardened the property-name side; this closes the same substring gap on the usage side (both the `usesFrequentState` check and the `.dp`/`.sp` `hasStateReference` path). State genuinely read in the modifier is still flagged.
- **HoistState** - State-usage detection is now identifier-aware instead of substring-based. A local state named `value` is no longer flagged because a child has a `value =` parameter (`Slider(value = 0f)`), and a state named `count` is no longer flagged because child names contain it as a substring (`AccountBadge`). State genuinely read by, passed to, or reassigned for children is still reported.
- **DerivedStateOfCandidate** - Lambda-valued properties such as `val onSave = { items.sortedBy { ... } }` are no longer flagged. The expensive operation inside a function value runs when the lambda is invoked, not during composition, so it is not a `remember`/`derivedStateOf` candidate. Computations evaluated directly at composition time are still reported.
- **MultipleContentEmitters** - Side effects (`LaunchedEffect`, `DisposableEffect`, `SideEffect`) are no longer counted as content emitters. A composable with one effect and one real emitter (e.g. `LaunchedEffect(Unit) { }` next to a `Box { ... }`) is no longer reported as emitting multiple pieces of content.

### Tests
- Added a **dead-rule sweep** that exercises every default-enabled rule against a canonical violating corpus and asserts each one fires — a regression guard against a rule silently going dead (as `LazyListMissingKey` had).
- Added a **clean-code false-positive sweep** that runs every default-enabled rule over idiomatic, rule-following Compose and asserts nothing fires — a regression guard against new false positives.
- Added **behavioral quick-fix tests** that actually apply `AddModifierParameter`, `HoistState`, `ReorderParameters`, `AddKeyParameter`, `MoveToTrailingLambda`, and `AddContentType` fixes to real PSI and assert the result is syntactically valid, correctly ordered, and preserves item content (previously these fixes had only string-comparison stubs).
- Added a **state/modifier fix behavior suite** covering `UseTypeSpecificState`, `AddModifierDefaultValue`, `UseImmutableCollection`, and `UseLifecycleAwareCollector` — applied to real PSI with assertions that no syntax error or `IllegalStateException` results and that the correct import is added. This suite surfaced the detached-element import crash above.
- Added an **effect fix behavior suite** that applies `UseRememberUpdatedState` to a real `LaunchedEffect` and asserts no crash plus the correct import — the test that surfaced the rememberUpdatedState detached-reference crash above.
- Added a **move-modifier-to-root behavior test** that applies the fix to a child modifier and asserts it lands on the root layout and is removed from the child — the test that surfaced the modifier-duplication bug above.
- Added a **rename-parameter behavior test** that asserts both the declaration and its body usages are renamed and the old name is gone — the test that surfaced the dangling-reference bug above.
- Added regression coverage locking in the previously-untested fixes that were verified correct: an **add-explicit-parameter behavior test** (hoists an implicit ViewModel to a defaulted parameter and rewrites the local-variable usages), plus a **misc quick-fix suite** covering `MakePreviewPrivate` (adds `private`, replaces `internal`), `UseMaterial3` (rewrites a `material` import to `material3`), and `AddLambdaAsEffectKey` (replaces a `Unit` effect key with the captured lambda).
- Added a **wrap-in-remember behavior suite** (wraps a bare expression in `remember { }`, keys it on the parameters it reads, adds the `remember` import, and does not double-wrap an expression already inside `remember`) and a **use-lambda-modifier behavior suite** (`alpha`/`rotate` → `graphicsLayer { }`, `offset` → `offset { IntOffset(...) }`) — both fixes were already correct; the tests guard them.
- Added a **rename-fix behavior suite** covering the resolution-based reference renaming above: a composable rename updates both the declaration and its call sites, a CompositionLocal rename updates both the declaration and its reads, and a same-named string literal is left untouched.
- Added **real behavioral coverage for `LambdaParameterInEffect`** that runs the rule against code and asserts it fires (lambda used in `LaunchedEffect`/`DisposableEffect` without a key) and does not fire (lambda passed as the effect key, the `rememberUpdatedState` pattern, non-lambda parameters). The existing `*RuleTest` `pattern_*`/`reason_*` methods only assert metadata (`rule.category`) despite `shouldViolate`/`shouldNotViolate` names, so they did not actually guard detection; this suite does.
- Added **real behavioral coverage for `ComposableNaming`, `EventParameterNaming`, and `RememberState`** — three rules that previously had only the metadata-only `*RuleTest` stubs and no real detection test. The new suites run each rule and assert: composable naming flags lowercase Unit-returning and uppercase value-returning composables while leaving the correct casing and remember-family names alone; event-parameter naming flags a past-tense lambda parameter (`onClicked`) while leaving present-tense (`onClick`), `-eed` words (`onProceed`), and non-lambda parameters alone; and remember-state flags `mutableStateOf`/`derivedStateOf`/`by`-delegated state created outside `remember` while leaving state already wrapped in `remember` alone.
- Added **real behavioral coverage for `UnstableCollections`, `ViewModelForwarding`, and `ModifierDefaultValue`** — three more rules that previously had only metadata-only stubs. The suites assert: unstable-collections flags `List`/`Map` parameters while leaving `ImmutableList` and non-collection parameters alone; ViewModel-forwarding flags a ViewModel passed to a child composable (`Child(viewModel = viewModel)`) while leaving a lowercase helper call and plain receiver usage alone; and modifier-default-value flags a `modifier: Modifier` parameter without a default while leaving `modifier: Modifier = Modifier` and modifier-less functions alone.
- Added **real behavioral coverage for `PreviewVisibility`, `AvoidComposed`, and `ModifierNaming`** — three more rules that previously had only metadata-only stubs. The suites assert: preview-visibility flags a public `@Preview` while leaving a private one and a non-preview composable alone; avoid-composed flags `composed { }` inside a `Modifier` extension factory while leaving a bare `composed()` (no lambda), a composed-free factory, and a non-`Modifier`-extension function alone; and modifier-naming flags a misnamed modifier parameter (`mod`) while leaving `modifier` and the sub-component `xModifier` suffix alone.
- Added **real behavioral coverage for `PreviewNaming`, `CompositionLocalNaming`, and `MutableParameter`** — three more rules that previously had only metadata-only stubs. The suites assert: preview-naming flags a `@Preview` whose name lacks "Preview" while leaving a `…Preview` name and a non-preview function alone; composition-local-naming flags a `compositionLocalOf`/`staticCompositionLocalOf` property without a `Local` prefix while leaving a `Local…` name and an ordinary (non-CompositionLocal) property alone; and mutable-parameter flags a `MutableList` parameter while leaving a `List` parameter and a `() -> MutableList` factory function type alone.
- Added **real behavioral coverage for `MultipreviewNaming`, `ComposableAnnotationNaming`, and `ContentEmission`** — three more rules that previously had only metadata-only stubs. The suites assert: multipreview-naming flags a `@Preview`-meta-annotated annotation class whose name lacks "Preview" while leaving a `Preview…` name and a non-multipreview annotation alone; composable-annotation-naming flags a `@ComposableTargetMarker` applier annotation whose name lacks a `Composable` suffix while leaving a `…Composable` name and a plain annotation alone; and content-emission flags a composable that both emits UI and returns a value while leaving a value-returning non-emitter and a Unit-returning emitter alone.
- Added **real behavioral coverage for `ParameterOrdering` and `ExplicitDependencies`** — the last two rules that lacked a real detection test. The suites assert: parameter-ordering flags a required parameter placed after an optional one while leaving correct ordering and single-parameter functions alone; and explicit-dependencies flags an implicit ViewModel factory call (`viewModel()`) while leaving an explicitly-passed ViewModel parameter and a factory call that already takes an argument alone. This completes the conversion: **every default rule now has at least one suite that actually runs the rule and asserts detection**, rather than only the metadata-only `pattern_*`/`reason_*` stubs.

### Fixed (Quick Fixes)
- **Add key parameter** - When a lazy list passed its content as a named `itemContent = { ... }` argument (rather than a trailing lambda), the "Add key parameter" quick fix dropped the content, producing `items(...) { }` with an empty body. The content is now preserved as the trailing lambda.
- **Reorder modifiers** - The reorder quick fix no longer treats `offset` as a touch-target-reducing modifier, matching the rule. A chain like `Modifier.offset(...).padding(...).clickable { }` now moves only `padding` after `clickable`, leaving `offset` in place.
- **Wrap in derivedStateOf** - The fix now converts the property to `by` delegation (`val x by remember { derivedStateOf { ... } }`) and adds the `getValue` import, instead of `val x = remember { derivedStateOf { ... } }` which produced a `State<T>` and broke reads like `if (x)`. The existing type annotation is preserved.
- **Use type-specific state / Use immutable collection / Use lifecycle-aware collector** - These three fixes threw `IllegalStateException("KtElement not inside KtFile")` and failed to apply whenever the new import was missing. Each replaced its target element and then read `containingKtFile` from that now-detached element to add the import. The file is now captured before the replace, so the import is added reliably.
- **Use rememberUpdatedState** - Same crash class: the fix rewrote every reference to the lambda inside restartable effects and then read `containingKtFile` from the originally-flagged reference — which is itself one of the rewritten (now-detached) references — throwing `IllegalStateException`. The file is now captured before the rewrite, so the `rememberUpdatedState`/`getValue` imports are added reliably.
- **Move modifier to root** - The fix added the modifier to the root layout first (replacing the whole root subtree, which detached the child call) and then tried to remove it from the now-detached child — a no-op. The result was the modifier **duplicated** on both the root and the child (e.g. `Column(modifier = …) { Text(…, modifier = …) }`). The child is now stripped first (in place, keeping the root attached), then the modifier is added to the root, so it is moved rather than copied.
- **Event parameter rename (present-tense suggestion)** - EventParameterNaming suggested a mangled name for callbacks whose verb root ends in a double `s`: `onPressed` → `onPres`, `onDismissed` → `onDismis`, `onMissed` → `onMis`, `onPassed` → `onPas`. The past-tense-to-present conversion stripped one of the doubled consonants (correct for `onStopped` → `onStop`), but English never doubles `s` for the past tense, so a root ending in `ss` (press, dismiss, miss, pass, cross, address) must keep both. The suggestion — and the rename quick fix that applies it — is now correct for these. Genuine doubled-consonant cases (`onTapped` → `onTap`) are unchanged.
- **Rename parameter** - The rename updated only the parameter's declaration, leaving every usage in the function body referring to the old name (e.g. `fun S(onClick: () -> Unit) { onClicked() }`) — non-compiling code. Usages within the function body are now renamed alongside the declaration.
- **Rename composable / Add Local prefix** - Both fixes renamed only the declaration identifier, leaving every call site / read of the symbol pointing at the old name (non-compiling code). They now also rename all references via a resolution-based reference search, so call sites and CompositionLocal reads are updated too. Because the search resolves references rather than matching text, unrelated same-named symbols are left untouched — e.g. renaming a composable named `item` does not disturb the `LazyListScope.item { }` DSL, nor a `"item"` string literal.

### Fixed (Detection)
- **Gutter icons honour suppression** - The line-marker gutter icon counted suppressed violations: after suppressing a rule (via `@Suppress("RuleId")` or a `noinspection` comment) the inline highlight disappeared but the gutter still showed the violation — wrong color, inflated count. The line-marker provider now filters suppressed violations, matching the inline annotator and the statistics scan.
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
