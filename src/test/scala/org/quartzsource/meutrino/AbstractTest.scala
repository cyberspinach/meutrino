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

import java.io.File
import java.util.Date
import org.quartzsource.meutrino.client.CommandServerFactory

abstract class AbstractTest {

  def getTempFolder: File = {
    val tmpFolder = new File(System.getProperty("java.io.tmpdir"))
    val folder = new File(tmpFolder, "meutrino_test")
    folder.mkdirs()
    folder
  }

  def getTestFolder(seed: Int = 0): File = {
    val path = getTempFolder
    val folder = new File(path + File.separator + getClass().toString.dropWhile(!_.isUpper) +
      "-" + System.currentTimeMillis().toString() + (if (seed > 0) s"-${seed}" else ""))
    folder.mkdir()
    folder
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}

abstract class AbstractRepositoryTest {
  val client = new CommandServerFactory("hg")
  val repo = client.open(new File("."))

  def tearDown() = {
    repo.close
  }
}
