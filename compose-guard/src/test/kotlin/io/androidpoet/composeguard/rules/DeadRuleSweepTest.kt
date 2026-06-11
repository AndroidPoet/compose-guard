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
 * Exercises every default-enabled rule against a corpus that contains a canonical *violation* for
 * each one, and asserts every rule fires at least once. This guards against a rule silently going
 * dead — e.g. `LazyListMissingKey`, which for a long time never reported because its key-detection
 * mistook the trailing content lambda for a `key` argument. If you add a new default-enabled rule,
 * add a canonical violating snippet for it to [corpus].
 */
class DeadRuleSweepTest : BasePlatformTestCase() {

  private val corpus = """
    import androidx.compose.material.Button
    import androidx.compose.material.Text

    annotation class Composable
    annotation class Preview
    annotation class ComposableTargetMarker

    @Composable fun lowercasename() { Text("x") }

    @Composable fun NoModifier() { Text("x") }

    @Composable fun ModNoDefault(modifier: Modifier) { Text("x") }

    @Composable fun ModBadName(mod: Modifier = Modifier) { Text("x") }

    @Composable fun TopMost(modifier: Modifier = Modifier) {
      Box { Column(modifier = modifier) { Text("a") } }
    }

    @Composable fun Reuse(modifier: Modifier = Modifier) {
      Column(modifier = modifier) { Text("a", modifier = modifier) }
    }

    @Composable fun OrderBad() {
      Box(modifier = Modifier.padding(16.dp).clickable { })
    }

    fun Modifier.shimmer(): Modifier = composed { this }

    @Composable fun NoRemember() { val s = mutableStateOf(0) }

    @Composable fun TypeSpecific() { val s = remember { mutableStateOf(0) } }

    @Composable fun Derived(users: List<String>) { val names = users.map { it.length } }

    @Composable fun Frequent(flow: Any) { val s = flow.collectAsState() }

    @Composable fun DeferReads(scrollState: Any) {
      val offset = scrollState.value
      Box(modifier = Modifier.offset(y = offset))
    }

    @Composable fun MutStateParam(s: MutableState<Int>) { }

    @Composable fun ParamOrder(a: Int = 0, b: Int) { }

    @Composable fun Trailing(content: @Composable () -> Unit, title: String) { }

    @Composable fun MutParam(items: MutableList<Int>) { }

    @Composable fun Explicit() { val vm = viewModel<MyViewModel>() }

    @Composable fun Forward(vm: MyViewModel) { Child(viewModel = vm) }

    @Composable fun EmitAndReturn(): String { Text("x"); return "r" }

    val CustomTheme = staticCompositionLocalOf { 0 }

    @Preview @Preview annotation class Devices

    @ComposableTargetMarker annotation class MyApplier

    @Composable fun Event(onClicked: () -> Unit) { }

    @Composable fun ContentEmit() { Text("a"); Text("b") }

    @Composable fun SlotReuse(content: @Composable () -> Unit) { content(); content() }

    @Composable fun Effect(id: Int) { LaunchedEffect(Unit) { println(id) } }

    @Composable fun LambdaEffect(onTick: () -> Unit) { LaunchedEffect(Unit) { onTick() } }

    @Composable fun Movable(content: @Composable () -> Unit) {
      val m = movableContentOf { content() }
      m()
    }

    @Preview @Composable fun HomeScreen() { }

    @Composable fun LazyType(data: List<Int>) {
      LazyColumn {
        item { Text("h") }
        items(data) { Text(it.toString()) }
      }
    }

    @Composable fun LazyKey(data: List<Int>) {
      LazyColumn { items(data) { Text(it.toString()) } }
    }

    @Composable fun Mat2() { Button(onClick = {}) { } }

    @Composable fun Unstable(items: List<Int>) { }

    @Composable fun Hoist() {
      val query = remember { mutableStateOf("") }
      Column {
        SearchField(text = query)
        ResultsList(filter = query)
      }
    }
  """.trimIndent()

  fun test_sweep() {
    val file = myFixture.configureByText("Corpus.kt", corpus) as KtFile
    val ctx = AnalysisContext(file)
    val fns = PsiTreeUtil.findChildrenOfType(file, KtNamedFunction::class.java)
    val props = PsiTreeUtil.findChildrenOfType(file, KtProperty::class.java)
    val classes = PsiTreeUtil.findChildrenOfType(file, KtClass::class.java)
    val elements = PsiTreeUtil.findChildrenOfType(file, KtElement::class.java)

    val report = StringBuilder("\n=== DEAD RULE SWEEP ===\n")
    val zeros = mutableListOf<String>()
    for (rule in ComposeRuleRegistry.getAllRules()) {
      if (!rule.enabledByDefault) continue
      var count = 0
      for (fn in fns) count += rule.analyzeFunction(fn, ctx).size
      for (p in props) count += rule.analyzeProperty(p, ctx).size
      for (c in classes) count += rule.analyzeClass(c, ctx).size
      for (e in elements) count += rule.analyzeElement(e, ctx).size
      if (count == 0) zeros.add(rule.id)
      report.append(String.format("%-28s %s%n", rule.id, if (count == 0) "*** ZERO ***" else count.toString()))
    }
    report.append("\nZERO-firing default rules: ${if (zeros.isEmpty()) "(none)" else zeros.joinToString(", ")}\n")
    println(report)

    assertTrue(
      "These default-enabled rules produced no violations on the corpus — they may be silently " +
        "dead, or need a canonical violating snippet added to the corpus: $zeros",
      zeros.isEmpty(),
    )
  }
}
