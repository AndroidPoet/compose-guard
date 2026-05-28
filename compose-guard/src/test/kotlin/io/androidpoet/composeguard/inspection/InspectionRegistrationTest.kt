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
package io.androidpoet.composeguard.inspection

import org.xml.sax.InputSource
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRegistrationTest {

  @Test
  fun composeGuardInspectionIsRegisteredInPluginXml() {
    val xmlInspectionNode = composeGuardInspectionNode()
    assertEquals(ComposeGuardInspection::class.java.name, xmlInspectionNode.attribute("implementationClass"))
  }

  @Test
  fun shortNameInPluginXmlMatchesInspectionClass() {
    val xmlShortName = composeGuardInspectionNode().attribute("shortName")
    assertEquals(xmlShortName, ComposeGuardInspection().shortName)
  }

  @Test
  fun shortNameRemainsComposeGuard() {
    val xmlShortName = composeGuardInspectionNode().attribute("shortName")
    assertEquals("ComposeGuard", xmlShortName)
    assertEquals("ComposeGuard", ComposeGuardInspection().shortName)
  }

  private fun composeGuardInspectionNode(): Node {
    val implClass = ComposeGuardInspection::class.java.name
    val stream = javaClass.classLoader.getResourceAsStream("META-INF/plugin.xml")!!
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(stream))
    val nodes = document.getElementsByTagName("localInspection")
    return (0 until nodes.length)
      .map { nodes.item(it) }
      .firstOrNull { it.attribute("implementationClass") == implClass }
      ?: error("Missing localInspection entry for $implClass in META-INF/plugin.xml")
  }
}

private fun Node.attribute(name: String): String {
  return attributes.getNamedItem(name)?.nodeValue
    ?: error("Missing attribute '$name' on localInspection node")
}
