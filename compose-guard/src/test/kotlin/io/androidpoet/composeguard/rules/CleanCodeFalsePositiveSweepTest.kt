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

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * The inverse of [DeadRuleSweepTest]: runs every default-enabled rule over a corpus of idiomatic,
 * rule-following Compose and asserts that NOTHING fires. Any violation here is a false positive on
 * clean code — exactly the kind of noise that drives users away. New idioms that are known-good can
 * be added to [corpus] to lock in that they stay quiet.
 */
class CleanCodeFalsePositiveSweepTest : BasePlatformTestCase() {

  private val corpus = """
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.lifecycle.compose.collectAsStateWithLifecycle

    // A stateless, hoisted component with a properly named/defaulted modifier.
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
      Text(text = name, modifier = modifier)
    }

    // Single-element wrapper — modifier forwarded, no hoisting needed.
    @Composable
    fun PrimaryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
      Button(onClick = onClick, modifier = modifier) { Text("Go") }
    }

    // Correct modifier order: clickable before padding.
    @Composable
    fun Tile(modifier: Modifier = Modifier) {
      Box(modifier = modifier.clickable { }.padding(16.dp)) { Text("x") }
    }

    // Type-specific state, remembered, hoisted via callback.
    @Composable
    fun Counter(count: Int, onCountChange: (Int) -> Unit, modifier: Modifier = Modifier) {
      Button(onClick = { onCountChange(count + 1) }, modifier = modifier) { Text("${'$'}count") }
    }

    // Internal UI state that is genuinely local (expansion toggle) — fine to keep.
    @Composable
    fun Expandable(modifier: Modifier = Modifier) {
      val expanded = remember { mutableIntStateOf(0) }
      Column(modifier = modifier) { Text("only child reads nothing shared") }
    }

    // Lifecycle-aware flow collection.
    @Composable
    fun Screen(viewModel: MyViewModel, modifier: Modifier = Modifier) {
      val state by viewModel.state.collectAsStateWithLifecycle()
      Text(text = state, modifier = modifier)
    }

    // Run-once effect with a constant key — allowed.
    @Composable
    fun OneShot(modifier: Modifier = Modifier) {
      LaunchedEffect(Unit) { }
      Box(modifier = modifier) { Text("x") }
    }

    // Lazy list with keys and contentType, stable (immutable) collection param — fully specified.
    @Composable
    fun ItemList(data: ImmutableList<Item>, modifier: Modifier = Modifier) {
      LazyColumn(modifier = modifier) {
        items(data, key = { it.id }, contentType = { "row" }) { Text(it.label) }
      }
    }

    // Properly named preview.
    @Preview
    @Composable
    private fun GreetingPreview() {
      Greeting(name = "World")
    }

    // Event parameter present tense, trailing lambda last.
    @Composable
    fun Dialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
      content()
    }

    annotation class Composable
    annotation class Preview
  """.trimIndent()

  fun test_noFalsePositivesOnCleanCode() {
    val file = myFixture.configureByText("Clean.kt", corpus) as KtFile
    val ctx = AnalysisContext(file)
    val fns = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
    val props = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java)
    val classes = PsiTreeUtil.findChildrenOfType(file, KtClass::class.java)
    val elements = PsiTreeUtil.findChildrenOfType(file, KtElement::class.java)

    val report = StringBuilder("\n=== CLEAN-CODE FALSE-POSITIVE SWEEP ===\n")
    val offenders = mutableListOf<String>()
    for (rule in ComposeRuleRegistry.getAllRules()) {
      if (!rule.enabledByDefault) continue
      val hits = mutableListOf<String>()
      for (fn in fns) rule.analyzeFunction(fn, ctx).forEach { hits.add("fun ${fn.name}: ${it.message}") }
      for (p in props) rule.analyzeProperty(p, ctx).forEach { hits.add("prop ${p.name}: ${it.message}") }
      for (c in classes) rule.analyzeClass(c, ctx).forEach { hits.add("class ${c.name}: ${it.message}") }
      for (e in elements) rule.analyzeElement(e, ctx).forEach { hits.add("elem: ${it.message}") }
      if (hits.isNotEmpty()) {
        offenders.add(rule.id)
        report.append("${rule.id} (${hits.size}):\n")
        hits.distinct().take(5).forEach { report.append("    $it\n") }
      }
    }
    report.append("\nRules firing on clean code: ${if (offenders.isEmpty()) "(none)" else offenders.joinToString(", ")}\n")
    println(report)

    assertTrue(
      "These rules fired on idiomatic, rule-following code — that is a false positive. See test " +
        "output for the offending messages: $offenders",
      offenders.isEmpty(),
    )
  }
}
