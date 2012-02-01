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

import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

@Test
class FromDosTest extends AbstractTest {
  val rewrite = false

  @Test
  def testRepository {
    val files = getData(new File("."))
    files.foreach(name => rewrite(name))
  }

  def getData(folder: File): List[File] = {
    folder.listFiles.toList.flatMap(name => {
      name.isDirectory() match {
        case true => getData(name)
        case false => name.getPath() match {
          case n if (n.endsWith(".class")) => Nil
          case n if (n.endsWith(".jar")) => Nil
          case n if (n.endsWith(".project")) => Nil
          case n if (n.endsWith(".classpath")) => Nil
          case n if (n.endsWith(".scala_dependencies")) => Nil
          case n if (n.endsWith("hotspot.log")) => Nil
          case n if (isIn(n, "target")) => Nil
          case n if (isIn(n, "bin")) => Nil
          case n if (isIn(n, ".settings")) => Nil
          case n if (isIn(n, ".hg")) => Nil
          case n => List(name)
        }
      }
    })
  }

  def isIn(name: String, folder: String): Boolean = {
    val f = "%s%s%s".format(File.separator, folder, File.separator)
    name.contains(f)
  }

  def rewrite(file: File): Boolean = {
    try {
      val source = scala.io.Source.fromFile(file).mkString
      val processed = source.trim().split("\r\n").toList.mkString("\n") + "\n"
      if (source.indexOf("\r\n") > 0 && source != processed) {
        println("Found CRLF EOL in: " + file.getCanonicalPath())
        if (rewrite) {
          val os = new BufferedOutputStream(new FileOutputStream(file))
          os.write(processed.getBytes("UTF-8"))
          os.close()
          println("Fixed: " + file.getCanonicalPath())
        }
        true
      } else {
        false
      }
    } catch {
      case e: Exception => {
        println("Cannot read: " + file.getCanonicalPath())
        true
      }
    }
  }
}
