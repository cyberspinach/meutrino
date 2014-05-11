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
import org.quartzsource.meutrino.CommandException
import java.io.File
import org.quartzsource.meutrino.client.AbstractClientTest

class OutgoingIncomingTest extends AbstractClientTest {

  @Test(expected = classOf[CommandException])
  def testNoPath {
    client.incoming()
  }

  @Test
  def testEmpty {
    client.clone(dest = Some("other"))
    val other = factory.open(new File(rootFolder, "other"))
    assertEquals(Nil, other.incoming())
    assertEquals(Nil, other.outgoing())
  }

  @Test
  def testBasic {
    append("a", "a")
    client.commit("first", addRemove = true)
    append("a", "a")
    client.commit("second")

    client.clone(dest = Some("other"))
    val other = factory.open(new File(rootFolder, "other"))
    assertEquals(client.log(), other.log())
    assertEquals(other.incoming(), client.outgoing(path = Some("other")))

    append("a", "a")
    val (rev, node) = client.commit("third")
    val out = client.outgoing(path = Some("other"))

    assertEquals(1, out.size)
    assertEquals(node, out(0).node)
  }

  @Test
  def testBookmarks {
    append("a", "a")
    client.commit("first", addRemove = true)
    append("a", "a")
    client.commit("second")

    client.clone(dest = Some("other"))
    val other = factory.open(new File(rootFolder, "other"))

    client.bookmark("bm1", Some(1))
    assertEquals(List(("bm1", client.tip().node.node)), other.incomingBookmarks())
    assertEquals(List(("bm1", client.tip().node.node)), client.outgoingBookmarks(path = Some("other")))
  }

}

