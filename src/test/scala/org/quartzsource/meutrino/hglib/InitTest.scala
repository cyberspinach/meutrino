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
import org.junit.Assert.fail
import org.junit.Test
import org.apache.commons.io.FileUtils
import org.quartzsource.meutrino.CommandException
import java.io.File
import org.quartzsource.meutrino.client.AbstractClientTest

class InitTest extends AbstractClientTest {

  @Test(expected = classOf[CommandException])
  def testExists {
    factory.create(rootFolder)
  }

  @Test
  def testExistsMessage {
    try {
      factory.create(rootFolder)
      fail()
    } catch {
      case e: CommandException => {
        assertFalse(e.code == 0)
        assertEquals("", e.output)
        assertTrue(e.getMessage().startsWith("abort: repository"))
        assertTrue(e.getMessage().endsWith("already exists!"))
      }
    }
  }

  @Test
  def testBasic {
    client.close
    val hgFolder = new File(rootFolder, ".hg")
    assertTrue(hgFolder.exists)
    FileUtils.deleteDirectory(hgFolder)
    assertFalse(hgFolder.exists)
    val client2 = factory.create(rootFolder)
    val root = client2.root.getAbsolutePath()
    assertTrue(root, root.contains("InitTest"))
  }
}

