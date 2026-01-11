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

@Composable
fun ParameterRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test parameter conventions", modifier = Modifier.padding(bottom = 8.dp))
    GoodParamOrder(title = "Hello")
  }
}

@Composable
fun BadParamOrder(enabled: Boolean = true, modifier: Modifier = Modifier, title: String) {
  Column(modifier = modifier) { Text(title) }
}

@Composable
fun GoodParamOrder(title: String, modifier: Modifier = Modifier, enabled: Boolean = true) {
  Column(modifier = modifier) { Text(title) }
}

@Composable
fun NotTrailing(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

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

@Composable
fun MutableListParam(items: MutableList<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    items.forEach { Text(it) }
  }
}

@Composable
fun ImmutableListParam(items: List<String>, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    items.forEach { Text(it) }
  }
}

@Composable
fun ImplicitDep(viewModel: SampleViewModel = viewModel<SampleViewModel>()) {
  Text("Data")
}

@Composable
fun ExplicitDep(viewModel: SampleViewModel, modifier: Modifier = Modifier) {
  Text("Data", modifier = modifier)
}

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

class SampleViewModel : ViewModel()

@Composable
fun RequiredAfterOptional(
  enabled: Boolean = true,
  title: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) { Text(title) }
}

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

@Composable
fun ContentNotTrailing(
  content: @Composable () -> Unit,
  title: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(title)
    content()
  }
}

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
}

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
