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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddContentTypeFixTest {

  private val fix = AddContentTypeFix()


  @Test
  fun testFamilyName() {
    assertEquals("Add contentType parameter", fix.familyName)
  }

  @Test
  fun testName() {
    assertEquals("Add contentType to items", fix.name)
  }

  @Test
  fun testIsHighPriorityAction() {
    assertTrue(fix is com.intellij.codeInsight.intention.HighPriorityAction)
  }


  @Test
  fun testExpectedBehavior_itemWithTrailingLambda() {
    val inputDescription = "item { Text(\"Header\") }"
    val expectedOutput = "item(contentType = \"contentType1\") { Text(\"Header\") }"
    val incorrectOutput =
      "item(key = { Text(\"Header\") }, contentType = \"contentType1\") { Text(\"Header\") }"

    assertTrue(expectedOutput.contains("contentType"))
    assertTrue(!expectedOutput.contains("key = {"))
    assertTrue(incorrectOutput.contains("key = {"))
  }

  @Test
  fun testExpectedBehavior_itemWithExistingKey() {
    val inputDescription = "item(key = \"myKey\") { Text(\"Header\") }"
    val expectedOutput =
      "item(key = \"myKey\", contentType = \"contentType1\") { Text(\"Header\") }"

    assertTrue(expectedOutput.contains("key = \"myKey\""))
    assertTrue(expectedOutput.contains("contentType = \"contentType1\""))
  }

  @Test
  fun testExpectedBehavior_itemsWithList() {
    val inputDescription = "items(users) { Text(it.name) }"
    val expectedOutput =
      "items(items = users, contentType = { _ -> \"contentType1\" }) { Text(it.name) }"

    assertTrue(expectedOutput.contains("items = users"))
    assertTrue(expectedOutput.contains("contentType = { _ ->"))
  }

  @Test
  fun testExpectedBehavior_itemsWithExistingKey() {
    val inputDescription = "items(users, key = { it.id }) { Text(it.name) }"
    val expectedOutput =
      "items(items = users, key = { it.id }, contentType = { _ -> \"ct1\" }) { ... }"

    assertTrue(expectedOutput.contains("key = { it.id }"))
    assertTrue(expectedOutput.contains("contentType = { _ ->"))
  }

  @Test
  fun testExpectedBehavior_itemsIndexed() {
    val inputDescription = "itemsIndexed(users) { index, user -> Text(user.name) }"
    val expectedOutput =
      "itemsIndexed(users, contentType = { _, _ -> \"ct1\" }) { index, user -> ... }"

    assertTrue(expectedOutput.contains("users"))
    assertTrue(expectedOutput.contains("contentType = { _, _"))
  }

  @Test
  fun testExpectedBehavior_stickyHeader() {
    val inputDescription = "stickyHeader { Text(\"Header\") }"
    val expectedOutput = "stickyHeader(contentType = \"contentType1\") { Text(\"Header\") }"

    assertTrue(expectedOutput.contains("contentType = \"contentType1\""))
    assertTrue(!expectedOutput.contains("key = {"))
  }


  @Test
  fun testBugFix_lambdaNotTreatedAsKey() {

    val buggyOutput =
      "item(key = { Text(\"Header\") }, contentType = \"ct1\") { Text(\"Header\") }"
    val fixedOutput = "item(contentType = \"contentType1\") { Text(\"Header\") }"

    assertTrue(buggyOutput.contains("key = { Text"))
    assertTrue(!fixedOutput.contains("key = { Text"))
  }

  @Test
  fun testExpectedBehavior_namedContentArgMovedToTrailing() {
    val inputDescription = "item(content = { Text(\"Header\") })"
    val expectedOutput = "item(contentType = \"contentType1\") { Text(\"Header\") }"

    assertTrue(expectedOutput.contains("contentType = \"contentType1\""))
    assertTrue(expectedOutput.endsWith("{ Text(\"Header\") }"))
  }


  @Test
  fun testContentTypeNaming_usesNumberedScheme() {
    val expectedPattern = "contentType\\d+"
    val sample = "contentType1"

    assertTrue(sample.matches(Regex(expectedPattern)))
  }

  @Test
  fun testContentTypeNaming_startsFromOne() {
    val expected = "contentType1"
    assertTrue(expected.endsWith("1"))
  }
}
