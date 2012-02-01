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

import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.quartzsource.meutrino.client.CommandServerConfig
import org.quartzsource.meutrino.client.CommandServerFactory
import org.quartzsource.meutrino.AbstractTest
import org.quartzsource.meutrino.QFactory
import org.quartzsource.meutrino.QRepository

//TODO move one package up
abstract class AbstractHglibTest extends AbstractTest {
  val rootFolder: File = getTestFolder()
  val factory = new RegisteredFactory()
  val client = factory.create(rootFolder)

  @Before
  def setUp {
    assertNotNull(client)
    println("Repo: " + rootFolder.getCanonicalPath())
  }

  @After
  def tearDown {
    factory.closeAll()
  }

  def append(fileName: String, content: String, folder: File = rootFolder) {
    write(fileName, content, true, folder)
  }

  def write(fileName: String, content: String, append: Boolean = false, folder: File = rootFolder) {
    val source = new File(folder, fileName)
    val out = new BufferedWriter(new FileWriter(source, append))
    out.write(content)
    out.close()
  }

  def read(fileName: String): String = {
    val source = new File(rootFolder, fileName)
    val input = new FileReader(source)
    val array: Array[Char] = new Array[Char](4096)
    val size = input.read(array, 0, 4096)
    if (size >= 4095) throw new RuntimeException("Too big test data")
    input.close()
    new String(array.take(size))
  }
}

class RegisteredFactory extends QFactory {
  val conf = Map(("ui" -> Map("username" -> "py4fun")))
  val factory = new CommandServerFactory("hg", CommandServerConfig(config = conf))
  val repositry: scala.collection.mutable.Map[String, QRepository] = scala.collection.mutable.Map.empty

  def create(path: File): QRepository = factory.create(path)

  def open(path: File): QRepository = {
    val result = factory.open(path)
    repositry.put(result.root().getAbsolutePath(), result)
    result
  }

  def clone(source: String, path: File, noupdate: Boolean = false,
    uncompressed: Boolean = false): QRepository = factory.clone(source, path, noupdate, uncompressed)

  def closeAll() {
    repositry.foreach { case (path, repo) => repo.close() }
  }
}

