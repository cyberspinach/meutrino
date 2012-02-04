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
import org.quartzsource.meutrino.QPath
import java.io.File
import org.quartzsource.meutrino.client.AbstractClientTest

class MoveTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a")
    client.add(List(QPath("a")))
    assertTrue(client.move(QPath("a"), QPath("b")))
  }

  @Test
  def testForce {
    append("a", "a")
    client.add(List(QPath("a")))
    client.commit("first")
    append("b", "b\n")
    try {
      client.move(QPath("b"), QPath("a"))
      fail()
    } catch {
      case e: CommandException => assertEquals("""b: not copying - file is not managed
abort: no files to copy
""", e.getMessage())
    }
    assertTrue(client.move(QPath("a"), QPath("b"), force = true))
  }

  @Test(expected = classOf[CommandException])
  def testMoveNonExistent {
    append("a", "a")
    client.add(List(QPath("a")))
    client.move(QPath("ccc"), QPath("aaa"))
  }

  @Test
  def testMoveToFolder {
    append("a", "a")
    client.add(List(QPath("a")))
    new File(rootFolder, "c").mkdir()
    assertTrue(client.move(QPath("a"), QPath("c")))
  }
}

