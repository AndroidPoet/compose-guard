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
package io.androidpoet.composeguard.rules.state

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.androidpoet.composeguard.quickfix.HoistStateFix
import io.androidpoet.composeguard.quickfix.SuppressComposeRuleFix
import io.androidpoet.composeguard.rules.AnalysisContext
import io.androidpoet.composeguard.rules.ComposableFunctionRule
import io.androidpoet.composeguard.rules.ComposeRuleViolation
import io.androidpoet.composeguard.rules.RuleCategory
import io.androidpoet.composeguard.rules.RuleSeverity
import io.androidpoet.composeguard.rules.isPreview
import io.androidpoet.composeguard.rules.isPrivate
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Rule: Hoist state to the lowest common ancestor when appropriate.
 *
 * State hoisting is a pattern where state is moved up to make a composable
 * stateless. Per Android's official guidance: "You should hoist UI state to
 * the lowest common ancestor between all the composables that read and write it."
 *
 * This rule uses smart heuristics to detect when state SHOULD be hoisted:
 * - State shared between multiple child composables (lowest common ancestor)
 * - State passed to child composables (parent should own it)
 * - State that could be controlled by parent for reusability
 *
 * And when state is ACCEPTABLE to keep local (reduces false positives):
 * - Screen-level composables (with ViewModel, ending in Screen/Page)
 * - Simple single-element wrappers (e.g., custom Button with press state)
 * - Private/Preview composables
 * - Internal UI state only used within this composable (expand/collapse toggles)
 * - UI element state holders (LazyListState, DrawerState, etc.)
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/state-hoisting">Android: State Hoisting</a>
 * @see <a href="https://mrmans0n.github.io/compose-rules/latest/rules/#hoist-all-the-things">Compose Rules: Hoist All The Things</a>
 */
public class HoistStateRule : ComposableFunctionRule() {

  override val id: String = "HoistState"

  override val name: String = "Consider Hoisting State"

  override val description: String = """
    State management should often be hoisted to parent composables
    to make components stateless and reusable.
  """.trimIndent()

  override val category: RuleCategory = RuleCategory.STATE

  override val severity: RuleSeverity = RuleSeverity.INFO

  override val documentationUrl: String =
    "https://mrmans0n.github.io/compose-rules/latest/rules/#hoist-all-the-things"

  // State creation patterns to detect
  private val stateCreationPatterns = setOf(
    "mutableStateOf",
    "mutableIntStateOf",
    "mutableLongStateOf",
    "mutableFloatStateOf",
    "mutableDoubleStateOf",
  )

  // UI element state holders that are typically fine to keep local
  // These are complex state objects that manage UI element behavior
  private val uiElementStateHolders = setOf(
    "rememberLazyListState",
    "rememberLazyGridState",
    "rememberScrollState",
    "rememberDrawerState",
    "rememberModalBottomSheetState",
    "rememberSheetState",
    "rememberPagerState",
    "rememberDismissState",
    "rememberSwipeableState",
    "rememberPullRefreshState",
    "rememberDatePickerState",
    "rememberTimePickerState",
  )

  // Known content-emitting composables (single UI elements)
  private val singleElementComposables = setOf(
    "Text", "Icon", "Image", "Button", "IconButton", "TextButton", "OutlinedButton",
    "TextField", "OutlinedTextField", "Checkbox", "RadioButton", "Switch", "Slider",
    "Divider", "Spacer", "CircularProgressIndicator", "LinearProgressIndicator",
  )

  // Container composables that wrap content
  private val containerComposables = setOf(
    "Box", "Column", "Row", "Surface", "Card", "Scaffold",
    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
    "ConstraintLayout", "FlowRow", "FlowColumn", "BoxWithConstraints",
    "AlertDialog", "Dialog", "ModalBottomSheet", "BottomSheet",
  )

  // Screen-level naming patterns
  private val screenSuffixes = setOf("Screen", "Page", "Route", "View", "Dialog", "Sheet")

  // ViewModel and large object types that indicate screen-level composables
  private val screenLevelTypes = setOf(
    "ViewModel",
    "UiState",
    "State",
    "Repository",
    "UseCase",
    "NavController",
    "NavHostController",
    "Navigator",
  )

  // Navigation contexts where state is idiomatic
  private val navigationContexts = setOf(
    "composable",
    "navigation",
    "NavHost",
    "NavGraphBuilder",
    "dialog",
    "bottomSheet",
  )

  override fun shouldAnalyze(function: KtNamedFunction): Boolean {
    // Only analyze public/internal composables, skip previews and private functions
    if (function.isPreview()) return false
    if (function.isPrivate()) return false
    return true
  }

  override fun doAnalyze(
    function: KtNamedFunction,
    context: AnalysisContext,
  ): List<ComposeRuleViolation> {
    val violations = mutableListOf<ComposeRuleViolation>()
    val body = function.bodyExpression ?: function.bodyBlockExpression ?: return emptyList()

    // Classify this composable
    val composableType = classifyComposableType(function)

    // Skip screen-level composables entirely - state is often appropriate there
    if (composableType == ComposableType.SCREEN) {
      return emptyList()
    }

    // Check if this is a simple single-element wrapper
    val isSimpleElement = isSimpleElementComposable(function, body)

    // Find local properties with state initialization
    val localProperties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)

    for (property in localProperties) {
      val initializer = property.initializer ?: continue
      val initializerText = initializer.text

      // Check if it's a remember { mutableStateOf() } pattern
      if (!isRememberedState(initializerText)) continue

      val propertyName = property.name ?: continue

      // Check if there's already a parameter for this state (already hoisted)
      if (hasCorrespondingParameter(function, propertyName)) continue

      // Analyze how this state is used
      val usagePattern = analyzeStateUsage(function, body, propertyName)

      // Decide whether to report based on smart heuristics
      val shouldReport = when {
        // Strong signal: state is shared between multiple children
        usagePattern == StateUsagePattern.SHARED_BETWEEN_CHILDREN -> true

        // Strong signal: state is passed to child composables
        usagePattern == StateUsagePattern.PASSED_TO_CHILDREN -> true

        // For simple elements, only report if state is externally accessible
        isSimpleElement -> false

        // For components, report if state could reasonably be hoisted
        composableType == ComposableType.COMPONENT &&
          usagePattern != StateUsagePattern.INTERNAL_ONLY -> true

        // Default: don't report to avoid noise
        else -> false
      }

      if (shouldReport) {
        // Infer state type for the quick fix
        val stateType = inferStateType(property)
        violations.add(
          createViolation(
            element = property.nameIdentifier ?: property,
            message = buildSmartMessage(propertyName, usagePattern),
            tooltip = buildSmartTooltip(propertyName, function.name ?: "Composable", usagePattern),
            quickFixes = listOf(
              HoistStateFix(propertyName, stateType),
              SuppressComposeRuleFix(id),
            ),
          ),
        )
      }
    }

    return violations
  }

  /**
   * Classifies the composable as SCREEN, COMPONENT, or SIMPLE_ELEMENT.
   */
  private fun classifyComposableType(function: KtNamedFunction): ComposableType {
    val name = function.name ?: return ComposableType.COMPONENT
    val params = function.valueParameters

    // Check for screen-level naming patterns (e.g., HomeScreen, SettingsPage)
    if (screenSuffixes.any { name.endsWith(it) }) {
      return ComposableType.SCREEN
    }

    // Check for ViewModel/large object parameters (indicates screen-level)
    val hasScreenLevelParams = params.any { param ->
      val typeName = param.typeReference?.text ?: return@any false
      screenLevelTypes.any { typeName.contains(it) }
    }
    if (hasScreenLevelParams) {
      return ComposableType.SCREEN
    }

    // Check if we're inside a navigation context
    if (isInNavigationContext(function)) {
      return ComposableType.SCREEN
    }

    return ComposableType.COMPONENT
  }

  /**
   * Checks if the composable is a simple wrapper around a single UI element.
   */
  private fun isSimpleElementComposable(function: KtNamedFunction, body: PsiElement): Boolean {
    val blockBody = body as? KtBlockExpression ?: return false

    // Get direct children call expressions (not nested in lambdas)
    val directCalls = PsiTreeUtil.getChildrenOfType(blockBody, KtCallExpression::class.java)
      ?: return false

    // Filter to composable calls (uppercase names, excluding remember/derivedStateOf)
    val composableCalls = directCalls.filter { call ->
      val callName = call.calleeExpression?.text ?: return@filter false
      callName.first().isUpperCase() &&
        callName !in setOf("LaunchedEffect", "DisposableEffect", "SideEffect") &&
        !callName.contains("remember", ignoreCase = true)
    }

    // If only one composable call and it's a single element type
    if (composableCalls.size == 1) {
      val onlyCallName = composableCalls[0].calleeExpression?.text ?: return false
      return onlyCallName in singleElementComposables
    }

    return false
  }

  /**
   * Checks if the function is defined inside a navigation context.
   */
  private fun isInNavigationContext(function: KtNamedFunction): Boolean {
    var parent = function.parent
    while (parent != null) {
      if (parent is KtCallExpression) {
        val callName = parent.calleeExpression?.text
        if (callName in navigationContexts) {
          return true
        }
      }
      parent = parent.parent
    }
    return false
  }

  /**
   * Analyzes how a state variable is used within the function.
   */
  private fun analyzeStateUsage(
    function: KtNamedFunction,
    body: PsiElement,
    stateName: String,
  ): StateUsagePattern {
    val bodyText = body.text
    val blockBody = body as? KtBlockExpression

    // Find all call expressions
    val allCalls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    // Find direct child composable calls (at top level, likely children)
    val directChildCalls = if (blockBody != null) {
      findDirectChildComposableCalls(blockBody)
    } else {
      emptyList()
    }

    // Count how many direct children reference this state
    val childrenUsingState = directChildCalls.count { call ->
      val callText = call.text
      // Check if state name appears in the call arguments
      callText.contains(stateName) ||
        callText.contains("$stateName.") ||
        callText.contains("$stateName,") ||
        callText.contains("= $stateName")
    }

    // Check if state is shared between multiple children
    if (childrenUsingState > 1) {
      return StateUsagePattern.SHARED_BETWEEN_CHILDREN
    }

    // Check if state is passed to any child composable
    if (childrenUsingState == 1) {
      return StateUsagePattern.PASSED_TO_CHILDREN
    }

    // Check if state is modified in callbacks (onXxxChange pattern)
    val hasCallback = allCalls.any { call ->
      val callText = call.text
      val hasOnCallback = callText.contains("on") && callText.contains("=")
      val modifiesState = callText.contains("$stateName =") ||
        callText.contains("$stateName++") ||
        callText.contains("$stateName--") ||
        callText.contains("$stateName +=") ||
        callText.contains("$stateName -=")
      hasOnCallback && modifiesState
    }
    if (hasCallback) {
      return StateUsagePattern.MODIFIED_IN_CALLBACK
    }

    // Check if state is used in effects (LaunchedEffect, DisposableEffect)
    val effectNames = setOf("LaunchedEffect", "DisposableEffect", "SideEffect")
    val usedInEffect = allCalls.any { call ->
      val callName = call.calleeExpression?.text ?: return@any false
      callName in effectNames && call.text.contains(stateName)
    }
    if (usedInEffect) {
      return StateUsagePattern.USED_IN_EFFECT
    }

    // Default: state is used internally only (likely UI state like expanded/collapsed)
    return StateUsagePattern.INTERNAL_ONLY
  }

  /**
   * Finds direct child composable calls (at top level of a block, not nested).
   */
  private fun findDirectChildComposableCalls(body: KtBlockExpression): List<KtCallExpression> {
    val directCalls = PsiTreeUtil.getChildrenOfType(body, KtCallExpression::class.java)
      ?: return emptyList()

    return directCalls.filter { call ->
      val callName = call.calleeExpression?.text ?: return@filter false
      // Composables start with uppercase
      callName.first().isUpperCase() &&
        // Exclude utility functions
        callName !in setOf("LaunchedEffect", "DisposableEffect", "SideEffect", "CompositionLocalProvider")
    }
  }

  /**
   * Infers the state type from a property for the quick fix.
   */
  private fun inferStateType(property: KtProperty): String {
    val initializer = property.initializer?.text ?: return "String"

    return when {
      initializer.contains("mutableIntStateOf") -> "Int"
      initializer.contains("mutableLongStateOf") -> "Long"
      initializer.contains("mutableFloatStateOf") -> "Float"
      initializer.contains("mutableDoubleStateOf") -> "Double"
      initializer.contains("mutableStateOf<") -> {
        // Extract type from generic: mutableStateOf<String>()
        val match = Regex("""mutableStateOf<(\w+)>""").find(initializer)
        match?.groupValues?.getOrNull(1) ?: "String"
      }
      initializer.contains("mutableStateOf(\"") -> "String"
      initializer.contains("mutableStateOf(true)") || initializer.contains("mutableStateOf(false)") -> "Boolean"
      initializer.contains("mutableStateOf(0)") || Regex("""mutableStateOf\(\d+\)""").containsMatchIn(initializer) -> "Int"
      initializer.contains("mutableStateOf(0L)") -> "Long"
      initializer.contains("mutableStateOf(0f)") || initializer.contains("mutableStateOf(0.0f)") -> "Float"
      initializer.contains("mutableStateOf(0.0)") -> "Double"
      initializer.contains("mutableStateOf(listOf") || initializer.contains("mutableStateOf(emptyList") -> "List<Any>"
      initializer.contains("mutableStateOf(null)") -> "Any?"
      else -> "String"
    }
  }

  private fun isRememberedState(text: String): Boolean {
    // Skip UI element state holders - these are fine to keep local per official guidance
    // Examples: rememberLazyListState(), rememberDrawerState(), etc.
    if (uiElementStateHolders.any { text.contains(it) }) {
      return false
    }

    // Check for patterns like:
    // remember { mutableStateOf(...) }
    // rememberSaveable { mutableStateOf(...) }
    val hasRemember = text.contains("remember") || text.contains("rememberSaveable")
    val hasStateCreation = stateCreationPatterns.any { text.contains(it) }
    return hasRemember && hasStateCreation
  }

  private fun hasCorrespondingParameter(function: KtNamedFunction, stateName: String): Boolean {
    val params = function.valueParameters
    val paramNames = params.mapNotNull { it.name?.lowercase() }

    // Check if there's already a parameter for this state
    val stateNameLower = stateName.lowercase()
    if (paramNames.any { it == stateNameLower || it.contains(stateNameLower) }) {
      return true
    }

    // Check if there's an onChange callback for this state
    val expectedCallback = "on${stateName.replaceFirstChar { it.uppercase() }}Change"
    if (paramNames.any { it.equals(expectedCallback, ignoreCase = true) }) {
      return true
    }

    return false
  }

  private fun buildSmartMessage(stateName: String, usagePattern: StateUsagePattern): String {
    return when (usagePattern) {
      StateUsagePattern.SHARED_BETWEEN_CHILDREN ->
        "State '$stateName' is shared between multiple children - consider hoisting"
      StateUsagePattern.PASSED_TO_CHILDREN ->
        "State '$stateName' is passed to child composable - consider hoisting"
      StateUsagePattern.MODIFIED_IN_CALLBACK ->
        "State '$stateName' is modified via callback - consider hoisting for better testability"
      StateUsagePattern.USED_IN_EFFECT ->
        "State '$stateName' is used in effect - consider hoisting if it represents business logic"
      StateUsagePattern.INTERNAL_ONLY ->
        "Consider hoisting state '$stateName' to make this composable stateless"
    }
  }

  private fun buildSmartTooltip(
    stateName: String,
    functionName: String,
    usagePattern: StateUsagePattern,
  ): String {
    val capitalizedState = stateName.replaceFirstChar { it.uppercase() }
    val reasonSection = when (usagePattern) {
      StateUsagePattern.SHARED_BETWEEN_CHILDREN -> """
        ⚠️ This state is shared between multiple child composables.
        This is a strong signal that state should be hoisted to allow
        the parent to coordinate state between children.
      """.trimIndent()
      StateUsagePattern.PASSED_TO_CHILDREN -> """
        ⚠️ This state is passed to a child composable.
        Consider whether the parent should own this state instead,
        making this component more reusable and testable.
      """.trimIndent()
      StateUsagePattern.MODIFIED_IN_CALLBACK -> """
        This state is modified through user interaction callbacks.
        Hoisting would allow the parent to handle state changes,
        improving testability and enabling state persistence.
      """.trimIndent()
      StateUsagePattern.USED_IN_EFFECT -> """
        This state is used in a side effect (LaunchedEffect/DisposableEffect).
        If it represents business logic, consider hoisting.
        If it's purely UI state (animation, visibility), it may be fine here.
      """.trimIndent()
      StateUsagePattern.INTERNAL_ONLY -> """
        This state is used internally within this composable.
        Hoisting makes composables stateless and more reusable.
      """.trimIndent()
    }

    return """
      $reasonSection

      Per Android's official guidance:
      "Hoist UI state to the lowest common ancestor between all
      composables that read and write it."

      State hoisting makes composables:
      • Easier to test (stateless)
      • More reusable (caller controls state)
      • Single source of truth (parent owns state)

      Consider refactoring to accept state as a parameter:

      ❌ Current (stateful):
      @Composable
      fun $functionName() {
          var $stateName by remember { mutableStateOf(...) }
          // uses $stateName internally
      }

      ✅ Hoisted (stateless):
      @Composable
      fun $functionName(
          $stateName: Type,
          on${capitalizedState}Change: (Type) -> Unit,
      ) {
          // uses $stateName from parameter
      }

      Learn more: https://developer.android.com/develop/ui/compose/state-hoisting
    """.trimIndent()
  }

  /**
   * Classifies a composable function's intended use.
   */
  private enum class ComposableType {
    /** Screen-level composable (e.g., HomeScreen, LoginPage) - state often appropriate */
    SCREEN,

    /** Reusable component - state should typically be hoisted */
    COMPONENT,
  }

  /**
   * Describes how a state variable is used within a composable.
   */
  private enum class StateUsagePattern {
    /** State is passed to multiple child composables - strong signal for hoisting */
    SHARED_BETWEEN_CHILDREN,

    /** State is passed to a single child composable - moderate signal */
    PASSED_TO_CHILDREN,

    /** State is modified through user interaction callbacks */
    MODIFIED_IN_CALLBACK,

    /** State is used in LaunchedEffect/DisposableEffect */
    USED_IN_EFFECT,

    /** State is only used internally (e.g., expanded/collapsed toggle) */
    INTERNAL_ONLY,
  }
}
