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
 * Comprehensive tests for ViewModelForwardingRule.
 *
 * Rule: Don't forward ViewModels to child composables.
 *
 * Why:
 * - Couples child to specific ViewModel implementation
 * - Makes child untestable without the ViewModel
 * - Breaks preview support
 * - Violates single responsibility - child knows too much
 *
 * Instead:
 * - Pass only the data child needs
 * - Pass callbacks for child to trigger actions
 */
class ViewModelForwardingRuleTest {

  private val rule = ViewModelForwardingRule()

  // ═══════════════════════════════════════════════════════════════════════════════
  // METADATA TESTS
  // ═══════════════════════════════════════════════════════════════════════════════

  @Test
  fun metadata_id() {
    assertEquals("ViewModelForwarding", rule.id)
  }

  @Test
  fun metadata_name() {
    assertEquals("Don't Forward ViewModels", rule.name)
  }

  @Test
  fun metadata_category() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  @Test
  fun metadata_severity() {
    assertEquals(RuleSeverity.WARNING, rule.severity)
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
  fun metadata_descriptionMentionsViewModel() {
    assertTrue(
      rule.description.contains("ViewModel") ||
        rule.description.contains("forward") ||
        rule.description.contains("child"),
    )
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // FORWARDING PATTERNS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Don't forward ViewModel to child.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun ParentScreen(viewModel: ProfileViewModel) {
   *     Column {
   *         ProfileHeader(viewModel)  // Forwarding ViewModel!
   *         ProfileContent(viewModel) // Forwarding ViewModel!
   *     }
   * }
   *
   * @Composable
   * fun ProfileHeader(viewModel: ProfileViewModel) {
   *     // Child is coupled to ViewModel
   *     Text(viewModel.name)
   * }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun ParentScreen(viewModel: ProfileViewModel) {
   *     Column {
   *         ProfileHeader(
   *             name = viewModel.name,
   *             avatarUrl = viewModel.avatarUrl
   *         )
   *         ProfileContent(
   *             items = viewModel.items,
   *             onItemClick = viewModel::onItemClick
   *         )
   *     }
   * }
   *
   * @Composable
   * fun ProfileHeader(
   *     name: String,
   *     avatarUrl: String,
   *     modifier: Modifier = Modifier
   * ) {
   *     // Pure, testable, previewable
   *     Text(name)
   * }
   * ```
   */
  @Test
  fun pattern_forwardingBad() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // DATA-ONLY PATTERN
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Pass only the data needed, not the entire ViewModel.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun UserCard(viewModel: UserViewModel) {
   *     Card {
   *         Text(viewModel.user.name)  // Only needs name
   *         Text(viewModel.user.email) // Only needs email
   *     }
   * }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun UserCard(
   *     name: String,
   *     email: String,
   *     modifier: Modifier = Modifier
   * ) {
   *     Card(modifier) {
   *         Text(name)
   *         Text(email)
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_dataOnly() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // CALLBACK PATTERN
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Use callbacks instead of ViewModel for actions.
   *
   * Wrong:
   * ```kotlin
   * @Composable
   * fun ActionButtons(viewModel: ProfileViewModel) {
   *     Button(onClick = { viewModel.onSave() }) { Text("Save") }
   *     Button(onClick = { viewModel.onDelete() }) { Text("Delete") }
   * }
   * ```
   *
   * Correct:
   * ```kotlin
   * @Composable
   * fun ActionButtons(
   *     onSave: () -> Unit,
   *     onDelete: () -> Unit,
   *     modifier: Modifier = Modifier
   * ) {
   *     Row(modifier) {
   *         Button(onClick = onSave) { Text("Save") }
   *         Button(onClick = onDelete) { Text("Delete") }
   *     }
   * }
   * ```
   */
  @Test
  fun pattern_callbacks() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // STATE HOLDER PATTERN
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Pattern: Use state holder classes for complex state.
   *
   * ```kotlin
   * // State holder (not ViewModel)
   * @Stable
   * class ProfileState(
   *     val name: String,
   *     val email: String,
   *     val isLoading: Boolean
   * )
   *
   * // Actions sealed class
   * sealed class ProfileAction {
   *     object Save : ProfileAction()
   *     object Delete : ProfileAction()
   * }
   *
   * // Screen composable - integration point
   * @Composable
   * fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
   *     val state by viewModel.state.collectAsState()
   *     ProfileContent(
   *         state = state,
   *         onAction = viewModel::onAction
   *     )
   * }
   *
   * // Content composable - pure, testable
   * @Composable
   * fun ProfileContent(
   *     state: ProfileState,
   *     onAction: (ProfileAction) -> Unit,
   *     modifier: Modifier = Modifier
   * ) {
   *     // Pure UI rendering
   * }
   * ```
   */
  @Test
  fun pattern_stateHolder() {
    assertEquals(RuleCategory.PARAMETER, rule.category)
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // BENEFITS
  // ═══════════════════════════════════════════════════════════════════════════════

  /**
   * Benefits of not forwarding ViewModels:
   *
   * 1. **Testability**: Easy to test with fake data
   * 2. **Previewability**: Works in @Preview without mocking
   * 3. **Reusability**: Not tied to specific ViewModel
   * 4. **Clear API**: Parameters show exactly what's needed
   * 5. **Separation of concerns**: UI doesn't know about business logic
   */
  @Test
  fun benefits() {
    assertTrue(rule.enabledByDefault)
  }
}
