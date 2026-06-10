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
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.parents

private const val COMPOSABLE_ANNOTATION = "Composable"
private const val PREVIEW_ANNOTATION = "Preview"

private val transparentComposableWrappers = setOf(
  "CompositionLocalProvider",
  "key",
)

private val sideEffectComposables = setOf(
  "LaunchedEffect",
  "DisposableEffect",
  "SideEffect",
  "remember",
  "rememberSaveable",
  "derivedStateOf",
)

internal fun KtNamedFunction.isComposable(): Boolean {
  return annotationEntries.any {
    it.shortName?.asString() == COMPOSABLE_ANNOTATION
  }
}

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

internal fun KtNamedFunction.isPublic(): Boolean {
  return !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD) &&
    !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD) &&
    !hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PROTECTED_KEYWORD)
}

internal fun KtNamedFunction.isInternal(): Boolean {
  return hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD)
}

internal fun KtNamedFunction.isPrivate(): Boolean {
  return hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD)
}

internal fun KtNamedFunction.returnsUnit(): Boolean {
  val returnType = typeReference?.text
  return returnType == null || returnType == "Unit"
}

internal fun KtNamedFunction.getModifierParameter(): KtParameter? {
  return valueParameters.find { param ->
    val typeName = param.typeReference?.text ?: return@find false
    typeName == "Modifier" || typeName.endsWith(".Modifier")
  }
}

internal fun KtNamedFunction.hasModifierParameter(): Boolean {
  return getModifierParameter() != null
}

internal fun KtNamedFunction.getLambdaParameters(): List<KtParameter> {
  return valueParameters.filter { param ->
    val typeText = param.typeReference?.text ?: return@filter false
    typeText.contains("->") || typeText.startsWith("@Composable")
  }
}

internal fun KtNamedFunction.getLastParameter(): KtParameter? {
  return valueParameters.lastOrNull()
}

internal fun KtParameter.isComposableLambda(): Boolean {
  val typeText = typeReference?.text ?: return false
  return typeText.contains("@Composable") && typeText.contains("->")
}

internal fun KtProperty.isCompositionLocal(): Boolean {
  val initializer = initializer ?: return false
  val callText = initializer.text
  return callText.contains("compositionLocalOf") ||
    callText.contains("staticCompositionLocalOf")
}

internal fun KtNamedFunction.findCallExpressions(): List<KtCallExpression> {
  val body = bodyExpression ?: bodyBlockExpression ?: return emptyList()
  return PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java).toList()
}

internal fun KtNamedFunction.findDotQualifiedExpressions(): List<KtDotQualifiedExpression> {
  val body = bodyExpression ?: bodyBlockExpression ?: return emptyList()
  return PsiTreeUtil.findChildrenOfType(body, KtDotQualifiedExpression::class.java).toList()
}

internal fun KtNamedFunction.emitsComposableContent(): Boolean {
  val body = bodyExpression ?: bodyBlockExpression ?: return false
  val calls = PsiTreeUtil.findChildrenOfType(body, KtCallExpression::class.java)

  for (call in calls) {
    val callName = call.calleeExpression?.text ?: continue

    if (callName in transparentComposableWrappers || callName in sideEffectComposables) {
      continue
    }

    if (callName.firstOrNull()?.isUpperCase() == true && call.isContentEmittingStatement()) {
      return true
    }

    if (call.parents.any { parent ->
        parent is KtLambdaArgument &&
          (parent.parent as? KtCallExpression)?.calleeExpression?.text in transparentComposableWrappers
      }
    ) {
      continue
    }
  }

  return false
}

/**
 * A Composable that emits UI is invoked as a *statement* and its (Unit) result is discarded.
 * Factory/constructor calls that merely start with an uppercase letter — e.g. `Color(0xFF..)`,
 * `TextStyle(...)`, `PaddingValues(...)` — are consumed as values (assigned to a local, passed as
 * an argument, returned) and do NOT emit content. This distinguishes real emission from those
 * false positives without needing full type resolution.
 */
internal fun KtCallExpression.isContentEmittingStatement(): Boolean {
  // Climb out of qualified / parenthesized wrappers while this call is on the value path.
  var expr: KtExpression = this
  while (true) {
    when (val parent = expr.parent) {
      is KtParenthesizedExpression -> expr = parent
      is KtQualifiedExpression -> {
        // `receiver.Selector()` — only the selector represents this call's result; if we are the
        // receiver the result is consumed by the selector, so it cannot be an emission.
        if (parent.selectorExpression == expr) expr = parent else return false
      }
      else -> break
    }
  }

  return when (val container = expr.parent) {
    // Statement inside a block or lambda body.
    is KtBlockExpression -> true
    // Body of a braces-less control structure: `if (x) Text(...)`, `while (x) Item()`.
    is KtContainerNodeForControlStructureBody -> true
    // `when (x) { a -> Text(...) }`.
    is KtWhenEntry -> true
    // Expression body of a Unit-returning composable: `@Composable fun Foo() = Column { ... }`.
    is KtNamedFunction -> container.bodyExpression == expr && container.returnsUnit()
    else -> false
  }
}

internal fun KtNamedFunction.getNameOrDefault(): String {
  return name ?: "<anonymous>"
}

internal fun KtAnnotationEntry.matchesName(vararg names: String): Boolean {
  val shortName = shortName?.asString() ?: return false
  return names.any { it == shortName }
}

internal fun PsiElement.getParentFunction(): KtNamedFunction? {
  return PsiTreeUtil.getParentOfType(this, KtNamedFunction::class.java)
}

private val inherentlyMutableHeadTypes = setOf(
  "MutableList",
  "MutableSet",
  "MutableMap",
  "MutableCollection",
  "MutableIterable",
  "MutableState",
  "ArrayList",
  "HashMap",
  "HashSet",
  "LinkedHashMap",
  "LinkedHashSet",
)

internal fun String.isMutableType(): Boolean {
  val normalized = trim().removeSuffix("?").trim()
  // A function type such as `() -> MutableList<T>` passes a factory, not a mutable instance.
  if (normalized.contains("->")) return false
  // Only the outermost type matters: `Wrapper<HashMap<..>>` is a stable wrapper, and observable
  // holders like `MutableStateFlow`/`MutableSharedFlow` are not the inherently-mutable collections
  // this rule targets. Anchoring on the head type avoids both false positives.
  val head = normalized.substringBefore("<").substringAfterLast(".").trim()
  return head in inherentlyMutableHeadTypes
}

internal fun String.isStandardCollection(): Boolean {
  val cleanType = this.substringBefore("<").trim()
  return cleanType in setOf("List", "Set", "Map", "Collection", "Iterable")
}

internal fun String.toPascalCase(): String {
  return if (isNotEmpty()) {
    this[0].uppercaseChar() + substring(1)
  } else {
    this
  }
}

internal fun String.toCamelCase(): String {
  return if (isNotEmpty()) {
    this[0].lowercaseChar() + substring(1)
  } else {
    this
  }
}
