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
import org.quartzsource.meutrino.QNodeId
import org.quartzsource.meutrino.client.ChangeContext
import org.quartzsource.meutrino.client.AbstractClientTest

class ContextTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a")
    append("b", "b")
    val (rev0, node0) = client.commit("first", addRemove = true)

    append("c", "c")
    val (rev1, node1) = client.commit("second", addRemove = true)

    val ctx = client(node0)

    assertEquals("first", ctx.description)
    assertEquals("<changectx %s>".format(node0.short), ctx.toString)
    assertEquals(node0, ctx.node)
    assertEquals(rev0, ctx.rev)
    assertEquals("default", ctx.branch)

    assertTrue(ctx.toBoolean)
    assertFalse("Manifest must not be empty.", ctx.manifest.isEmpty)
    assertTrue(ctx.manifest.exists(_._1.path == "a"))
    assertTrue(ctx.manifest.exists(_._1.path == "b"))
    assertEquals(List(QPath("a"), QPath("b")), ctx.toList)
    assertEquals(List(QPath("a"), QPath("b")), ctx.files)

    assertEquals(Nil, ctx.modified)
    assertEquals(List(QPath("a"), QPath("b")), ctx.added)
    assertEquals(Nil, ctx.removed)
    assertEquals(Nil, ctx.ignored)
    assertEquals(Nil, ctx.clean)

    val man = Map(QPath("a") -> QNodeId("047b75c6d7a3ef6a2243bd0e99f94f6ea6683597"),
      QPath("b") -> QNodeId("62452855512f5b81522aa3895892760bb8da9f3f"))
    assertEquals(man.toString(), ctx.manifest.toString())
    assertEquals(man, ctx.manifest)

    assertEquals(List(-1), ctx.parents.map(_.toInt))
    assertEquals(-1, ctx.p1.toInt)
    assertEquals(-1, ctx.p2.toInt)

    assertEquals(List(1), ctx.children.map(_.toInt))
    assertEquals(List(0, 1), ctx.descendants.map(_.toInt))
    assertEquals(List(0), ctx.ancestors.map(_.toInt))

    client.bookmark("bookmark", inactive = true, rev = Some(rev0))
    assertEquals(List("bookmark"), ctx.bookmarks)

    client.tag("tag", rev = Some(node0), user = Some("py4fun"))
    //tags are read on construction
    assertEquals(List("tag"), client(node0).tags)
  }

  @Test
  def testConstruction {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    val tip = client.tip()

    val ctx = new ChangeContext(client, tip)
    assertEquals(tip.node, ctx.node)

    // from revset
    val ctx2 = new ChangeContext(client, "all()")
    assertEquals(tip.node, ctx2.node)
  }
}

