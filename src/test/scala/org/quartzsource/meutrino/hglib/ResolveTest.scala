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
import org.quartzsource.meutrino.REMOVED
import org.quartzsource.meutrino.UNRESOLVED
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.client.AbstractClientTest

class ResolveTest extends AbstractClientTest {
  append("a", "a")
  append("b", "b")
  val (_, node0) = client.commit("first", addRemove = true)

  append("a", "a")
  append("b", "b")
  val (_, node1) = client.commit("second")

  @Test
  def testBasic {
    client.update(Some(node0))
    append("a", "b")
    append("b", "a")
    val (_, node3) = client.commit("third")

    assertFalse(client.merge(Some(node1)))
    assertFalse(client.resolve(all = true))

    assertEquals(List((UNRESOLVED, QPath("a")), (UNRESOLVED, QPath("b"))),
      client.resolveListFiles())

    assertTrue(client.resolve(List(QPath("a")), mark = true))
    assertEquals(List((REMOVED, QPath("a")), (UNRESOLVED, QPath("b"))),
      client.resolveListFiles())
  }
}

