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
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import scala.collection.JavaConverters._
import org.quartzsource.meutrino._

/**
 * hg - full path to Mercurial executable
 * environment - global environment variables
 * useGlobalHgrcPath - when this is false then only the local settings in .hg/hgrc from the current repository is read.
 *
 * --ssh CMD           specify ssh command to use
 * --remotecmd CMD     specify hg command to run on the remote side
 * --insecure          do not verify server certificate (ignoring web.cacerts config)
 */
class CommandServerFactory(hg: String,
  workingDir: Option[File] = None,
  config: Map[String, Map[String, String]] = Map.empty,
  useGlobalHgrcPath: Boolean = false,
  environment: Map[String, String] = Map.empty,
  sync: Boolean = false) extends QFactory {
  val charSet = Charset.forName("UTF-8")
  def this(hg: String, workingDir: File, configMap: java.util.Map[String, java.util.Map[String, String]],
    useGlobalHgrcPath: Boolean, envMap: java.util.Map[String, String]) =
    this(hg,
      Some(workingDir),
      configMap.asScala.map {
        case (key, submap) => (key -> submap.asScala.toMap)
      }.toMap,
      useGlobalHgrcPath,
      envMap.asScala.toMap)

  def create(path: File): QRepository = {
    val args: List[String] = List("init", path.getCanonicalPath())
    val output = executeCommand(args, workingDir)
    open(path)
  }

  def open(path: File): QRepository = {
    if (!new File(path, ".hg").isDirectory()) {
      throw new IllegalStateException("No .hg repository found in " + path.getCanonicalPath())
    }
    val process = launch(List("serve", "--cmdserver", "pipe", "--config", "ui.interactive=True") ++
      configToArguments(config), Some(path))
    getVersion() match {
      case v @ QVersion(1, minor, _) if minor < 9 =>
        throw new CommandServerException("CommandServer is not supported before 1.9: " + v)
      case _ => //Ok
    }
    val serverProcess = new CommandServer(path, process, sync)
    new LocalRepository(serverProcess)
  }

  def clone(source: String, path: File, noupdate: Boolean,
    uncompressed: Boolean): QRepository = {
    val args: List[String] = List("clone", "--pull") ++ (if (noupdate) List("--noupdate") else Nil) ++
      (if (uncompressed) List("--uncompressed") else Nil) ++ List(source, path.getCanonicalPath())
    val output = executeCommand(args, workingDir)
    open(path)
  }

  private[this] def launch(arguments: List[String], path: Option[File]): Process = {
    val args = new java.util.ArrayList[String](arguments.size + 1)
    args.add(hg)
    args.addAll(arguments.asJava)
    val processBuilder = new ProcessBuilder(args)
    path.map(processBuilder.directory(_))
    val env: java.util.Map[String, String] = processBuilder.environment()
    //see http://selenic.com/hg/file/3e13ade423f0/mercurial/help/environment.txt
    //disables any configuration settings that might change Mercurial's default output.
    //(the value is not important)
    env.put("HGPLAIN", "True")
    //This overrides the default locale setting detected by Mercurial,
    //UTF-8 is always used
    env.put("HGENCODING", charSet.displayName)
    if (!useGlobalHgrcPath) {
      //only the .hg/hgrc from the current repository is read.
      env.put("HGRCPATH", "")
    }
    environment.foreach { case (key, value) => env.put(key, value) }
    processBuilder.start()
  }

  private[this] def configToArguments(config: Map[String, Map[String, String]]): List[String] = {
    val list = config.flatMap {
      case (section, keyValueMap) => keyValueMap.map {
        case (key, value) => section + "." + key + "=" + value
      }
    }
    list.flatMap("--config" :: _ :: Nil).toList
  }

  def executeCommand(arguments: List[String], path: Option[File] = None): (String, String) = {

    def read(input: InputStream): String = {
      val reader = new BufferedReader(new InputStreamReader(input, charSet))
      Stream.continually(reader.read()).takeWhile(_ != -1).map(_.toChar).toList.mkString
    }

    val process = launch(arguments, path)
    val error = read(process.getErrorStream)
    val input = read(process.getInputStream)

    val processCode = process.waitFor()
    if (processCode != 0) {
      throw new CommandException(processCode, input.trim, error.trim);
    }
    (input, error)
  }

  def getVersion(): QVersion = {
    val (output, _) = executeCommand(List("version", "--quiet"), workingDir)
    CommandServerFactory.parseVersion(output)
  }
}
object CommandServerFactory {
  def parseVersion(output: String): QVersion = {
    val VersionEntry = """(.+)\(version(.+)\)\s*""".r
    output match {
      case VersionEntry(_, version) => {
        val splitted = version.split("""\.""")
        val major = splitted(0).trim.toInt
        val minor = splitted(1).trim.toInt
        val micro = if (splitted.size > 2) splitted(2).trim.toInt else 0
        QVersion(major, minor, micro)
      }
      case _ => throw new RuntimeException("Unexpected output with version: " + output)
    }
  }
}
