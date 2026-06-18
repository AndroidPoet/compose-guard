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
  id(libs.plugins.kover.get().pluginId)
}

kotlin {
  explicitApi()
}

group = "io.androidpoet"
version = "1.2.4"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
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
      untilBuild = "262.*"
    }

    description = """
            Developer: androidpoet (Ranbir Singh)<br/>
            Real-time detection of Compose best practices and rule violations in Android Studio.
            <br/><br/>
            <b>Features:</b>
            <ul>
                <li>36 Compose Rules: Naming, modifiers, state, parameters, composables, and stricter checks</li>
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
            <b>1.2.4</b>
            <ul>
                <li><b>More False-Positive Fixes (second pass)</b>:</li>
                <li><b>DeferStateReads</b>: no longer treats ordinary identifiers (text, index, expanded, progress) as animated state — only values produced by animation/scroll/derived-state builders (including delegated <code>by</code>) are considered frequently-changing.</li>
                <li><b>TypeSpecificState</b>: removed the non-canonical collection-factory check that flagged plain <code>mutableListOf&lt;Int&gt;()</code>; only <code>mutableStateOf</code> primitive variants are suggested.</li>
                <li><b>ModifierReuse &amp; ContentSlotReused</b>: a modifier or content slot used once per mutually-exclusive <code>if</code>/<code>when</code> branch is no longer reported as reuse — only usages reachable on the same composition pass count.</li>
                <li><b>ModifierTopMost</b>: modifiers on content nested inside scope-providing emitters (Scaffold, BoxWithConstraints) or slots that expose a scope parameter are no longer flagged.</li>
                <li><b>AvoidComposed</b>: only the real <code>Modifier.composed { }</code> factory is flagged, not unrelated <code>composed()</code> calls.</li>
                <li><b>MovableContent</b>: accepts the whole remember family (rememberSaveable, custom remember wrappers) instead of only the literal <code>remember</code>.</li>
                <li><b>Tests</b>: added behavioral regression suites for every fix above.</li>
            </ul>
            <b>1.2.3</b>
            <ul>
                <li><b>Major False-Positive Reduction</b>: Audited every rule against the official Compose Rules to stop flagging valid code.</li>
                <li><b>Content emission accuracy</b>: ModifierRequired, ContentEmission and MultipleContentEmitters no longer treat value factories like <code>Color(...)</code>, <code>TextStyle(...)</code> or <code>PaddingValues(...)</code> as emitted UI — only calls whose result is discarded count as emission.</li>
                <li><b>Modifier overrides</b>: ModifierDefaultValue and ModifierNaming skip <code>override</code>/<code>abstract</code> composables, whose parameters cannot legally be renamed or given defaults.</li>
                <li><b>Type matching</b>: MutableParameter and MutableStateParameter now match the parameter's own type, so function types (<code>() -> MutableList</code>), wrappers (<code>Holder&lt;MutableState&gt;</code>) and observable holders (<code>MutableStateFlow</code>) are no longer flagged.</li>
                <li><b>ViewModelForwarding</b>: only flags ViewModels forwarded into another composable, not those handed to ordinary helpers or effects.</li>
                <li><b>EffectKeys</b>: <code>LaunchedEffect(Unit)</code> run-once effects are allowed; a constant key is only flagged when the effect captures parameters that should be keys.</li>
                <li><b>EventParameterNaming</b>: stops flagging present-tense/noun callbacks ending in -ed (<code>onSpeed</code>, <code>onProceed</code>, <code>onFeed</code>).</li>
                <li><b>Material 2</b>: the shared <code>material.ripple</code> package and <code>material.icons</code> star-imports are no longer reported.</li>
                <li><b>TrailingLambda</b>: multi-slot composables (Scaffold-style) no longer require a specific slot to be trailing.</li>
                <li><b>Annotation naming</b>: ComposableAnnotationNaming targets <code>@ComposableTargetMarker</code> annotations, and MultipreviewNaming applies to multipreview annotation classes instead of ordinary stacked-<code>@Preview</code> functions.</li>
                <li><b>Tests</b>: added behavioral regression tests covering each fixed false positive.</li>
            </ul>
            <b>1.2.2</b>
            <ul>
                <li><b>Inspection Registration Fix</b>: Corrected ComposeGuard inspection registration mismatch by aligning the inspection shortName with plugin.xml metadata</li>
                <li><b>Regression Coverage</b>: Added inspection registration tests to prevent future shortName and metadata drift</li>
            </ul>
            <b>1.2.1</b>
            <ul>
                <li><b>False Positive Fix</b>: DerivedStateOfCandidate now ignores collection copies and other computed values inside event handlers such as onClick/onRemove, where the work runs only when the event fires rather than on every recomposition</li>
            </ul>
            <b>1.2.0</b>
            <ul>
                <li><b>False Positive Fixes</b>: DerivedStateOfCandidate now ignores expensive computations inside named event callbacks such as onClick/onRemove</li>
                <li><b>Modifier Root Fix</b>: ModifierTopMost and its quick fix no longer treat CompositionLocalProvider as a root layout</li>
                <li><b>Trailing Lambda Fix</b>: Event lambda detection now works for custom callbacks following the onX naming pattern</li>
                <li><b>Rule Accuracy Improvements</b>: ModifierRequired and ContentEmission now use PSI-based content emission detection</li>
                <li><b>Multiple Content Detection</b>: Custom top-level composables with trailing lambdas are now counted correctly</li>
                <li><b>Release Cleanup</b>: Updated rule counts, changelog metadata, and build configuration warnings</li>
            </ul>
            <b>1.0.9</b>
            <ul>
                <li><b>State/Callback Pairing</b>: ReorderParametersFix now keeps state/callback pairs together (e.g., value/onValueChange, checked/onCheckedChange)</li>
                <li><b>Improved Parameter Ordering</b>: Smart pairing for common patterns like text/onTextChange, query/onQueryChange, selected/onSelectedChange</li>
            </ul>
            <b>1.0.8</b>
            <ul>
                <li><b>Rule Enable/Disable Bug Fix</b>: Fixed critical issue with rule enable/disable functionality</li>
                <li><b>Restructured Settings Architecture</b>: Improved enable/disable state management for individual rules</li>
                <li><b>Settings Persistence Fix</b>: Rule states are now correctly saved and restored across IDE restarts</li>
            </ul>
            <b>1.0.7</b>
            <ul>
                <li><b>All Rules Enabled by Default</b>: All rules including experimental are now enabled out of the box</li>
                <li><b>Simplified Settings UX</b>: Completely redesigned settings panel
                    <ul>
                        <li>All checkboxes are now always interactive (no more grayed-out states)</li>
                        <li>"Enable All Rules" master switch to select/deselect all rules at once</li>
                        <li>Category checkboxes act as "select all/none" for rules in that category</li>
                        <li>Parent checkbox states automatically derived from child selections</li>
                        <li>Standard IDE settings pattern (like IntelliJ inspections panel)</li>
                    </ul>
                </li>
                <li><b>Fixed Critical Settings Bug</b>: Disabling one rule no longer disables ALL rules</li>
                <li><b>Hierarchical Rule Control</b>: Proper Master → Category → Rule hierarchy</li>
                <li><b>Fixed Rule Category Mismatch</b>: LambdaParameterInEffect moved to correct STATE category</li>
                <li><b>Comprehensive Test Coverage</b>: 45+ new tests for settings and enable/disable functionality</li>
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
  named("check") {
    dependsOn("verifyPluginProjectConfiguration")
    dependsOn("verifyPluginStructure")
  }

  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
      freeCompilerArgs.addAll(
        listOf(
          "-Xcontext-parameters",
          "-opt-in=org.jetbrains.kotlin.analysis.api.KaExperimentalApi",
          "-opt-in=org.jetbrains.kotlin.analysis.api.KaImplementationDetail",
        ),
      )
    }
  }

}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
