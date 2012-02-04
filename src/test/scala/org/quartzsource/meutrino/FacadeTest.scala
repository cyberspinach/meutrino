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

package org.quartzsource.meutrino

import java.util.Date

import org.junit.Assert._
import org.junit.Test

@Test
class FacadeTest extends AbstractTest {

  @Test(expected = classOf[IllegalArgumentException])
  def testQRevisionTooShort {
    QRevision(List("1", "2", "3"))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQRevisionEmpty {
    QRevision(Nil)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQRevisionNull {
    QRevision(null)
  }

  @Test
  def testQRevision {
    val rev = QRevision(List("1", "1234567890aaaaaaaaaa1234567890bbbbbbbbbb", "tip", "default", "py4fun", "foo\nbar", "1326905676.0-3600"))
    assertEquals(1, rev.rev)
    assertEquals(QNodeId("1234567890aaaaaaaaaa1234567890bbbbbbbbbb"), rev.node)
    assertEquals(List("tip"), rev.tags)
    assertEquals("default", rev.branch)
    assertEquals("py4fun", rev.author)
    assertEquals("foo\nbar", rev.desc)
    assertEquals("1326905676.0-3600", rev.mdate)
    val milis: Long = 1326905676L * 1000L
    assertEquals(rev.mdate, new Date(milis), rev.date)
  }

  @Test
  def testQBookmark {
    val bookmark = QBookmark("name1", true)
    assertEquals("name1", bookmark.name)
    assertEquals("name1", bookmark.getName)
    assertTrue(bookmark.active)
    assertTrue(bookmark.getActive)
    assertEquals("name1", bookmark.toString())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQBookmarkNull {
    QBookmark(null, true)
  }

  @Test
  def testQBranch {
    val branch = QBranch("name1", true)
    assertEquals("name1", branch.name)
    assertEquals("name1", branch.getName)
    assertTrue(branch.active)
    assertTrue(branch.getActive)
    assertEquals("name1", branch.toString())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQBranchNull {
    QBranch(null, true)
  }

  @Test
  def testQNodeId {
    val id = "1" * 40
    val node = QNodeId(id)
    assertEquals(id, node.node)
    assertEquals(id, node.getNode())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQNodeIdNull {
    QNodeId(null)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQNodeIdInvalid {
    QNodeId("1234567890")
  }

  @Test
  def testQPath {
    val path = QPath("a")
    assertEquals("a", path.path)
    assertEquals("a", path.getPath())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQPathNull {
    QPath(null)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQPathEmpty {
    QPath("")
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQPathRoot {
    QPath(java.io.File.separatorChar + "a")
  }

  @Test
  def testQVersion {
    val version = QVersion(1, 9, 2)
    assertEquals(1, version.major)
    assertEquals(9, version.minor)
    assertEquals(2, version.fix)
    assertEquals("1.9.2", version.toString())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQVersionOld {
    QVersion(0, 9, 1)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQVersionPositiveMinor {
    QVersion(2, -2, 1)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQVersionPositiveFix {
    QVersion(2, 1, -1)
  }
}
