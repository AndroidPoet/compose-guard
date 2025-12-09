# Compose Stability Analyzer IntelliJ Plugin 0.4.2

**Release Date:** November 3, 2025

## Overview

This release brings critical bug fixes and stability improvements to the IntelliJ IDEA plugin, ensuring accurate stability analysis for Android-specific patterns and preventing crashes when analyzing complex type hierarchies.

## 🐛 Bug Fixes

### Fixed @Parcelize Data Classes (Issue #3)

The plugin no longer shows false warnings for `@Parcelize`-annotated data classes that implement `Parcelable`.

**What was wrong:**
- Classes with `@Parcelize` were incorrectly flagged with "Extends android.os.Parcelable which has runtime stability" tooltip
- Even when all properties were stable (`val` with stable types), the class was marked as RUNTIME

**Now fixed:**
```kotlin
@Parcelize
data class RecordEditArgs(
    val id: String,              // ✅ Stable
    val pageNumber: Int,         // ✅ Stable
    val quote: String,           // ✅ Stable
    val review: String,          // ✅ Stable
    // ... all val properties with stable types
) : Parcelable  // ✅ Now correctly shows as STABLE
```

**Technical details:**
- The plugin now checks only the properties of `@Parcelize` classes
- Ignores the Parcelable interface's runtime stability
- If all properties are `val` and have stable types → class is STABLE

### Fixed StackOverflowError for Recursive Types (Issue #11)

The plugin no longer crashes when analyzing code with recursive type references or complex function type aliases.

**Problematic patterns that are now handled:**
```kotlin
// Function type aliases
typealias ScreenTransitionContent = @Composable AnimatedVisibilityScope.(Screen) -> Unit

// Complex nested generics
fun ScreenTransition(
    transitionSpec: AnimatedContentTransitionScope<Screen>.() -> ContentTransform
)

// Self-referential types
data class Node(val children: List<Node>)
```

**Solution:** Implemented cycle detection that safely breaks infinite recursion by returning RUNTIME stability when a circular reference is detected.

### Fixed Compose Shape Types Stability

Shape types from Compose Foundation were incorrectly showing as RUNTIME.

**Fixed types:**
- `RoundedCornerShape` ✅
- `CircleShape` ✅
- `CutCornerShape` ✅
- `CornerBasedShape` ✅
- `AbsoluteRoundedCornerShape` ✅
- `AbsoluteCutCornerShape` ✅
- `RectangleShape` ✅

These are now correctly recognized as STABLE types in:
- Hover tooltips
- Gutter icons
- Inline parameter hints
- Code inspections

## 🎯 Visual Improvements

### Gutter Icons

Fixed gutter icon colors for:
- ✅ Green dot for STABLE composables (including @Parcelize classes)
- ⚠️ Yellow/orange dot for RUNTIME composables (only when truly runtime-dependent)
- ❌ Red dot for UNSTABLE composables (mutable properties)

### Hover Tooltips

More accurate stability information:
```
✅ Before: "Extends android.os.Parcelable which has runtime stability"
✅ After:  "@Parcelize with all stable properties"
```

### Inline Hints

Parameter hints now correctly show:
- `shape: RoundedCornerShape` → **STABLE** badge (was showing RUNTIME)
- `args: RecordEditArgs` → **STABLE** badge (was showing RUNTIME for @Parcelize)

## 🔧 Technical Improvements

### Cycle Detection

Implemented ThreadLocal-based cycle detection:
```kotlin
private val analyzingTypes = ThreadLocal.withInitial { mutableSetOf<String>() }
```

**Benefits:**
- Thread-safe analysis in IDE background tasks
- Prevents infinite recursion
- Minimal performance overhead
- Graceful handling of edge cases

### Consistent with Compiler Plugin

The IDEA plugin now uses the same analysis order as the compiler plugin:
1. Nullable types
2. Type parameters
3. Function types
4. Known stable types
5. Annotations (@Stable, @Immutable)
6. Primitives
7. String, Unit, Nothing
8. Collections
9. Value classes
10. Enums
11. **@Parcelize** (new priority)
12. Interfaces
13. Abstract classes
14. Regular classes
15. @StabilityInferred

This ensures IDE analysis matches compilation results.

## 📦 Installation

### From Disk (Recommended for Pre-Release)

1. Download: [compose-stability-analyzer-idea-0.4.2.zip](https://github.com/skydoves/compose-stability-analyzer/releases/tag/0.4.2)
2. Open **Android Studio** or **IntelliJ IDEA**
3. Go to **Settings → Plugins**
4. Click **⚙️ (gear icon) → Install Plugin from Disk...**
5. Select the downloaded `.zip` file
6. Restart IDE

### From Marketplace (Coming Soon)

Once approved by JetBrains, you'll be able to install directly from:
**Settings → Plugins → Marketplace → Search "Compose Stability Analyzer"**

## ⚙️ Plugin Settings

The plugin settings remain unchanged. You can still customize:

**Settings → Tools → Compose Stability Analyzer**

- ✅ Enable/disable stability checks
- 🎨 Customize gutter icon colors
- 🎨 Customize inline hint colors
- 📝 Set stability configuration file path
- 🚫 Add ignored type patterns
- ⚡ Enable Strong Skipping mode

## 🐛 Known Issues

None reported for this release. If you encounter any issues, please report them at:
https://github.com/skydoves/compose-stability-analyzer/issues

## 📊 Compatibility

- **Android Studio:** 2023.3+ (Hedgehog and newer)
- **IntelliJ IDEA:** 2023.3+ (Community or Ultimate)
- **Kotlin:** 2.0.21+
- **Compose:** Any version

## 🔄 Updating from 0.4.1

No configuration changes needed. Simply:
1. Uninstall the old version (optional, IDE can update in place)
2. Install 0.4.2 following the installation steps above
3. Restart your IDE

Your custom settings will be preserved.

## 📝 What's Next?

We're continuously improving the plugin. Upcoming features:
- Performance optimizations for large codebases
- More customization options for visual indicators
- Integration with stability validation reports
- Support for custom stability annotations

## 🙏 Acknowledgments

Special thanks to the community for reporting issues:
- [@apptechxonia](https://github.com/apptechxonia) - @Parcelize issue report
- [@noloman](https://github.com/noloman) - @Parcelize confirmation
- [@Tolriq](https://github.com/Tolriq) - StackOverflowError report

Your feedback helps make this plugin better! 🎉

## 📖 Documentation

For complete documentation, visit:
- [README.md](https://github.com/skydoves/compose-stability-analyzer/blob/main/README.md)
- [Plugin Documentation](https://github.com/skydoves/compose-stability-analyzer/tree/main/compose-stability-analyzer-idea)
- [Issue Tracker](https://github.com/skydoves/compose-stability-analyzer/issues)

---

**Plugin Version:** 0.4.2
**Release Date:** November 3, 2025
**License:** Apache 2.0
