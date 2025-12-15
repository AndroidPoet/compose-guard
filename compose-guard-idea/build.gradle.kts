/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
  kotlin("jvm")
  id("org.jetbrains.intellij.platform") version "2.10.1"
  id(libs.plugins.spotless.get().pluginId)
}

kotlin {
  explicitApi()
}

group = "io.androidpoet"
version = "1.0.15"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    // Use IntelliJ IDEA Ultimate (has Kotlin bundled)
    intellijIdeaUltimate("2024.2")
    bundledPlugin("org.jetbrains.kotlin")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    pluginVerifier()
  }

  testImplementation(kotlin("test"))
  testImplementation(kotlin("test-junit"))
}

intellijPlatform {
  buildSearchableOptions = false
  instrumentCode = true

  pluginConfiguration {
    ideaVersion {
      sinceBuild = "242"
      untilBuild = "253.*"
    }

    description = """
            Developer: androidpoet (Ranbir Singh)<br/>
            Real-time detection of Compose best practices and rule violations in Android Studio.
            <br/><br/>
            <b>Features:</b>
            <ul>
                <li>35 Compose Rules: Naming, modifiers, state, parameters, effects, and 4 experimental rules</li>
                <li>Real-time Highlighting: See violations as you type</li>
                <li>Gutter Icons: Visual indicators for composable rule status</li>
                <li>Inline Hints: Parameter-level rule violation hints</li>
                <li>Quick Fixes: One-click fixes shown directly in popup</li>
                <li>Configurable Settings: Enable/disable rules by category or individually</li>
                <li>Experimental Rules: LazyList optimizations and lifecycle-aware collection (opt-in)</li>
            </ul>
            <br/>
            Based on <a href="https://mrmans0n.github.io/compose-rules/">Compose Rules</a> documentation.
        """.trimIndent()
    changeNotes = """
            <b>1.0.15</b>
            <ul>
                <li><b>All Rules Enabled by Default</b>: Experimental rules are now enabled by default</li>
                <li>All rule categories (including Experimental) are now active out of the box</li>
            </ul>
            <b>1.0.14</b>
            <ul>
                <li><b>Bug Fix</b>: Fixed critical bug where disabling one rule would disable ALL rules</li>
                <li>Master switch now correctly stays enabled when individual rules are toggled</li>
                <li>Added regression tests to prevent this issue from recurring</li>
            </ul>
            <b>1.0.13</b>
            <ul>
                <li><b>Simplified Settings UX</b>: Removed gray-out/disabled concept entirely</li>
                <li>All checkboxes are now always interactive</li>
                <li>"Enable All Rules" master switch: Select/deselect all rules at once</li>
                <li>Category checkboxes: Act as "select all/none" for rules in that category</li>
                <li>Parent checkbox states are automatically derived from child selections</li>
                <li>Standard IDE settings pattern (like IntelliJ inspections panel)</li>
            </ul>
            <b>1.0.12</b>
            <ul>
                <li><b>Improved Parent-Child Enable/Disable UX</b>: Better hierarchical rule control</li>
                <li>Category enabled: Individual rules can be toggled independently</li>
                <li>Category disabled: All child rules are greyed out but settings preserved</li>
                <li>Re-enabling category restores individual rule settings</li>
                <li>Added <code>isRuleEffectivelyEnabled()</code> for proper hierarchy: Master → Category → Rule</li>
                <li>Added <code>isCategoryEnabled()</code> method for category state checking</li>
                <li><b>Comprehensive Test Coverage</b>: 20+ new tests for hierarchy behavior</li>
            </ul>
            <b>1.0.7</b>
            <ul>
                <li><b>Fixed Settings Save Issue</b>: Individual rule enable/disable now works correctly</li>
                <li>Checking a rule checkbox now auto-enables its parent category</li>
                <li>Fixed isModified() detection for proper settings persistence</li>
                <li><b>Fixed Rule Category Mismatch</b>: LambdaParameterInEffect moved to correct STATE category in UI</li>
                <li><b>Comprehensive Test Coverage</b>: Added 26 new tests for enable/disable functionality</li>
                <li>Tests for PreviewVisibility, LazyListContentType, LazyListMissingKey rules</li>
                <li>Tests for category and rule interaction, master switch behavior</li>
            </ul>
            <b>1.0.6</b>
            <ul>
                <li><b>Statistics Dashboard</b>: New tool window with violation analytics by category, file, and rule</li>
                <li><b>Parameter Ordering Fix</b>: Fixed modifier position to follow official Jetpack Compose guidelines</li>
                <li>Modifier is now correctly enforced as the FIRST optional parameter (not last)</li>
                <li>Order: required → modifier (first optional) → other optionals → content lambda</li>
                <li>Updated ReorderParametersFix quick fix to use correct ordering</li>
                <li><b>Suppress Built-in Compose Inspections</b>: Automatically hides Android Studio's built-in Compose lint warnings when ComposeGuard handles them</li>
                <li>New setting: "Suppress built-in Compose inspections" in Settings → Tools → ComposeGuard</li>
                <li><b>Comprehensive Test Coverage</b>: Added tests for all 36 rules</li>
                <li>New test files for Modifier, Naming, State, Composable, Stricter, Effects, and Performance categories</li>
                <li>Added EventParameterNaming rule with past tense detection</li>
                <li>Improved settings UI with category toggles</li>
            </ul>
            <b>1.0.5</b>
            <ul>
                <li>Bug fixes and stability improvements</li>
                <li>Performance optimizations for rule detection</li>
            </ul>
            <b>1.0.4</b>
            <ul>
                <li>Fixed Suppress action not working for ModifierRequired, MultipleContentEmitters, ParameterOrdering rules</li>
                <li>SuppressComposeRuleFix now correctly handles name identifier elements</li>
                <li>Added AddLambdaAsEffectKeyFix and UseRememberUpdatedStateFix quick fixes</li>
                <li>Improved ModifierReuseRule, ParameterOrderingRule, and LambdaParameterInEffectRule</li>
            </ul>
            <b>1.0.3</b>
            <ul>
                <li>Improved quick fix behavior - only actionable fixes provided</li>
                <li>Added MoveModifierToRootFix for ModifierTopMostRule</li>
                <li>Fixed @Suppress support for EffectKeysRule and LambdaParameterInEffectRule</li>
                <li>3 New Rules: ViewModelForwardingRule, LambdaParameterInEffectRule, ContentSlotReusedRule</li>
                <li>All 32 rules with proper quick fixes or suppress options</li>
            </ul>
            <b>1.0.2</b>
            <ul>
                <li>Bug fixes and stability improvements</li>
                <li>Performance optimizations for rule detection</li>
                <li>Improved ModifierReuseRule, TypeSpecificStateRule, ExplicitDependenciesRule</li>
                <li>RememberStateRule now handles 'by' delegate syntax properly</li>
            </ul>
            <b>1.0.1</b>
            <ul>
                <li>Fixed @Suppress annotation support - @Suppress("RuleId") now properly suppresses warnings</li>
                <li>Supports suppression at function, property, and class level</li>
                <li>All 29 rules fully implemented with quick fixes</li>
                <li>Quick fixes now show directly in popup (HighPriorityAction)</li>
            </ul>
            <b>1.0.0</b>
            <ul>
                <li><b>Initial release of ComposeGuard</b></li>
                <li>27 compose rules covering naming, modifiers, state, parameters, and composable structure</li>
                <li>Real-time detection with inspections, annotators, gutter icons, and inlay hints</li>
                <li>Quick fixes for common violations</li>
                <li>Based on compose-rules documentation</li>
            </ul>
        """.trimIndent()
  }

  pluginVerification {
    ides {
      recommended()
    }
  }

  publishing {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
      freeCompilerArgs.addAll(
        listOf(
          "-Xcontext-receivers",
          "-opt-in=org.jetbrains.kotlin.analysis.api.KaExperimentalApi",
          "-opt-in=org.jetbrains.kotlin.analysis.api.KaImplementationDetail",
        ),
      )
    }
  }

}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}
