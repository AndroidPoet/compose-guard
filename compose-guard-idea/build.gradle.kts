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
version = "1.6.2"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2025.2")
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
                <li>32 Compose Rules: Naming, modifiers, state, parameters, effects, and more</li>
                <li>Real-time Highlighting: See violations as you type</li>
                <li>Gutter Icons: Visual indicators for composable rule status</li>
                <li>Inline Hints: Parameter-level rule violation hints</li>
                <li>Quick Fixes: One-click fixes shown directly in popup</li>
            </ul>
            <br/>
            Based on <a href="https://mrmans0n.github.io/compose-rules/">Compose Rules</a> documentation.
        """.trimIndent()
    changeNotes = """
            <b>1.7.0</b>
            <ul>
                <li>Improved quick fix behavior - only actionable fixes provided</li>
                <li>Removed unnecessary quick fixes for complex rules requiring manual changes</li>
                <li>Added MoveModifierToRootFix for ModifierTopMostRule</li>
                <li>Fixed @Suppress support for EffectKeysRule and LambdaParameterInEffectRule</li>
                <li>All 31 rules verified against compose-rules documentation</li>
            </ul>
            <b>1.6.0</b>
            <ul>
                <li>Bug fixes and stability improvements</li>
                <li>Performance optimizations for rule detection</li>
            </ul>
            <b>1.5.0</b>
            <ul>
                <li><b>3 New Rules Added:</b></li>
                <li>ViewModelForwardingRule - Detects passing ViewModels through composable functions</li>
                <li>LambdaParameterInEffectRule - Detects lambda params in effects without proper keys</li>
                <li>ContentSlotReusedRule - Detects reused content slots that lose state</li>
                <li><b>Improved Existing Rules:</b></li>
                <li>ModifierReuseRule - Now tracks modifier reassignments</li>
                <li>TypeSpecificStateRule - Detects 30+ primitive collection types</li>
                <li>ExplicitDependenciesRule - Skips navigation contexts and overrides</li>
                <li>ComposableNamingRule - Skips operators and overrides</li>
                <li>RememberStateRule - Now handles 'by' delegate syntax properly</li>
                <li>ModifierOrderRule - Added ReorderModifiersFix quick fix</li>
                <li>CompositionLocalNamingRule - Added AddLocalPrefixFix quick fix</li>
                <li><b>All 32 rules with proper quick fixes or suppress options</b></li>
            </ul>
            <b>1.4.0</b>
            <ul>
                <li><b>Fixed @Suppress annotation support</b> - @Suppress("RuleId") now properly suppresses warnings</li>
                <li>Added isSuppressed() utility function for checking @Suppress annotations</li>
                <li>Supports suppression at function, property, and class level</li>
                <li>Also supports IntelliJ-style // noinspection comments</li>
                <li>All 28 rules verified and working correctly</li>
                <li>All 17 quick fixes verified and working correctly</li>
            </ul>
            <b>1.3.0</b>
            <ul>
                <li><b>All 29 rules now fully implemented</b></li>
                <li>Implemented ModifierOrderRule: Detects padding/offset before clickable (reduces touch target)</li>
                <li>Implemented AvoidComposedRule: Now properly detects composed {} in Modifier extensions</li>
                <li>Implemented ParameterOrderingRule: Detects lambdas before non-lambda parameters</li>
                <li>Implemented MultipleContentRule: Detects generic names (content1, content2) for slots</li>
                <li>Implemented ComposableAnnotationNamingRule: Enforces "Composable" suffix on custom annotations</li>
                <li>Implemented HoistStateRule: Suggests hoisting state for better reusability</li>
                <li>Quick fixes now show directly in popup (HighPriorityAction)</li>
                <li>Suppress actions moved to low priority</li>
                <li>Added support for analyzing annotation classes</li>
            </ul>
            <b>1.0.0</b>
            <ul>
                <li><b>Initial release of ComposeGuard</b></li>
                <li>27 compose rules covering naming, modifiers, state, parameters, and composable structure</li>
                <li>Real-time detection with inspections, annotators, gutter icons, and inlay hints</li>
                <li>Quick fixes for common violations</li>
                <li>Stricter rules (Material 2 usage, unstable collections) enabled by default</li>
                <li>Based on compose-rules documentation</li>
            </ul>
        """.trimIndent()
  }

  pluginVerification {
    ides {
      recommended()
    }
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
