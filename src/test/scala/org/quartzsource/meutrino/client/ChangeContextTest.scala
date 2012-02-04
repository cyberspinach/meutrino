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
import org.quartzsource.meutrino.CommandException
import java.util.Date

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
  }
}

