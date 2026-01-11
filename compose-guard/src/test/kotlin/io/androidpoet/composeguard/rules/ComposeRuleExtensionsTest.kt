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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeRuleExtensionsTest {


  @Test
  fun testIsMutableType_mutableList() {
    assertTrue("MutableList<String>".isMutableType())
    assertTrue("MutableList".isMutableType())
  }

  @Test
  fun testIsMutableType_mutableSet() {
    assertTrue("MutableSet<Int>".isMutableType())
    assertTrue("MutableSet".isMutableType())
  }

  @Test
  fun testIsMutableType_mutableMap() {
    assertTrue("MutableMap<String, Int>".isMutableType())
    assertTrue("MutableMap".isMutableType())
  }

  @Test
  fun testIsMutableType_mutableState() {
    assertTrue("MutableState<Boolean>".isMutableType())
    assertTrue("MutableState".isMutableType())
  }

  @Test
  fun testIsMutableType_arrayList() {
    assertTrue("ArrayList<String>".isMutableType())
    assertTrue("ArrayList".isMutableType())
  }

  @Test
  fun testIsMutableType_hashMap() {
    assertTrue("HashMap<String, Int>".isMutableType())
    assertTrue("HashMap".isMutableType())
  }

  @Test
  fun testIsMutableType_hashSet() {
    assertTrue("HashSet<String>".isMutableType())
    assertTrue("HashSet".isMutableType())
  }

  @Test
  fun testIsMutableType_startsWithMutable() {
    assertTrue("MutableCustomType".isMutableType())
  }

  @Test
  fun testIsMutableType_immutableTypes() {
    assertFalse("List<String>".isMutableType())
    assertFalse("Set<Int>".isMutableType())
    assertFalse("Map<String, Int>".isMutableType())
    assertFalse("String".isMutableType())
    assertFalse("Int".isMutableType())
  }

  @Test
  fun testIsMutableType_immutableCollections() {
    assertFalse("ImmutableList<String>".isMutableType())
    assertFalse("ImmutableSet<Int>".isMutableType())
    assertFalse("ImmutableMap<String, Int>".isMutableType())
  }


  @Test
  fun testIsStandardCollection_list() {
    assertTrue("List".isStandardCollection())
    assertTrue("List<String>".isStandardCollection())
  }

  @Test
  fun testIsStandardCollection_set() {
    assertTrue("Set".isStandardCollection())
    assertTrue("Set<Int>".isStandardCollection())
  }

  @Test
  fun testIsStandardCollection_map() {
    assertTrue("Map".isStandardCollection())
    assertTrue("Map<String, Int>".isStandardCollection())
  }

  @Test
  fun testIsStandardCollection_collection() {
    assertTrue("Collection".isStandardCollection())
    assertTrue("Collection<Any>".isStandardCollection())
  }

  @Test
  fun testIsStandardCollection_iterable() {
    assertTrue("Iterable".isStandardCollection())
    assertTrue("Iterable<String>".isStandardCollection())
  }

  @Test
  fun testIsStandardCollection_notStandardCollections() {
    assertFalse("MutableList<String>".isStandardCollection())
    assertFalse("ArrayList<String>".isStandardCollection())
    assertFalse("ImmutableList<String>".isStandardCollection())
    assertFalse("String".isStandardCollection())
    assertFalse("Int".isStandardCollection())
  }


  @Test
  fun testToPascalCase_lowercaseFirst() {
    assertEquals("Hello", "hello".toPascalCase())
    assertEquals("UserCard", "userCard".toPascalCase())
  }

  @Test
  fun testToPascalCase_alreadyPascalCase() {
    assertEquals("Hello", "Hello".toPascalCase())
    assertEquals("UserCard", "UserCard".toPascalCase())
  }

  @Test
  fun testToPascalCase_emptyString() {
    assertEquals("", "".toPascalCase())
  }

  @Test
  fun testToPascalCase_singleCharacter() {
    assertEquals("A", "a".toPascalCase())
    assertEquals("A", "A".toPascalCase())
  }


  @Test
  fun testToCamelCase_uppercaseFirst() {
    assertEquals("hello", "Hello".toCamelCase())
    assertEquals("userCard", "UserCard".toCamelCase())
  }

  @Test
  fun testToCamelCase_alreadyCamelCase() {
    assertEquals("hello", "hello".toCamelCase())
    assertEquals("userCard", "userCard".toCamelCase())
  }

  @Test
  fun testToCamelCase_emptyString() {
    assertEquals("", "".toCamelCase())
  }

  @Test
  fun testToCamelCase_singleCharacter() {
    assertEquals("a", "A".toCamelCase())
    assertEquals("a", "a".toCamelCase())
  }
}
