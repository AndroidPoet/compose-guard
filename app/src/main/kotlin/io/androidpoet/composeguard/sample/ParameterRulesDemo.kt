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
@file:Suppress("unused", "UNUSED_PARAMETER")

package io.androidpoet.composeguard.sample

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// ═══════════════════════════════════════════════════════════════════════════════
// PARAMETER RULES DEMO
// ═══════════════════════════════════════════════════════════════════════════════
//
// Official Compose API Parameter Order:
// ┌────────────────────────────────────────────────────────────────────────────┐
// │ 1. Required parameters (no defaults) - data first, then callbacks          │
// │ 2. modifier: Modifier = Modifier (FIRST optional parameter)                │
// │ 3. Other optional parameters (with defaults)                               │
// │ 4. Trailing @Composable lambda (if any)                                    │
// └────────────────────────────────────────────────────────────────────────────┘
//
// From official guidelines: "Since the modifier is recommended for any component
// and is used often, placing it first ensures that it can be set without a named
// parameter and provides a consistent place for this parameter in any component."
//
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ParameterRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test parameter conventions", modifier = Modifier.padding(bottom = 8.dp))
    GoodParamOrder(title = "Hello")
  }
}

// -------------------------------------------------------------------------------
// 20. ParameterOrdering - Required first, optional last
// -------------------------------------------------------------------------------

/** BAD: Optional parameters before required */
@Composable
fun BadParamOrder(enabled: Boolean = true, modifier: Modifier = Modifier, title: String) {
  Column(modifier = modifier) { Text(title) }
}

/** GOOD: Required first, modifier second, other optionals last */
@Composable
fun GoodParamOrder(title: String, modifier: Modifier = Modifier, enabled: Boolean = true) {
  Column(modifier = modifier) { Text(title) }
}

// -------------------------------------------------------------------------------
// 21. TrailingLambda - Content should be last
// -------------------------------------------------------------------------------

/** BAD: Content lambda is not the last parameter */
@Composable
fun NotTrailing(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

/** GOOD: Content lambda is the last parameter */
@Composable
fun Trailing(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

// -------------------------------------------------------------------------------
// 22. MutableParameter - Use immutable collections
// -------------------------------------------------------------------------------

/** BAD: Mutable collection as parameter */
@Composable
fun MutableListParam(items: MutableList<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    items.forEach { Text(it) }
  }
}

/** GOOD: Immutable collection as parameter */
@Composable
fun ImmutableListParam(items: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    items.forEach { Text(it) }
  }
}

// -------------------------------------------------------------------------------
// 23. ExplicitDependencies - Make dependencies explicit
// -------------------------------------------------------------------------------

/** BAD: ViewModel obtained inside composable (implicit dependency) */
@Composable
fun ImplicitDep(viewModel: SampleViewModel = viewModel<SampleViewModel>()) {
  Text("Data")
}

/** GOOD: ViewModel passed as parameter (explicit dependency) */
@Composable
fun ExplicitDep(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
  Text("Data", modifier = modifier)
}

// -------------------------------------------------------------------------------
// 24. ViewModelForwarding - Don't forward ViewModel
// -------------------------------------------------------------------------------

/** BAD: Forwarding ViewModel to child composable */
@Composable
fun ParentForwards(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    ChildVM(viewModel = viewModel)
  }
}

@Composable
fun ChildVM(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
  Text("Forwarded", modifier = modifier)
}

/** GOOD: Pass data, not ViewModel */
@Composable
fun ParentData(data: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    ChildData(data = data)
  }
}

@Composable
fun ChildData(data: String, modifier: Modifier = Modifier) {
  Text(data, modifier = modifier)
}

// Helper class
class SampleViewModel : ViewModel()

// ===============================================================================
// PARAMETER REORDERING SAMPLES - Click "Reorder parameters" to fix in one click!
// ===============================================================================

// -------------------------------------------------------------------------------
// VIOLATION 1: Required parameter after optional (basic)
// Expected order: title → enabled → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Required 'title' comes after optional 'enabled' */
@Composable
fun RequiredAfterOptional(
  enabled: Boolean = true, // ❌ Optional should come after required
  title: String, // Required - should be first!
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) { Text(title) }
}

// -------------------------------------------------------------------------------
// VIOLATION 2: Optional parameters before modifier
// Expected order: title → modifier → enabled → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Optional parameter 'enabled' comes BEFORE modifier */
@Composable
fun OptionalBeforeModifier(
  title: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 3: Content lambda not trailing
// Expected order: title → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Content lambda should be at the end for trailing lambda syntax */
@Composable
fun ContentNotTrailing(
  content: @Composable () -> Unit, // ❌ Should be last!
  title: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 4: State and callback not paired together
// Expected order: value → onValueChange → label → onFocusChange → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: State (value) and callback (onValueChange) should be adjacent */
@Composable
fun StateCallbackNotPaired(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "",
  onFocusChange: (Boolean) -> Unit = {},
) {
  Column(modifier = modifier) {
    Text(label)
    Text(value)
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 5: Multiple violations - real-world Button-like component
// Expected: onClick → shape → enabled → colors → elevation → border → contentPadding → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Multiple parameter ordering violations */
@Composable
fun CustomButton(
  onClick: () -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(),
  elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
  border: BorderStroke? = null,
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  content: @Composable RowScope.() -> Unit,
) {
  // Implementation
}

// -------------------------------------------------------------------------------
// VIOLATION 6: TextField-like with state+callback separation
// Expected: value → onValueChange → label → placeholder → enabled → singleLine → maxLines → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Separates value from onValueChange, modifier misplaced */
@Composable
fun CustomTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String = "",
  placeholder: String = "",
  enabled: Boolean = true,
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(label)
    Text(value)
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 7: Checkbox-like component
// Expected: checked → onCheckedChange → enabled → colors → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Checked state separated from its callback */
@Composable
fun CustomCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
  Column(modifier = modifier) { Text("Checked: $checked") }
}

// -------------------------------------------------------------------------------
// VIOLATION 8: Scaffold-like with multiple slots
// Click "Reorder parameters" to fix: title → modifier → navigationIcon → actions → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Modifier should be first optional, content should be trailing */
@Composable
fun CustomScaffold(
  title: String,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable () -> Unit = {},
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    navigationIcon()
    actions()
    content()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 9: Card-like with mixed issues
// Expected: onClick → elevation → shape → border → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Required callback after optionals, content not trailing */
@Composable
fun CustomCard(
  shape: Shape,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  elevation: ButtonElevation? = null,
  border: BorderStroke? = null,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) { content() }
}

// -------------------------------------------------------------------------------
// VIOLATION 10: Dialog-like with confirmButton and dismissButton slots
// Expected: onDismissRequest → confirmButton → dismissButton → title → text → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Required callback mixed with optional slots incorrectly */
@Composable
fun CustomDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  title: String = "",
  text: String = "",
  dismissButton: @Composable (() -> Unit)? = null,
  confirmButton: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    Text(title)
    Text(text)
    dismissButton?.invoke()
    confirmButton()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 11: Switch-like component
// Expected: checked → onCheckedChange → thumbContent → enabled → colors → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: State/callback separation and modifier placement */
@Composable
fun CustomSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(),
  thumbContent: @Composable (() -> Unit)? = null,
) {
  Column(modifier = modifier) { Text("Switch: $checked") }
}

// -------------------------------------------------------------------------------
// VIOLATION 12: Slider-like component
// Expected: value → onValueChange → valueRange → steps → enabled → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: State separated from callback, required after optional */
@Composable
fun CustomSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  enabled: Boolean = true,
) {
  Column(modifier = modifier) { Text("Value: $value") }
}

// -------------------------------------------------------------------------------
// VIOLATION 13: TabRow-like component
// Expected: selectedTabIndex → tabs → divider → indicator → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Content slots not properly ordered, modifier misplaced */
@Composable
fun CustomTabRow(
  selectedTabIndex: Int,
  modifier: Modifier = Modifier,
  indicator: @Composable () -> Unit = {},
  divider: @Composable () -> Unit = {},
  tabs: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    indicator()
    divider()
    tabs()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 14: BottomSheet-like with sheetContent slot
// Expected: onDismissRequest → sheetState → sheetContent → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: All ordering rules violated */
@Composable
fun CustomBottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: Int = 0,
  sheetContent: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier) {
    sheetContent()
    content()
  }
}

// -------------------------------------------------------------------------------
// VIOLATION 15: Complex form field with validation (OFFICIAL RULE DEMO)
// Expected: label → value → onValueChange → modifier → isError → errorMessage → enabled → placeholder
// -------------------------------------------------------------------------------

/**
 * ❌ BAD: Multiple violations of official Compose API parameter ordering:
 *
 * 1. Optional params (errorMessage, enabled) appear BEFORE modifier
 * 2. Required params (label, value, onValueChange) appear AFTER optional params
 *
 * From official Compose Component API Guidelines:
 * "Since the modifier is recommended for any component and is used often,
 * placing it first ensures that it can be set without a named parameter
 * and provides a consistent place for this parameter in any component."
 *
 * ✅ CORRECT ORDER:
 * @Composable
 * fun FormField(
 *     label: String,                      // 1. Required params first
 *     value: String,                      // 1. Required params
 *     onValueChange: (String) -> Unit,    // 1. Required callback
 *     modifier: Modifier = Modifier,      // 2. Modifier (FIRST optional)
 *     isError: Boolean = false,           // 3. Other optional params
 *     errorMessage: String = "",
 *     enabled: Boolean = true,
 *     placeholder: String = ""
 * )
 */
@Composable
fun FormField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  errorMessage: String = "",
  enabled: Boolean = true,
  isError: Boolean = false,
  placeholder: String = "",
) {
  Column(modifier = modifier) {
    Text(label)
    Text(value)
    if (isError) Text(errorMessage)
  }
}

// -------------------------------------------------------------------------------
// CORRECT EXAMPLE: FormField with proper parameter ordering
// -------------------------------------------------------------------------------

/**
 * ✅ CORRECT: Follows official Compose API parameter ordering:
 *
 * 1. Required parameters (no defaults) - data first, then callbacks
 * 2. Modifier (FIRST optional parameter)
 * 3. Other optional parameters with defaults
 */
@Composable
fun FormFieldCorrect(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  errorMessage: String = "",
  enabled: Boolean = true,
  placeholder: String = "",
) {
  Column(modifier = modifier) {
    Text(label)
    Text(value)
    if (isError) Text(errorMessage)
  }
}
