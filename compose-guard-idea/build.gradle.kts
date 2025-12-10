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
version = "1.0.5"

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
