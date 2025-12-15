# ComposeGuard IntelliJ Plugin

## Version 1.0.7

**Release Date:** December 13, 2025

### What's New in 1.0.7

#### Fixed
- **Settings Save Issue** - Individual rule enable/disable now works correctly
  - Checking a rule checkbox now auto-enables its parent category
  - Fixed `isModified()` detection for proper settings persistence

- **Rule Category Mismatch** - LambdaParameterInEffect moved to correct STATE category in UI

#### Added
- **Comprehensive Test Coverage** - Added 26 new tests for enable/disable functionality
  - Tests for PreviewVisibility, LazyListContentType, LazyListMissingKey rules
  - Tests for category and rule interaction, master switch behavior

#### Improved
- **LazyListContentType Rule Enhancement** - Now highlights both:
  - The `LazyColumn`/`LazyRow` itself with a summary warning
  - Each individual `item()` call that's missing `contentType` parameter

### Previous Releases

#### 1.0.6
- **Statistics Dashboard** - New tool window with violation analytics by category, file, and rule
- **Parameter Ordering Fix** - Modifier is now correctly enforced as the FIRST optional parameter
- **Suppress Built-in Compose Inspections** - Automatically hides Android Studio's built-in Compose lint warnings
- Added EventParameterNaming rule with past tense detection
- Comprehensive test coverage for all 36 rules

#### 1.0.5
- **AvoidComposed rule** - Detects usage of `Modifier.composed {}` and suggests using `Modifier.Node` instead
- **Disable rules feature** - Added settings UI to enable/disable individual rules or entire categories

#### 1.0.4
- Fixed Suppress action not working for ModifierRequired, MultipleContentEmitters, ParameterOrdering rules
- SuppressComposeRuleFix now correctly handles name identifier elements
- Added AddLambdaAsEffectKeyFix and UseRememberUpdatedStateFix quick fixes
- Improved ModifierReuseRule, ParameterOrderingRule, and LambdaParameterInEffectRule

#### 1.0.3
- Improved quick fix behavior - only actionable fixes provided

### Overview

ComposeGuard is an IntelliJ/Android Studio plugin that provides real-time detection of Jetpack Compose best practices and rule violations directly in your IDE.

### Features

- **27 Compose Rules** covering naming, modifiers, state, parameters, and composable structure
- **Real-time Highlighting** - See violations as you type
- **Gutter Icons** - Visual indicators for composable rule status
- **Inline Hints** - Parameter-level rule violation hints
- **Quick Fixes** - One-click fixes for common issues
- **Configurable Rules** - Enable/disable rules per category or individually

### Rule Categories

| Category | Description |
|----------|-------------|
| **Naming** | Composable naming, modifier naming, CompositionLocal naming, preview naming |
| **Modifiers** | Required modifiers, default values, ordering, reuse, avoid composed |
| **State** | Remember state, type-specific state, state hoisting |
| **Parameters** | Ordering, trailing lambdas, mutable types |
| **Composables** | Content emission, preview visibility, effect keys |
| **Stricter** | Material 2 usage, unstable collections (enabled by default) |

### Installation

#### From JetBrains Marketplace

**Settings → Plugins → Marketplace → Search "ComposeGuard"**

Or visit: [JetBrains Marketplace - ComposeGuard](https://plugins.jetbrains.com/plugin/29308-composeguard)

### Compatibility

- **Android Studio:** 2024.2+ (Ladybug and newer)
- **IntelliJ IDEA:** 2024.2+
- **Kotlin:** 2.0+

### Documentation

- [README.md](https://github.com/androidpoet/compose-guard/blob/master/README.md)
- [Issue Tracker](https://github.com/androidpoet/compose-guard/issues)

### Credits

Based on the [Compose Rules](https://mrmans0n.github.io/compose-rules/) documentation by [Mrmans0n](https://github.com/mrmans0n).

---

**Plugin Version:** 1.0.7
**License:** Apache 2.0
