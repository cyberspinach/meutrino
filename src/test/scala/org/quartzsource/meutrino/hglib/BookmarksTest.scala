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
import org.quartzsource.meutrino.QBookmark
import org.quartzsource.meutrino.CommandException
import org.quartzsource.meutrino.client.AbstractClientTest

class BookmarksTest extends AbstractClientTest {
  @Test
  def testEmpty {
    assertEquals((List()), client.bookmarks())
  }

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")

    client.bookmark("0", Some(rev0))
    assertEquals(List((QBookmark("0", false), rev0, node0)), client.bookmarks())

    client.bookmark("1", Some(rev1))
    assertEquals(List((QBookmark("0", false), rev0, node0),
      (QBookmark("1", true), rev1, node1)), client.bookmarks())
  }

  @Test
  def testBasic2 {
    //test only one bookmark, which is active
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")

    client.bookmark("0")
    assertEquals(List((QBookmark("0", true), rev1, node1)), client.bookmarks())
  }

  @Test
  def testSpaces {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")

    client.bookmark("s paces")
    assertEquals(List((QBookmark("s paces", true), rev1, node1)), client.bookmarks())
  }

  @Test
  def testInActiveAndRenameAndDelete {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")

    client.bookmark("0", inactive = true)
    assertEquals(List((QBookmark("0", false), rev1, node1)), client.bookmarks())

    client.bookmark("1", rename = Some("0"))
    assertEquals(List((QBookmark("1", false), rev1, node1)), client.bookmarks())

    client.bookmark("1", delete = true)
    assertEquals((List()), client.bookmarks())
  }

  @Test(expected = classOf[CommandException])
  def testNoActiveAndRename2 {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")

    client.bookmark("0", inactive = true)
    assertEquals(List((QBookmark("0", false), rev1, node1)), client.bookmarks())

    client.bookmark("1", rename = Some("bla-bla"))
    fail("Should not rename non-existent bookmark")
  }
}

