/*
 * Designed and developed by 2025 androidpoet (Ranbir Singh)
 */
package io.androidpoet.composeguard.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that provides a template to convert composed {} to Modifier.Node.
 */
public class ConvertToModifierNodeFix : LocalQuickFix {

  override fun getFamilyName(): String = "Convert to Modifier.Node"

  override fun getName(): String = "Convert to Modifier.Node (inserts template)"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val call = element as? KtCallExpression ?: return

    val factory = KtPsiFactory(project)

    // Replace composed {} call with a TODO comment and template
    val template = """
      |// TODO: Convert to Modifier.Node pattern:
      |// 1. Create a ModifierNodeElement:
      |//    private data class MyModifierElement(...) : ModifierNodeElement<MyModifierNode>() {
      |//        override fun create() = MyModifierNode(...)
      |//        override fun update(node: MyModifierNode) { ... }
      |//    }
      |// 2. Create the ModifierNode:
      |//    private class MyModifierNode(...) : Modifier.Node(), DrawModifierNode { ... }
      |// 3. Return: Modifier.then(MyModifierElement(...))
      |Modifier
    """.trimMargin()

    val newExpr = factory.createExpression(template)
    call.replace(newExpr)
  }
}
