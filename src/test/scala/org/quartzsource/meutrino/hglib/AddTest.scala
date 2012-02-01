/**
 * Copyright (c) 2012, www.quartzsource.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quartzsource.meutrino.hglib

import org.junit.Assert._
import org.junit.Test
import org.quartzsource.meutrino._

class AddTest extends AbstractHglibTest {

  @Test
  def testBasic {
    append("a", "a\n")
    assertTrue(client.add(List(QPath("a"))))
    val (_, node0) = client.commit("first")
    val cset0 = client(node0)
    val manifest0 = cset0.manifest
    assertEquals(1, manifest0.size)
    assertTrue(manifest0.exists(t => t._1.path == "a"))
    assertEquals(List(QPath("a")), cset0.added)

    append("b", "b\n")
    client.addRemove(Nil)
    val (_, node1) = client.commit("second")
    val cset1 = client(node1)
    val manifest1 = cset1.manifest
    assertEquals(2, manifest1.size)
    assertTrue(manifest1.exists(t => t._1.path == "b"))
    assertEquals(List(QPath("b")), cset1.added)
  }

  @Test
  def testAddNonExisting {
    append("a", "a\n")
    assertFalse(client.add(List(QPath("b"))))
    client.addRemove(List(QPath("b")))
  }
}

