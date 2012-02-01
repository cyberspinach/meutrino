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
import org.quartzsource.meutrino.QFactory
import org.quartzsource.meutrino.client.CommandServerFactory
import org.quartzsource.meutrino.client.CommandServerConfig

class ConfigTest extends AbstractHglibTest {

  @Test
  def testBasic {
    client.close()
    write(".hg/hgrc", "[section]\nkey=value\n")
    val reopen = factory.open(rootFolder)
    val (files, config) = reopen.config()
    assertEquals(config.mkString("\n"), 1, config.count(t => t match {
      case (Some(file), "section", "key", "value") if (file.endsWith("hgrc:2")) => true
      case _ => false
    }))
    assertTrue("No files must be used.", files.isEmpty)
    assertTrue("Only our custom setting must be found,", config.count(t => t._1.isDefined) == 1)
  }

  @Test
  def testGlobalPath {
    client.close()
    write(".hg/hgrc", "[section]\nkey=value\n")
    val factory2: QFactory = new CommandServerFactory("hg", new CommandServerConfig(useGlobalHgrcPath = true))
    val reopen = factory2.open(rootFolder)
    val (files, config) = reopen.config()
    assertEquals(1, config.count(t => t match {
      case (Some(file), "section", "key", "value") if (file.endsWith("hgrc:2")) => true
      case _ => false
    }))
    assertFalse(files.isEmpty)
    assertTrue(config.count(t => t._1.isDefined) > 1)
  }

  @Test
  def testCistomConfig {
    client.close()
    write(".hg/hgrc", "[section]\nkey=value\n")
    val conf = Map(("ui" -> Map("username" -> "py4fun")))
    val factory2: QFactory = new CommandServerFactory("hg", new CommandServerConfig(config = conf))
    val reopen = factory2.open(rootFolder)
    val (files, config) = reopen.config()
    assertEquals(2, config.count(t => t match {
      case (_, "section", "key", "value") => true
      case (None, "ui", "username", "py4fun") => true
      case _ => false
    }))
  }
}

