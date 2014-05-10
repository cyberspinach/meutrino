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

package org.quartzsource.meutrino.client

import org.junit.Assert._
import org.junit.Test
import java.util.Date
import org.quartzsource.meutrino.QRevision
import org.quartzsource.meutrino.QuartzException

class ChangeContextTest extends AbstractClientTest {

  @Test
  def testBasicProperties {
    append("a", "a")
    val now = new Date()
    val (_, node) = client.commit("first", Some("foo"), addRemove = true, date = Some(now))
    val cxt = client(node)
    assertEquals(0, cxt.rev)
    assertEquals(node, cxt.node)
    assertEquals(Nil, cxt.tags)
    assertEquals("default", cxt.branch)
    assertEquals("foo", cxt.author)
    assertEquals("first", cxt.description)
    assertEquals(new Date(now.getTime() / 1000 * 1000), cxt.date)
    assertEquals("<changectx 000000000000>", cxt.p1.toString())
    assertEquals("<changectx 000000000000>", cxt.p2.toString())
    assertEquals(List(cxt.p1), cxt.parents)
    assertEquals(List(), cxt.bookmarks)
    assertEquals(List(), cxt.children)
    assertEquals(cxt, cxt.ancestor(node))
    assertEquals(node.node, cxt.hex)
    assertEquals(cxt.manifest, cxt.toMap)
    assertEquals(Map(), cxt.status(true, true))
    assertTrue(cxt.toBoolean)
  }

  @Test
  def testInitProperties {
    val revision = client.tip()
    assertEquals(-1, revision.toInt)
    val etalon = QRevision(List("-1", "0000000000000000000000000000000000000000", "tip", "default", "", "", "00"))
    assertEquals(etalon, revision)
  }

  @Test
  def testEquals {
    append("a", "a")
    val (_, node) = client.commit("first", Some("foo"), addRemove = true)
    append("a", "b")
    val (_, node1) = client.commit("second", Some("foo"))
    val cxt0 = client(node)
    val cxt1 = client(node1)
    assertFalse(cxt0.hashCode() == cxt1.hashCode())
    assertFalse(cxt0 == cxt1)
    assertFalse(cxt1.node == cxt0.node)
    assertEquals(cxt0, client(node))
    val set = Set(cxt0, cxt1, cxt0, cxt1)
    assertEquals(2, set.size)
    assertTrue(set.contains(cxt0))
    assertTrue(set.contains(cxt1))
  }

  @Test
  def testMerged {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("change")
    client.update(Some(node0))
    append("b", "a")
    val (rev2, node2) = client.commit("new file", addRemove = true)
    assertTrue(client.merge(Some(node1)))
    val parent1 :: parent2 :: Nil = client.parents()
    val (rev, node) = client.commit("merged", addRemove = true)
    val cxt = client(node)
    assertEquals(2, cxt.parents.size)
    assertEquals(parent1.node, cxt.p1.node)
    assertEquals(parent2.node, cxt.p2.node)
    assertEquals(client(node2), cxt.p1)
    assertEquals(client(node1), cxt.p2)
    assertEquals(List(cxt.p1, cxt.p2), cxt.parents)
    assertEquals(List(client(node)), client(node1).children)
    assertEquals(cxt, cxt.ancestor(node))
  }

  @Test
  def testNotValidRevision {
    append("a", "a")
    val now = new Date()
    val (_, node) = client.commit("first", Some("foo"), addRemove = true, date = Some(now))
    try {
      val cxt = client(25)
      fail()
    } catch {
      case e: QuartzException => assertEquals("abort: unknown revision '25'!\n", e.getMessage())
    }
  }
}

