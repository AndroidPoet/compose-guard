import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kover)
}

// Modules whose test coverage we aggregate and report on. The IntelliJ plugin
// is the unit-testable core; the Android sample is excluded from coverage.
val coveredModules = listOf("compose-guard")

subprojects {
  tasks.withType<KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)

  // Static analysis: detekt with the shared config in config/detekt/detekt.yml.
  apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
  configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    source.setFrom(
      files(
        "src/main/kotlin",
        "src/test/kotlin",
      ).filter { it.exists() },
    )
  }
  tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    reports {
      html.required.set(true)
      xml.required.set(true)
      sarif.required.set(true)
    }
  }

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      ktlint().editorConfigOverride(
        mapOf(
          "indent_size" to 2,
          "continuation_indent_size" to 2,
          "max_line_length" to "off",
        ),
      )
      licenseHeaderFile(rootProject.file("$rootDir/spotless/copyright.kt"))
    }
    format("kts") {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
      target("**/*.xml")
      targetExclude("**/build/**/*.xml")
      licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
    }
  }
}

// Aggregate test coverage across the covered modules. Run `./gradlew koverXmlReport`
// (machine-readable) or `./gradlew koverHtmlReport` (browsable), and `koverVerify`
// to enforce the floor below.
dependencies {
  coveredModules.forEach { kover(project(":$it")) }
}

kover {
  reports {
    verify {
      rule("Aggregate line coverage") {
        // Floor for the PSI rule engine + settings logic (currently ~68%).
        minBound(50)
      }
    }
  }
}
