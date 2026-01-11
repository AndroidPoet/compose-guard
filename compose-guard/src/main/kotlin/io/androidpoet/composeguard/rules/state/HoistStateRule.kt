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

  private val stateCreationPatterns = setOf(
    "mutableStateOf",
    "mutableIntStateOf",
    "mutableLongStateOf",
    "mutableFloatStateOf",
    "mutableDoubleStateOf",
  )

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

  private val singleElementComposables = setOf(
    "Text", "Icon", "Image", "Button", "IconButton", "TextButton", "OutlinedButton",
    "TextField", "OutlinedTextField", "Checkbox", "RadioButton", "Switch", "Slider",
    "Divider", "Spacer", "CircularProgressIndicator", "LinearProgressIndicator",
  )

  private val containerComposables = setOf(
    "Box", "Column", "Row", "Surface", "Card", "Scaffold",
    "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
    "ConstraintLayout", "FlowRow", "FlowColumn", "BoxWithConstraints",
    "AlertDialog", "Dialog", "ModalBottomSheet", "BottomSheet",
  )

  private val screenSuffixes = setOf("Screen", "Page", "Route", "View", "Dialog", "Sheet")

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

  private val navigationContexts = setOf(
    "composable",
    "navigation",
    "NavHost",
    "NavGraphBuilder",
    "dialog",
    "bottomSheet",
  )

  override fun shouldAnalyze(function: KtNamedFunction): Boolean {
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

    val composableType = classifyComposableType(function)

    if (composableType == ComposableType.SCREEN) {
      return emptyList()
    }

    val isSimpleElement = isSimpleElementComposable(function, body)

    val localProperties = PsiTreeUtil.findChildrenOfType(body, KtProperty::class.java)

    for (property in localProperties) {
      val initializer = property.initializer ?: continue
      val initializerText = initializer.text

      if (!isRememberedState(initializerText)) continue

      val propertyName = property.name ?: continue

      if (hasCorrespondingParameter(function, propertyName)) continue

      val usagePattern = analyzeStateUsage(function, body, propertyName)

      val shouldReport = when {
        usagePattern == StateUsagePattern.SHARED_BETWEEN_CHILDREN -> true

        usagePattern == StateUsagePattern.PASSED_TO_CHILDREN -> true

        isSimpleElement -> false

        composableType == ComposableType.COMPONENT &&
          usagePattern != StateUsagePattern.INTERNAL_ONLY -> true

        else -> false
      }

      if (shouldReport) {
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

  private fun classifyComposableType(function: KtNamedFunction): ComposableType {
    val name = function.name ?: return ComposableType.COMPONENT
    val params = function.valueParameters

    if (screenSuffixes.any { name.endsWith(it) }) {
      return ComposableType.SCREEN
    }

    val hasScreenLevelParams = params.any { param ->
      val typeName = param.typeReference?.text ?: return@any false
      screenLevelTypes.any { typeName.contains(it) }
    }
    if (hasScreenLevelParams) {
      return ComposableType.SCREEN
    }

    if (isInNavigationContext(function)) {
      return ComposableType.SCREEN
    }

    return ComposableType.COMPONENT
  }

  private fun isSimpleElementComposable(function: KtNamedFunction, body: PsiElement): Boolean {
    val blockBody = body as? KtBlockExpression ?: return false

    val directCalls = PsiTreeUtil.getChildrenOfType(blockBody, KtCallExpression::class.java)
      ?: return false

    val composableCalls = directCalls.filter { call ->
      val callName = call.calleeExpression?.text ?: return@filter false
      callName.first().isUpperCase() &&
        callName !in setOf("LaunchedEffect", "DisposableEffect", "SideEffect") &&
        !callName.contains("remember", ignoreCase = true)
    }

    if (composableCalls.size == 1) {
      val onlyCallName = composableCalls[0].calleeExpression?.text ?: return false
      return onlyCallName in singleElementComposables
    }

    return false
  }

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

  private fun analyzeStateUsage(
    function: KtNamedFunction,
    body: PsiElement,
    stateName: String,
  ): StateUsagePattern {
    val bodyText = body.text
    val blockBody = body as? KtBlockExpression

    val allCalls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

    val directChildCalls = if (blockBody != null) {
      findDirectChildComposableCalls(blockBody)
    } else {
      emptyList()
    }

    val childrenUsingState = directChildCalls.count { call ->
      val callText = call.text
      callText.contains(stateName) ||
        callText.contains("$stateName.") ||
        callText.contains("$stateName,") ||
        callText.contains("= $stateName")
    }

    if (childrenUsingState > 1) {
      return StateUsagePattern.SHARED_BETWEEN_CHILDREN
    }

    if (childrenUsingState == 1) {
      return StateUsagePattern.PASSED_TO_CHILDREN
    }

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

    val effectNames = setOf("LaunchedEffect", "DisposableEffect", "SideEffect")
    val usedInEffect = allCalls.any { call ->
      val callName = call.calleeExpression?.text ?: return@any false
      callName in effectNames && call.text.contains(stateName)
    }
    if (usedInEffect) {
      return StateUsagePattern.USED_IN_EFFECT
    }

    return StateUsagePattern.INTERNAL_ONLY
  }

  private fun findDirectChildComposableCalls(body: KtBlockExpression): List<KtCallExpression> {
    val directCalls = PsiTreeUtil.getChildrenOfType(body, KtCallExpression::class.java)
      ?: return emptyList()

    return directCalls.filter { call ->
      val callName = call.calleeExpression?.text ?: return@filter false
      callName.first().isUpperCase() &&
        callName !in setOf("LaunchedEffect", "DisposableEffect", "SideEffect", "CompositionLocalProvider")
    }
  }

  private fun inferStateType(property: KtProperty): String {
    val initializer = property.initializer?.text ?: return "String"

    return when {
      initializer.contains("mutableIntStateOf") -> "Int"
      initializer.contains("mutableLongStateOf") -> "Long"
      initializer.contains("mutableFloatStateOf") -> "Float"
      initializer.contains("mutableDoubleStateOf") -> "Double"
      initializer.contains("mutableStateOf<") -> {
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
    if (uiElementStateHolders.any { text.contains(it) }) {
      return false
    }

    val hasRemember = text.contains("remember") || text.contains("rememberSaveable")
    val hasStateCreation = stateCreationPatterns.any { text.contains(it) }
    return hasRemember && hasStateCreation
  }

  private fun hasCorrespondingParameter(function: KtNamedFunction, stateName: String): Boolean {
    val params = function.valueParameters
    val paramNames = params.mapNotNull { it.name?.lowercase() }

    val stateNameLower = stateName.lowercase()
    if (paramNames.any { it == stateNameLower || it.contains(stateNameLower) }) {
      return true
    }

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
      }

      ✅ Hoisted (stateless):
      @Composable
      fun $functionName(
          $stateName: Type,
          on${capitalizedState}Change: (Type) -> Unit,
      ) {
      }

      Learn more: https://developer.android.com/develop/ui/compose/state-hoisting
    """.trimIndent()
  }

  private enum class ComposableType {
    SCREEN,
    COMPONENT,
  }

  private enum class StateUsagePattern {
    SHARED_BETWEEN_CHILDREN,
    PASSED_TO_CHILDREN,
    MODIFIED_IN_CALLBACK,
    USED_IN_EFFECT,
    INTERNAL_ONLY,
  }
}
