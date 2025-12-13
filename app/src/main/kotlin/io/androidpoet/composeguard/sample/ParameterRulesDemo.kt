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
  content: @Composable () -> Unit
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
    enabled: Boolean = true,        // ❌ Optional should come after required
    title: String,                  // Required - should be first!
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) { Text(title) }
}

// -------------------------------------------------------------------------------
// VIOLATION 2: Modifier not in the correct position
// Expected order: title → enabled → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Modifier is not the last non-lambda parameter */
@Composable
fun ModifierNotLast(
    title: String,
    modifier: Modifier = Modifier,  // ❌ Modifier should come after 'enabled'
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
    content: @Composable () -> Unit,  // ❌ Should be last!
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
    label: String = "",                       // ❌ Separates value from onValueChange!
    onValueChange: (String) -> Unit,          // Should immediately follow 'value'
    onFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,                               // ❌ Modifier should be last non-lambda
    content: @Composable RowScope.() -> Unit,                    // ❌ Content should be trailing
    onClick: () -> Unit,                                         // Required - should be first!
    shape: Shape,                                                // Required - should be second
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
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
  modifier: Modifier = Modifier
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
    enabled: Boolean = true,              // ❌ Optional before required!
    checked: Boolean,                     // Required state - should be first
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onCheckedChange: (Boolean) -> Unit,   // ❌ Should immediately follow 'checked'
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) { Text("Checked: $checked") }
}

// -------------------------------------------------------------------------------
// VIOLATION 8: Scaffold-like with multiple slots
// Expected: title → navigationIcon → actions → modifier → content
// -------------------------------------------------------------------------------

/** ❌ BAD: Multiple slots and modifier ordering issues */
@Composable
fun CustomScaffold(
    modifier: Modifier = Modifier,                     // ❌ Should be after optional slots
    content: @Composable () -> Unit,                   // ❌ Primary content should be last
    title: String,                                     // Required - should be first!
    navigationIcon: @Composable () -> Unit = {},       // Optional slot
    actions: @Composable () -> Unit = {},              // Optional slot
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
    content: @Composable () -> Unit,                   // ❌ Should be trailing
    elevation: ButtonElevation? = null,
    shape: Shape,                                      // ❌ Required after optional
    onClick: () -> Unit,                               // ❌ Required should be first
    border: BorderStroke? = null,
    modifier: Modifier = Modifier,
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
    title: String = "",
    modifier: Modifier = Modifier,                      // ❌ Should be last
    confirmButton: @Composable () -> Unit,              // Required slot
    text: String = "",
    onDismissRequest: () -> Unit,                       // ❌ Required should be first
    dismissButton: @Composable (() -> Unit)? = null,    // Optional slot
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
    modifier: Modifier = Modifier,                      // ❌ Not last non-lambda
    checked: Boolean,
    enabled: Boolean = true,
    thumbContent: @Composable (() -> Unit)? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onCheckedChange: (Boolean) -> Unit,                 // ❌ Should follow 'checked'
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
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    value: Float,                                        // ❌ Required after optional
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,                      // ❌ Should follow 'value'
    modifier: Modifier = Modifier,
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
  tabs: @Composable () -> Unit
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
  sheetState: Int = 0,
  modifier: Modifier = Modifier,
  sheetContent: @Composable () -> Unit,
  content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        sheetContent()
        content()
    }
}

// -------------------------------------------------------------------------------
// VIOLATION 15: Complex form field with validation
// Expected: value → onValueChange → label → placeholder → isError → errorMessage → enabled → modifier
// -------------------------------------------------------------------------------

/** ❌ BAD: Multiple required params mixed with optionals incorrectly */
@Composable
fun FormField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  isError: Boolean = false,
  errorMessage: String = "",
  enabled: Boolean = true,
  placeholder: String = "",
  modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label)
        Text(value)
        if (isError) Text(errorMessage)
    }
}

