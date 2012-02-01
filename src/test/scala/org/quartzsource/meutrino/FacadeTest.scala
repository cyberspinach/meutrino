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
  def testQRevisionWrong1 {
    QRevision(List("1", "2", "3"))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQRevisionLazyNil {
    QRevision(Nil)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testQRevisionWrong2 {
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
}
