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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ComposableRulesDemo(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("Test composable patterns", modifier = Modifier.padding(bottom = 8.dp))
    SingleRoot()
  }
}

@Composable
fun EmitAndReturn(): String {
  Column { Text("Hello") }
  return "World"
}

@Composable
fun EmitOnly(modifier: Modifier = Modifier) {
  Column(modifier = modifier) { Text("Hello") }
}

@Composable
fun MultiRoot() {
  Text("First")
  Text("Second")
}

@Composable
fun SingleRoot(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text("First")
    Text("Second")
  }
}

@Composable
fun SlotTwice(flag: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Column(modifier = modifier) {
    if (flag) content() else content()
  }
}

@Composable
fun ConstantKey(id: String, modifier: Modifier = Modifier) {
  LaunchedEffect(Unit) { println("never restarts") }
  Box(modifier = modifier) { Text(id) }
}

@Composable
fun ProperKey(id: String, modifier: Modifier = Modifier) {
  LaunchedEffect(id) { println("restarts on change") }
  Box(modifier = modifier) { Text(id) }
}

@Composable
fun LambdaNotKeyed(onDone: () -> Unit, modifier: Modifier = Modifier) {
  LaunchedEffect(Unit) {
    delay(1000)
    onDone()
  }
  Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun LambdaKeyed(onDone: () -> Unit, modifier: Modifier = Modifier) {
  LaunchedEffect(onDone) {
    delay(1000)
    onDone()
  }
  Box(modifier = modifier) { Text("Effect") }
}

@Composable
fun MovableNotRemembered(modifier: Modifier = Modifier) {
  val content = remember { movableContentOf { Text("Movable") } }
  Box(modifier = modifier) { content() }
}

@Composable
fun MovableRemembered(modifier: Modifier = Modifier) {
  val content = remember { movableContentOf { Text("Movable") } }
  Box(modifier = modifier) { content() }
}

@Preview
@Composable
fun PublicPreview() {
  Text("Preview")
}

@Preview
@Composable
private fun PrivatePreview() {
  Text("Preview")
}

data class User(val id: String, val name: String)

@Composable
fun NoContentType(users: List<User>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    item { Text("Header") }
    items(users, key = { it.id }) { Text(it.name) }
    item { Text("Footer") }
  }
}

@Composable
fun WithContentType(users: List<User>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    item(contentType = "contentType1") { Text("Header") }
    items(
      items = users,
      key = { user -> user.id },
      contentType = { _ -> "contentType2" },
    ) { user -> Text(user.name) }
    item(contentType = "footer") { Text("Footer") }
  }
}

@Composable
fun NoKey(users: List<User>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    items(users) { Text(it.name) }
  }
}

@Composable
fun WithKey(users: List<User>, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    items(users, key = { it.id }) { Text(it.name) }
  }
}
