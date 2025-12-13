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
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that hoists state by adding value + callback parameters to the composable function.
 *
 * Transforms:
 * ```kotlin
 * @Composable
 * fun MyComponent() {
 *     var name by remember { mutableStateOf("") }
 *     TextField(value = name, onValueChange = { name = it })
 * }
 * ```
 *
 * To:
 * ```kotlin
 * @Composable
 * fun MyComponent(
 *     name: String,
 *     onNameChange: (String) -> Unit,
 * ) {
 *     TextField(value = name, onValueChange = onNameChange)
 * }
 * ```
 *
 * Per Android's official guidance: "State hoisting is a pattern of moving state to
 * a composable's caller to make a composable stateless."
 *
 * @see <a href="https://developer.android.com/develop/ui/compose/state#state-hoisting">State Hoisting</a>
 */
public class HoistStateFix(
  private val stateName: String,
  private val stateType: String = "String",
) : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): String = "Hoist state"

  override fun getName(): String = "Hoist '$stateName' (add $stateName + on${stateName.capitalize()}Change parameters)"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val property = element as? KtProperty
      ?: element.parent as? KtProperty
      ?: return

    val function = PsiTreeUtil.getParentOfType(property, KtNamedFunction::class.java) ?: return
    val factory = KtPsiFactory(project)

    // Determine the state type from the property
    val inferredType = inferStateType(property)

    // Create the new parameter names
    val valueParamName = stateName
    val callbackParamName = "on${stateName.replaceFirstChar { it.uppercase() }}Change"

    // Build the new parameters
    val newValueParam = "$valueParamName: $inferredType"
    val newCallbackParam = "$callbackParamName: ($inferredType) -> Unit"

    // Get existing parameters
    val existingParams = function.valueParameterList?.parameters ?: emptyList()
    val existingParamsText = existingParams.joinToString(", ") { it.text }

    // Find where to insert new parameters (before Modifier if exists, otherwise at start)
    val modifierIndex = existingParams.indexOfFirst { param ->
      param.typeReference?.text?.contains("Modifier") == true
    }

    val newParamsList = buildString {
      if (modifierIndex > 0) {
        // Insert before modifier
        val beforeModifier = existingParams.subList(0, modifierIndex).joinToString(",\n    ") { it.text }
        val modifierAndAfter = existingParams.subList(modifierIndex, existingParams.size).joinToString(",\n    ") { it.text }
        append(beforeModifier)
        if (beforeModifier.isNotEmpty()) append(",\n    ")
        append(newValueParam)
        append(",\n    ")
        append(newCallbackParam)
        append(",\n    ")
        append(modifierAndAfter)
      } else if (existingParams.isEmpty()) {
        // No existing params
        append("\n    ")
        append(newValueParam)
        append(",\n    ")
        append(newCallbackParam)
        append(",\n")
      } else {
        // Add at the beginning (before existing params)
        append("\n    ")
        append(newValueParam)
        append(",\n    ")
        append(newCallbackParam)
        append(",\n    ")
        append(existingParamsText)
        append(",\n")
      }
    }

    // Create new parameter list
    val newParamListExpr = factory.createParameterList("($newParamsList)")

    // Replace the parameter list
    function.valueParameterList?.replace(newParamListExpr)
      ?: function.addAfter(newParamListExpr, function.nameIdentifier)

    // Remove the local state declaration
    property.delete()

    // Update usages in the function body to use the callback
    // This is a simplified approach - in practice, we'd need more sophisticated refactoring
    updateStateUsages(function, factory, stateName, callbackParamName)
  }

  /**
   * Infers the state type from the property initializer.
   */
  private fun inferStateType(property: KtProperty): String {
    val initializer = property.initializer?.text ?: return stateType

    // Try to extract type from mutableStateOf<Type>() or mutableStateOf(defaultValue)
    return when {
      initializer.contains("mutableIntStateOf") -> "Int"
      initializer.contains("mutableLongStateOf") -> "Long"
      initializer.contains("mutableFloatStateOf") -> "Float"
      initializer.contains("mutableDoubleStateOf") -> "Double"
      initializer.contains("mutableStateOf<") -> {
        // Extract type from generic: mutableStateOf<String>()
        val match = Regex("""mutableStateOf<(\w+)>""").find(initializer)
        match?.groupValues?.getOrNull(1) ?: stateType
      }
      initializer.contains("mutableStateOf(\"") -> "String"
      initializer.contains("mutableStateOf(true)") || initializer.contains("mutableStateOf(false)") -> "Boolean"
      initializer.contains("mutableStateOf(0)") || initializer.contains("mutableStateOf(1)") -> "Int"
      initializer.contains("mutableStateOf(0L)") -> "Long"
      initializer.contains("mutableStateOf(0f)") || initializer.contains("mutableStateOf(0.0f)") -> "Float"
      initializer.contains("mutableStateOf(0.0)") -> "Double"
      initializer.contains("mutableStateOf(listOf") || initializer.contains("mutableStateOf(emptyList") -> "List<Any>"
      initializer.contains("mutableStateOf(null)") -> "Any?"
      else -> stateType
    }
  }

  /**
   * Updates state assignment usages to use the callback parameter.
   *
   * For example, transforms:
   *   onValueChange = { name = it }
   * To:
   *   onValueChange = onNameChange
   */
  private fun updateStateUsages(
    function: KtNamedFunction,
    factory: KtPsiFactory,
    stateName: String,
    callbackParamName: String,
  ) {
    val body = function.bodyBlockExpression ?: return
    val bodyText = body.text

    // Simple replacement for common patterns like { stateName = it }
    // This handles the most common TextField pattern
    val simpleAssignmentPattern = Regex("""\{\s*$stateName\s*=\s*it\s*\}""")
    if (simpleAssignmentPattern.containsMatchIn(bodyText)) {
      val newBodyText = simpleAssignmentPattern.replace(bodyText, callbackParamName)
      val newBody = factory.createBlock(newBodyText.removeSurrounding("{", "}").trim())
      body.replace(newBody)
    }
  }

  private fun String.capitalize(): String = replaceFirstChar { it.uppercase() }
}
