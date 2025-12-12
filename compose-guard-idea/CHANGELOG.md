# ComposeGuard IntelliJ Plugin - Changelog

All notable changes to the IntelliJ IDEA plugin will be documented in this file.

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
