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
import org.quartzsource.meutrino.QPath

class ParentsTest extends AbstractHglibTest {

  @Test
  def testNoParents {
    assertEquals(Nil, client.parents())
  }

  @Test
  def testBasic {
    append("a", "a")
    val (rev, node) = client.commit("first", addRemove = true)
    assertEquals(node, client.parents()(0).node)
    assertEquals(node, client.parents(file = Some(QPath("a")))(0).node)
  }

  @Test
  def testTwoParents {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("change")
    client.update(Some(node0))
    client.remove(List(QPath("a")))
    val (rev2, node2) = client.commit("remove")

    client.merge(nonInteractive = true)

    assertEquals(node2, client.parents()(0).node)
    assertEquals(node1, client.parents()(1).node)
  }
}

