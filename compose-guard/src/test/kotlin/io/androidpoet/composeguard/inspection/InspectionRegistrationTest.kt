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
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRegistrationTest {

  @Test
  fun shortNameInPluginXmlMatchesInspectionClass() {
    val implClass = ComposeGuardInspection::class.java.name
    val stream = javaClass.classLoader.getResourceAsStream("META-INF/plugin.xml")!!
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(stream))
    val nodes = doc.getElementsByTagName("localInspection")

    val xmlShortName = (0 until nodes.length)
      .map { nodes.item(it) }
      .first { it.attributes.getNamedItem("implementationClass")?.nodeValue == implClass }
      .attributes.getNamedItem("shortName").nodeValue

    assertEquals(xmlShortName, ComposeGuardInspection().shortName)
  }
}
