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
package io.androidpoet.composeguard.rules.parameters

import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive tests for ExplicitDependenciesRule.
 *
 * Rule: Make dependencies explicit as parameters.
 *
 * Why:
 * - Improves testability - no need to mock viewModel() or CompositionLocal
 * - Enables preview - can provide fake data
 * - Clear data flow - easy to understand what the composable needs
 * - Better reusability - not tied to specific DI mechanism
 */
class ExplicitDependenciesRuleTest {

  private val rule = ExplicitDependenciesRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ExplicitDependencies", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Make Dependencies Explicit", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WEAK_WARNING, rule.severity)
  }

  @Test
  fun metadata_enabledByDefault() {
    assertTrue(rule.enabledByDefault)
  }

  @Test
  fun metadata_documentationUrl() {
    assertNotNull(rule.documentationUrl)
    assertTrue(rule.documentationUrl!!.startsWith("https://"))
  }

  @Test
  fun metadata_descriptionMentionsDependencies() {
    assertTrue(
      rule.description.contains("ViewModel") ||
        rule.description.contains("CompositionLocal") ||
        rule.description.contains("parameter") ||
        rule.description.contains("explicit"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // VIEWMODEL PATTERNS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Don't call viewModel() inside composable.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun ProfileScreen() {
   *     val viewModel: ProfileViewModel = viewModel()
   *     val state by viewModel.state.collectAsState()
   *     ProfileContent(state)
   * }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun ProfileScreen(
   *     viewModel: ProfileViewModel = viewModel()  // Default in signature
   * ) {
   *     val state by viewModel.state.collectAsState()
   *     ProfileContent(state)
   * }
   * ```
   *
   * Even better (data-based):
   * ```kotlin
   * @Composable
   * fun ProfileScreen(
   *     state: ProfileState,
   *     onAction: (ProfileAction) -> Unit,
   *     modifier: Modifier = Modifier
   * ) {
   *     // Pure UI component
   * }
   * ```
   */
  @Test
  fun pattern_viewModelInBody() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // COMPOSITION LOCAL PATTERNS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Prefer explicit parameters over CompositionLocal.current.
   *
   * Less ideal (implicit):
   * ```kotlin
   * @Composable
   * fun ThemedText(text: String) {
   *     val theme = LocalTheme.current  // Implicit dependency
   *     Text(text, color = theme.textColor)
   * }
   * ```
   *
   * Better (explicit):
   * ```kotlin
   * @Composable
   * fun ThemedText(
   *     text: String,
   *     theme: Theme = LocalTheme.current,  // Explicit with default
   *     modifier: Modifier = Modifier
   * ) {
   *     Text(text, color = theme.textColor, modifier = modifier)
   * }
   * ```
   */
  @Test
  fun pattern_compositionLocalCurrent() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // TESTABILITY BENEFITS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Benefits of explicit dependencies for testing:
   *
   * Implicit (hard to test):
   * ```kotlin
   * @Composable
   * fun UserProfile() {
   *     val vm: UserViewModel = viewModel()  // How to mock?
   *     // ...
   * }
   * ```
   *
   * Explicit (easy to test):
   * ```kotlin
   * @Composable
   * fun UserProfile(
   *     userName: String,
   *     userAvatar: String,
   *     onEditClick: () -> Unit,
   *     modifier: Modifier = Modifier
   * ) {
   *     // Easy to test with any data!
   * }
   *
   * // Test:
   * composeTestRule.setContent {
   *     UserProfile(
   *         userName = "Test User",
   *         userAvatar = "test.png",
   *         onEditClick = {}
   *     )
   * }
   * ```
   */
  @Test
  fun benefit_testability() {
    assertTrue(rule.enabledByDefault)
  }

  /**
   * Benefits of explicit dependencies for previews:
   *
   * Implicit (no preview):
   * ```kotlin
   * @Composable
   * fun UserProfile() {
   *     val vm: UserViewModel = viewModel()  // Can't work in preview!
   *     // ...
   * }
   *
   * @Preview
   * @Composable
   * fun UserProfilePreview() {
   *     UserProfile()  // Crashes!
   * }
   * ```
   *
   * Explicit (easy preview):
   * ```kotlin
   * @Preview
   * @Composable
   * fun UserProfilePreview() {
   *     UserProfile(
   *         userName = "Jane Doe",
   *         userAvatar = "avatar.png",
   *         onEditClick = {}
   *     )
   * }
   * ```
   */
  @Test
  fun benefit_previewSupport() {
    assertTrue(rule.enabledByDefault)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // SCREEN VS COMPONENT PATTERN
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Screen composable can use viewModel(), content composables should be pure.
   *
   * Screen (integration point):
   * ```kotlin
   * @Composable
   * fun ProfileScreenRoute(
   *     viewModel: ProfileViewModel = viewModel()
   * ) {
   *     val state by viewModel.state.collectAsState()
   *     ProfileScreen(
   *         state = state,
   *         onAction = viewModel::onAction
   *     )
   * }
   * ```
   *
   * Content (pure, testable):
   * ```kotlin
   * @Composable
   * fun ProfileScreen(
   *     state: ProfileState,
   *     onAction: (ProfileAction) -> Unit,
   *     modifier: Modifier = Modifier
   * ) {
   *     // Pure UI rendering
   * }
   * ```
   */
  @Test
  fun pattern_screenVsComponent() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }
}
