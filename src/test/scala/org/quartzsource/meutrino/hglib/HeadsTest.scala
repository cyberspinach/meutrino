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

import org.junit.Assert.assertEquals
import org.junit.Test
import org.quartzsource.meutrino.client.AbstractClientTest

class HeadsTest extends AbstractClientTest {

  @Test
  def testEmpty {
    assertEquals(Nil, client.heads())
  }

  @Test
  def testBasic {
    append("a", "a\n")
    val (rev0, node0) = client.commit("first", addRemove = true)
    assertEquals(List(client.tip()), client.heads())

    client.branch(Some("foo"))
    append("a", "a\n")
    val (rev1, node1) = client.commit("second")
    assertEquals(Nil, client.heads(revs = List(node0), topological = true))
  }
}

