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
  def testAdd {
    append("a", "a\n")
    assertTrue(client.add(List(QPath("a"))))
    val (_, node0) = client.commit("first", addRemove = false)
    val ctx0 = client(node0)
    assertEquals(Map((QPath("a") -> QNodeId("b789fdd96dc2f3bd229c1dd8eedf0fc60e2b68e3"))), ctx0.manifest)
    assertEquals(List(QPath("a")), ctx0.added)
  }

  @Test
  def testAddRemove {
    append("b", "b\n")
    client.addRemove(Nil)
    val (_, node0) = client.commit("first", addRemove = false)
    val ctx0 = client(node0)
    assertEquals(Map((QPath("b") -> QNodeId("1e88685f5ddec574a34c70af492f95b6debc8741"))), ctx0.manifest)
    assertEquals(List(QPath("b")), ctx0.added)
  }

  @Test
  def testAddNonExisting {
    append("a", "a\n")
    assertFalse(client.add(List(QPath("b"))))
    client.addRemove(List(QPath("b")))
  }
}

