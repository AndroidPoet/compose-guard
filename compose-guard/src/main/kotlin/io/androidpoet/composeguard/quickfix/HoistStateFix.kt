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

    val inferredType = inferStateType(property)

    val valueParamName = stateName
    val callbackParamName = "on${stateName.replaceFirstChar { it.uppercase() }}Change"

    val newValueParam = "$valueParamName: $inferredType"
    val newCallbackParam = "$callbackParamName: ($inferredType) -> Unit"

    val existingParams = function.valueParameterList?.parameters ?: emptyList()
    val existingParamsText = existingParams.joinToString(", ") { it.text }

    val modifierIndex = existingParams.indexOfFirst { param ->
      param.typeReference?.text?.contains("Modifier") == true
    }

    val newParamsList = buildString {
      if (modifierIndex > 0) {
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
        append("\n    ")
        append(newValueParam)
        append(",\n    ")
        append(newCallbackParam)
        append(",\n")
      } else {
        append("\n    ")
        append(newValueParam)
        append(",\n    ")
        append(newCallbackParam)
        append(",\n    ")
        append(existingParamsText)
        append(",\n")
      }
    }

    val newParamListExpr = factory.createParameterList("($newParamsList)")

    function.valueParameterList?.replace(newParamListExpr)
      ?: function.addAfter(newParamListExpr, function.nameIdentifier)

    property.delete()

    updateStateUsages(function, factory, stateName, callbackParamName)
  }

  private fun inferStateType(property: KtProperty): String {
    val initializer = property.initializer?.text ?: return stateType

    return when {
      initializer.contains("mutableIntStateOf") -> "Int"
      initializer.contains("mutableLongStateOf") -> "Long"
      initializer.contains("mutableFloatStateOf") -> "Float"
      initializer.contains("mutableDoubleStateOf") -> "Double"
      initializer.contains("mutableStateOf<") -> {
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

  private fun updateStateUsages(
    function: KtNamedFunction,
    factory: KtPsiFactory,
    stateName: String,
    callbackParamName: String,
  ) {
    val body = function.bodyBlockExpression ?: return
    val bodyText = body.text

    val simpleAssignmentPattern = Regex("""\{\s*$stateName\s*=\s*it\s*\}""")
    if (simpleAssignmentPattern.containsMatchIn(bodyText)) {
      val newBodyText = simpleAssignmentPattern.replace(bodyText, callbackParamName)
      val newBody = factory.createBlock(newBodyText.removeSurrounding("{", "}").trim())
      body.replace(newBody)
    }
  }

  private fun String.capitalize(): String = replaceFirstChar { it.uppercase() }
}
