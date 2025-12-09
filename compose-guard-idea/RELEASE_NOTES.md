# ComposeGuard IntelliJ Plugin

## Version 1.0.1

**Release Date:** December 2025

### Overview

ComposeGuard is an IntelliJ/Android Studio plugin that provides real-time detection of Jetpack Compose best practices and rule violations directly in your IDE.

### Features

- **27 Compose Rules** covering naming, modifiers, state, parameters, and composable structure
- **Real-time Highlighting** - See violations as you type
- **Gutter Icons** - Visual indicators for composable rule status
- **Inline Hints** - Parameter-level rule violation hints
- **Quick Fixes** - One-click fixes for common issues

### Rule Categories

| Category | Description |
|----------|-------------|
| **Naming** | Composable naming, modifier naming, CompositionLocal naming, preview naming |
| **Modifiers** | Required modifiers, default values, ordering, reuse |
| **State** | Remember state, type-specific state, state hoisting |
| **Parameters** | Ordering, trailing lambdas, mutable types |
| **Composables** | Content emission, preview visibility, effect keys |
| **Stricter** | Material 2 usage, unstable collections (enabled by default) |

### Installation

#### From Disk

1. Download: [compose-guard-idea-1.0.2.zip](https://github.com/androidpoet/compose-guard/releases)
2. Open **Android Studio** or **IntelliJ IDEA**
3. Go to **Settings → Plugins**
4. Click **⚙️ (gear icon) → Install Plugin from Disk...**
5. Select the downloaded `.zip` file
6. Restart IDE

#### From JetBrains Marketplace (Coming Soon)

**Settings → Plugins → Marketplace → Search "ComposeGuard"**

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

**Plugin Version:** 1.0.1
**License:** Apache 2.0
