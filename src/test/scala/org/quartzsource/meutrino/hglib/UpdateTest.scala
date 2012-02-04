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
import org.quartzsource.meutrino.MODIFIED
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.client.AbstractClientTest

class UpdateTest extends AbstractClientTest {
  append("a", "a")
  val (rev0, node0) = client.commit("first", addRemove = true)
  append("a", "a")
  val (rev1, node1) = client.commit("second")

  @Test
  def testBasic {
    val (u, m, r, ur) = client.update(Some(node0))
    assertEquals(1, u)
    assertEquals(0, m)
    assertEquals(0, r)
    assertEquals(0, ur)
  }

  @Test
  def testUnresolved {
    client.update(Some(node0))
    append("a", "b")
    val (u, m, r, ur) = client.update()
    assertEquals(0, u)
    assertEquals(0, m)
    assertEquals(0, r)
    assertEquals(1, ur)
    assertTrue(client.status().contains((MODIFIED, QPath("a"))))
  }

  @Test
  def testMerge {
    val content = "\n\n\n\nb"
    append("a", content)
    val (rev2, node2) = client.commit("third")
    append("a", "b")
    client.commit("fourth")
    client.update(Some(node2))
    write("a", "a" + content)
    val (u, m, r, ur) = client.update()
    assertEquals(0, u)
    assertEquals(1, m)
    assertEquals(0, r)
    assertEquals(0, ur)
    assertTrue(client.status().contains((MODIFIED, QPath("a"))))
  }

  @Test
  def testTip {
    client.update(Some(node0))
    val (u, m, r, ur) = client.update()
    assertEquals(1, u)
    assertEquals(0, m)
    assertEquals(0, r)
    assertEquals(0, ur)
    assertEquals(node1, client.parents()(0).node)

    client.update(Some(node0))
    append("a", "b")
    val (rev2, node2) = client.commit("new head")
    client.update(Some(node0))
    client.update()
    assertEquals(node2, client.parents()(0).node)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCheckClean {
    client.update(check = true, clean = true)
  }

  @Test
  def testClean {
    val old = read("a")
    append("a", "b")
    try {
      client.update(check = true)
      fail()
    } catch {
      case e: Exception => assertEquals("abort: uncommitted local changes\n", e.getMessage())
    }
    val (u, m, r, ur) = client.update(clean = true)
    assertEquals(1, u)
    assertEquals(0, m)
    assertEquals(0, r)
    assertEquals(0, ur)
    assertEquals(old, read("a"))
  }
}

