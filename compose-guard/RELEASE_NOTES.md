# ComposeGuard IntelliJ Plugin

## Version 1.0.7

**Release Date:** December 13, 2025

### What's New in 1.0.7

#### New Features
- **All Rules Enabled by Default** - All rules including experimental are now enabled out of the box
- **Simplified Settings UX** - Completely redesigned settings panel:
  - All checkboxes are now always interactive (no more grayed-out states)
  - "Enable All Rules" master switch to select/deselect all rules at once
  - Category checkboxes act as "select all/none" for rules in that category
  - Parent checkbox states automatically derived from child selections
  - Standard IDE settings pattern (like IntelliJ inspections panel)

#### Fixed
- **Critical Settings Bug** - Disabling one rule no longer disables ALL rules
- **Rule Category Mismatch** - LambdaParameterInEffect moved to correct STATE category in UI
- **Hierarchical Rule Control** - Proper Master → Category → Rule hierarchy now works correctly

#### Added
- **Comprehensive Test Coverage** - 45+ new tests for settings and enable/disable functionality

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
