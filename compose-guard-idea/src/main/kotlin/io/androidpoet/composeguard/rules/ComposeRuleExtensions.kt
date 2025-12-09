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
package io.androidpoet.composeguard.rules

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUserType

/**
 * Extension functions for compose rule analysis.
 */

private const val COMPOSABLE_ANNOTATION = "Composable"
private const val PREVIEW_ANNOTATION = "Preview"

/**
 * Check if a function is annotated with @Composable.
 */
internal fun KtNamedFunction.isComposable(): Boolean {
  return annotationEntries.any {
    it.shortName?.asString() == COMPOSABLE_ANNOTATION
  }
}

/**
 * Check if a function is annotated with @Preview or a meta-annotation of @Preview.
 */
internal fun KtNamedFunction.isPreview(): Boolean {
  return annotationEntries.any { annotation ->
    if (annotation.shortName?.asString() == PREVIEW_ANNOTATION) {
      return@any true
    }

    try {
      val typeReference = annotation.typeReference
      val userType = typeReference?.typeElement as? KtUserType
      val referenceExpression = userType?.referenceExpression
      val annotationClass = referenceExpression?.mainReference?.resolve() as? KtClass

      annotationClass?.annotationEntries?.any {
        it.shortName?.asString() == PREVIEW_ANNOTATION
      } == true
    } catch (e: Exception) {
      false
    }
  }
}

/**
 * Check if a function has public visibility.
 */
internal fun KtNamedFunction.isPublic(): Boolean {
  return !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD) &&
    !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD) &&
    !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PROTECTED_KEYWORD)
}

/**
 * Check if a function has internal visibility.
 */
internal fun KtNamedFunction.isInternal(): Boolean {
  return hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD)
}

/**
 * Check if a function has private visibility.
 */
internal fun KtNamedFunction.isPrivate(): Boolean {
  return hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD)
}

/**
 * Check if a function returns Unit (or has no explicit return type).
 */
internal fun KtNamedFunction.returnsUnit(): Boolean {
  val returnType = typeReference?.text
  return returnType == null || returnType == "Unit"
}

/**
 * Get the modifier parameter if it exists.
 */
internal fun KtNamedFunction.getModifierParameter(): KtParameter? {
  return valueParameters.find { param ->
    val typeName = param.typeReference?.text ?: return@find false
    typeName == "Modifier" || typeName.endsWith(".Modifier")
  }
}

/**
 * Check if a function has a modifier parameter.
 */
internal fun KtNamedFunction.hasModifierParameter(): Boolean {
  return getModifierParameter() != null
}

/**
 * Get all parameters that are lambda types (potential content slots).
 */
internal fun KtNamedFunction.getLambdaParameters(): List<KtParameter> {
  return valueParameters.filter { param ->
    val typeText = param.typeReference?.text ?: return@filter false
    typeText.contains("->") || typeText.startsWith("@Composable")
  }
}

/**
 * Get the last parameter (potential trailing lambda).
 */
internal fun KtNamedFunction.getLastParameter(): KtParameter? {
  return valueParameters.lastOrNull()
}

/**
 * Check if a parameter is a composable lambda.
 */
internal fun KtParameter.isComposableLambda(): Boolean {
  val typeText = typeReference?.text ?: return false
  return typeText.contains("@Composable") && typeText.contains("->")
}

/**
 * Check if a parameter has a default value.
 */
internal fun KtParameter.hasDefaultValue(): Boolean {
  return defaultValue != null
}

/**
 * Check if a property is a CompositionLocal.
 */
internal fun KtProperty.isCompositionLocal(): Boolean {
  val initializer = initializer ?: return false
  val callText = initializer.text
  return callText.contains("compositionLocalOf") ||
    callText.contains("staticCompositionLocalOf")
}

/**
 * Find all call expressions within a function body.
 */
internal fun KtNamedFunction.findCallExpressions(): List<KtCallExpression> {
  val body = bodyExpression ?: bodyBlockExpression ?: return emptyList()
  return PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java).toList()
}

/**
 * Find all dot-qualified expressions within a function body.
 */
internal fun KtNamedFunction.findDotQualifiedExpressions(): List<KtDotQualifiedExpression> {
  val body = bodyExpression ?: bodyBlockExpression ?: return emptyList()
  return PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java).toList()
}

/**
 * Get the function name or a fallback if null.
 */
internal fun KtNamedFunction.getNameOrDefault(): String {
  return name ?: "<anonymous>"
}

/**
 * Check if an annotation matches a specific name.
 */
internal fun KtAnnotationEntry.matchesName(vararg names: String): Boolean {
  val shortName = shortName?.asString() ?: return false
  return names.any { it == shortName }
}

/**
 * Get the parent function of an element if it exists.
 */
internal fun PsiElement.getParentFunction(): KtNamedFunction? {
  return PsiTreeUtil.getParentOfType(this, KtNamedFunction::class.java)
}

/**
 * Check if a type string represents a mutable type.
 */
internal fun String.isMutableType(): Boolean {
  return startsWith("Mutable") ||
    contains("MutableList") ||
    contains("MutableSet") ||
    contains("MutableMap") ||
    contains("MutableState") ||
    contains("ArrayList") ||
    contains("HashMap") ||
    contains("HashSet")
}

/**
 * Check if a type string represents a standard collection interface.
 */
internal fun String.isStandardCollection(): Boolean {
  val cleanType = this.substringBefore("<").trim()
  return cleanType in setOf("List", "Set", "Map", "Collection", "Iterable")
}

/**
 * Convert first character to uppercase for PascalCase.
 */
internal fun String.toPascalCase(): String {
  return if (isNotEmpty()) {
    this[0].uppercaseChar() + substring(1)
  } else {
    this
  }
}

/**
 * Convert first character to lowercase for camelCase.
 */
internal fun String.toCamelCase(): String {
  return if (isNotEmpty()) {
    this[0].lowercaseChar() + substring(1)
  } else {
    this
  }
}
