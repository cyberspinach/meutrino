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

import java.io.InputStream
import java.io.File
import java.util.Date
import java.util.TimeZone
import org.quartzsource.meutrino._

class LocalRepository(commandServer: CommandServer) extends QRepository with JavaQRepository {
  val TEMPLATE = """{rev}\0{node}\0{tags}\0{branch}\0{author}\0{desc}\0{date}\0"""
  val path: File = root()

  override def toString = {
    "Repo at " + path
  }

  /**
   * Run Mercurial command
   * @param <A> - the result type
   * @param command - Mercurial command
   * @param args - arguments for Mercurial command
   * @param eh - ErrorHandler in case of non-0 return code
   * @param userOption - user input
   * @param f - function to translate tuple (output, error) to the result value
   * @return result value
   */
  def runCommand[A](command: String, args: List[String] = Nil,
    userInput: UserInput = new NoUserInput())(f: (Int, String, String) => A): A = {
    commandServer.runCommand[A](command :: args, userInput)(f)
  }

  def add(files: List[QPath]): Boolean = {
    runCommand[Boolean]("add", files.map(_.path))(processBoolean)
  }

  def addRemove(files: List[QPath], similarity: Int) {
    require(similarity >= 0)
    require(similarity <= 100)
    runCommand[Boolean]("addremove", List("--similarity", similarity.toString()) ++
      files.map(_.path))(processBoolean)
  }

  def annotate(file: QPath, rev: Option[Int] = None): List[(Int, String)] = {
    val data = runCommand[String]("annotate", List(file.path, "--verbose") ++ option("rev", rev))(processOutput)
    val lines: Array[(Int, String)] = data.split("\n").map(line => {
      val splitted = line.split(": ", 2) //apply the pattern only once
      val rev = splitted(0)
      val contents: String = splitted(1)
      (rev.toInt, contents)
    })
    lines.toList
  }

  def bookmark(name: String, rev: Option[Int], force: Boolean, delete: Boolean,
    inactive: Boolean, rename: Option[String]) {
    runCommand[String]("bookmark", List(name) ++ option("rev", rev) ++ option("force", force) ++
      option("delete", delete) ++ option("inactive", inactive) ++ option("rename", rename))(processOutput)
  }

  def bookmarks(): List[(QBookmark, Int, QNodeId)] = {
    val data = runCommand[String]("bookmarks", List("--debug"))(processOutput)
    if ("no bookmarks set" != data.trim()) {
      val result = data.split("\n").toList.map(line => {
        val (isCurrent, bookmark) = line.splitAt(3)
        val current = isCurrent.contains('*')
        val revTuple = parseTailRev(bookmark)
        (QBookmark(revTuple._1.trim, current), revTuple._2, revTuple._3)
      })
      result
    } else {
      Nil
    }
  }

  def branch(name: Option[String] = None, force: Boolean = false, clean: Boolean = false): QBranch = {
    require(!(name.isDefined && clean), "cannot use both name and clean")
    val data = runCommand[String]("branch", name.toList ++ option("force", force) ++ option("clean", clean))(processOutput).trim
    val branchName: String = name match {
      case Some(n) => n
      case None => if (!clean) data else data.drop(34)
    }
    QBranch(branchName)
  }

  def branches(active: Boolean, closed: Boolean): List[(QBranch, Int, QNodeId)] = {
    val data = runCommand[String]("branches", List("--debug") ++
      option("active", active) ++ option("closed", closed))(processOutput)
    val trimmed = data.trim
    val result = (trimmed == "") match {
      case true => Nil
      case false => {
        trimmed.split("\n").toList.map(line => {
          val splitted = line.reverse.split(":", 2) //split only once
          val nameRev = splitted(1).split(" ", 2)
          val name = nameRev(1).trim().reverse
          val rev: Int = nameRev(0).reverse.toInt
          val nodeData = splitted(0).reverse
          val inactive = nodeData.contains("inactive")
          val closed = nodeData.contains("closed")
          val node = if (inactive || closed) {
            nodeData.split(" ")(0)
          } else {
            nodeData
          }
          (QBranch(name, active = !inactive, closed = closed), rev, QNodeId(node))
        })
      }
    }
    result
  }

  def bundle(file: String, destRepo: Option[String]) = {
    val args = List(file) ++ destRepo.toList
    val data = runCommand[Boolean]("bundle", args)(processBoolean)
    data
  }

  def cat(file: QPath, revision: Option[QNodeId]): String = {
    runCommand[String]("cat", List(file.path) ++ option("rev", revision))(processOutput)
  }

  def clone(source: String = ".", dest: Option[String] = None, branch: Option[String] = None,
    updateRev: Option[String] = None) {
    val data = runCommand[String]("clone", List(source) ++ dest.toList ++ option("branch", branch) ++
      option("updaterev", updateRev))(processOutput)
    //println(data)
  }

  def commit(message: String, user: Option[String], logfile: Option[String], addRemove: Boolean,
    closebranch: Boolean, date: Option[Date]): (Int, QNodeId) = {
    val data = runCommand[String]("commit", List("--debug", "--message", message) ++
      option("user", user) ++ option("close-branch", closebranch) ++
      option("addremove", addRemove) ++
      dateOption(date) ++
      option("logfile", logfile))(processOutput)
    val lastLine = data.split("\n").last
    val tuple = parseTailRev(lastLine)
    (tuple._2, tuple._3)
  }

  def config(): (List[String], List[(Option[String], String, String, String)]) = {
    val nonStart = "none: "
    // 'ui.username=py4fun' => (ui, username, py4fun)
    def parseValue(content: String): (String, String, String) = {
      val sectionSplitted = content.split("""\.""", 2) //split only once
      val keySplitted = sectionSplitted(1).split("=", 2) //split only once
      (sectionSplitted(0), keySplitted(0), keySplitted(1))
    }
    // 'none: ui.username=py4fun' => (None, ui, username, py4fun)
    // 'filename:3: ui.username=py4fun' => (Some(filename:3), ui, username, py4fun)
    def splitValueLine(line: String): (Option[String], String, String, String) = {
      val tuple = line.startsWith(nonStart) match {
        case true => {
          val content = line.substring(nonStart.size)
          val (section, key, value) = parseValue(content)
          (None, section, key, value)
        }
        case false => {
          val fileSplitted = line.split(""":\d+: """, 2) //split only once
          val data = fileSplitted(1)
          val filename = line.dropRight(data.size + 2) //include the line number
          val (section, key, value) = parseValue(data)
          (Some(filename), section, key, value)
        }
      }
      tuple
    }
    val data = runCommand[String]("showconfig", List("--debug"))(processOutput)
    val fileStart = "read config from: "
    val (fileLines, lines) = data.trim.split("\n").toList.span(_.startsWith(fileStart))
    val files = fileLines.map(_.substring(fileStart.size)).toList
    val conf = lines.map(splitValueLine(_))
    (files, conf)
  }

  def copy(source: QPath, dest: QPath, after: Boolean = false, force: Boolean = false): Boolean = {
    val data = runCommand[Boolean]("copy", option("after", after) ++ option("force", force) ++
      List(source.path, dest.path))(processBoolean)
    data
  }

  def diff(files: List[QPath] = Nil, revs: List[QNodeId] = Nil, change: Option[QNodeId] = None,
    text: Boolean = false, git: Boolean = false, nodates: Boolean = false, showfunction: Boolean = false,
    reverse: Boolean = false, ignoreAllSpace: Boolean = false, ignoreSpaceChange: Boolean = false,
    ignoreBlankLines: Boolean = false, unified: Option[Int] = None, stat: Boolean = false,
    subrepos: Boolean = false): String = {
    if (!revs.isEmpty && change.isDefined) throw new IllegalArgumentException("cannot specify both change and rev")
    val data = runCommand[String]("diff", option("text", text) ++ option("git", git) ++ option("nodates", nodates) ++
      option("show-function", showfunction) ++ option("reverse", reverse) ++
      option("ignore-all-space", ignoreAllSpace) ++ option("ignore-space-change", ignoreSpaceChange) ++
      option("ignore-blank-lines", ignoreBlankLines) ++ option("unified", unified) ++
      option("stat", stat) ++ option("subrepos", subrepos) ++
      option("change", change) ++ option("rev", revs.map(_.node)) ++ files.map(_.path))(processOutput)
    data
  }

  def forget(files: List[QPath]): Boolean = {
    val data = runCommand[Boolean]("forget", files.map(_.path))(processBoolean)
    data
  }

  def grep(pattern: String): List[List[String]] = throw new UnsupportedOperationException("grep command is not yet implemented.")

  def heads(revs: List[QNodeId], startRev: Option[QNodeId], topological: Boolean,
    closed: Boolean): List[QRevision] = {
    val data = runCommand[String]("heads", option("rev", startRev) ++ option("topo", topological) ++
      option("closed", closed) ++ List("--template", TEMPLATE) ++
      revs.map(_.node)) {
      case (code: Int, output: String, input: String) => code match {
        case 0 => output
        case 1 => ""
        case _ => throw new CommandException(code, output, input)
      }
    }
    parseRevs(data)
  }

  def import_(patch: InputStream, strip: Option[Int] = None, force: Boolean = false,
    noCommit: Boolean = false, bypass: Boolean = false, exact: Boolean = false,
    importBranch: Boolean = false, message: Option[String] = None, date: Option[Date] = None,
    user: Option[String] = None, similarity: Option[Int] = None): String = {
    val batchInput = new UserInputStream(patch)
    val data = runCommand[String]("import", option("strip", strip) ++
      option("force", force) ++ option("no-commit", noCommit) ++ option("bypass", bypass) ++
      option("exact", exact) ++ option("import-branch", importBranch) ++
      option("message", message) ++ dateOption(date) ++
      option("user", user) ++ option("similarity", similarity) ++
      List("-"), batchInput)(processOutput)
    batchInput.close
    data
  }

  def incoming(revRange: Option[String] = None, path: Option[String] = None, force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[QRevision] = {
    val data = runCommand[List[QRevision]]("incoming", option("rev", revRange) ++ List("--template", TEMPLATE) ++
      option("force", force) ++ option("newest-first", newest) ++
      option("no-merges", noMerges) ++ option("subrepos", subrepos) ++
      option("branch", branch) ++ option("limit", limit) ++ path.toList)(processIncomingOutgoing)
    data
  }
  def incomingBookmarks(revRange: Option[String] = None, path: Option[String] = None, force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[(String, String)] = {
    val data = runCommand[List[(String, String)]]("incoming", option("rev", revRange) ++ List("--bookmarks", "--debug") ++
      option("force", force) ++ option("newest-first", newest) ++
      option("no-merges", noMerges) ++ option("subrepos", subrepos) ++
      option("branch", branch) ++ option("limit", limit) ++ path.toList)(processBookmarks)
    data
  }

  def log(files: List[QPath], revRange: List[String], follow: Boolean,
    date: Option[Date], copies: Boolean, keyword: List[String],
    removed: Boolean, user: List[String],
    branch: List[String], prune: List[QNodeId], hidden: Boolean,
    limit: Option[Int], noMerges: Boolean): List[QRevision] = {
    val data = runCommand[String]("log", option("rev", revRange) ++
      List("--template", TEMPLATE) ++
      option("follow", follow) ++
      dateOption(date) ++
      option("copies", copies) ++
      option("keyword", keyword) ++
      option("removed", removed) ++
      option("user", user) ++
      option("branch", branch) ++
      option("prune", prune.map(_.node)) ++
      option("hidden", hidden) ++
      option("limit", limit) ++
      option("no-merges", noMerges) ++
      files.map(_.path))(processOutput)
    parseRevs(data)
  }

  private def parseRevs(data: String): List[QRevision] = {
    val splitted = if (data.trim == "") Nil else data.trim.split('\u0000').toList
    val grouped = splitted.grouped(7).map(QRevision(_))
    grouped.toList
  }

  def manifest(rev: Option[QNodeId]): List[(QNodeId, String, Boolean, Boolean, QPath)] = {
    val data = runCommand[String]("manifest", "--debug" :: option("rev", rev))(processOutput)
    val result = data.split("\n").toList.map(line => {
      val node = line.substring(0, 40)
      val perm = line.substring(41, 44)
      val symlink = line.charAt(45) == '@'
      val executable = line.charAt(45) == '*'
      val path = line.substring(47)
      (QNodeId(node), perm, executable, symlink, QPath(path))
    })
    result
  }

  def manifestAll(): List[QPath] = {
    val data = runCommand[String]("manifest", List("--debug", "--all"))(processOutput)
    data.split("\n").toList.map(QPath(_))
  }

  def merge(rev: Option[QNodeId] = None, force: Boolean = false,
    interaction: Option[QInteractiveMerge], nonInteractive: Boolean = false) = {
    if (nonInteractive && interaction.isDefined) throw new IllegalArgumentException("nonineractive mode does not need user input")
    val userInput: UserInput = interaction match {
      case None => new NoUserInput()
      case Some(prompt) => new MergeInput(prompt)
    }
    runCommand[Boolean]("merge", option("noninteractive", nonInteractive) ++ option("force", force), userInput)(processBoolean)
  }

  def mergePreview(mergeRevision: QNodeId): List[QRevision] = {
    val revset = "ancestors(%s)-ancestors(.)".format(mergeRevision)
    val data = runCommand[String]("log",
      List("--rev", revset, "--template", TEMPLATE))(processOutput)
    parseRevs(data)
  }

  def move(source: QPath, dest: QPath, after: Boolean = false, force: Boolean = false, dryRun: Boolean = false): Boolean = {
    runCommand[Boolean]("move", option("after", after) ++ option("force", force) ++
      option("dry-run", dryRun) ++ List(source.path, dest.path))(processBoolean)
  }

  def outgoing(revRange: Option[String] = None, path: Option[String] = None, force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[QRevision] = {
    val data = runCommand[List[QRevision]]("outgoing", option("rev", revRange) ++ List("--template", TEMPLATE) ++
      option("force", force) ++ option("newest-first", newest) ++
      option("no-merges", noMerges) ++ option("subrepos", subrepos) ++
      option("branch", branch) ++ option("limit", limit) ++ path.toList)(processIncomingOutgoing)
    data
  }
  def outgoingBookmarks(revRange: Option[String] = None, path: Option[String] = None,
    force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[(String, String)] = {
    val data = runCommand[List[(String, String)]]("outgoing", option("rev", revRange) ++ List("--bookmarks", "--debug") ++
      option("force", force) ++ option("newest-first", newest) ++
      option("no-merges", noMerges) ++ option("subrepos", subrepos) ++
      option("branch", branch) ++ option("limit", limit) ++ path.toList)(processBookmarks)
    data
  }

  def parents(rev: Option[QNodeId] = None, file: Option[QPath]): List[QRevision] = {
    val data = runCommand[String]("parents", option("rev", rev) ++ List("--template", TEMPLATE) ++
      option(file))(processOutput)
    parseRevs(data)
  }

  def paths(): Map[String, String] = {
    val data = runCommand[String]("paths")(processOutput)
    data.trim match {
      case "" => Map.empty
      case content => {
        content.split("\n").map(_.trim).map(line => {
          val splitted = line.split(" = ")
          (splitted(0), splitted(1))
        }).toMap
      }
    }
  }

  def pull(source: Option[String] = None, rev: List[QNodeId] = Nil, update: Boolean = false,
    force: Boolean = false, bookmark: List[String] = Nil, branch: List[String] = Nil): Boolean = {
    val data = runCommand[Boolean]("pull", option(source) ++ option("rev", rev.map(_.node)) ++
      option("update", update) ++ option("force", force) ++
      option("bookmark", bookmark) ++ option("branch", branch))(processBoolean)
    data
  }

  def push(dest: Option[String] = None, rev: List[QNodeId] = Nil, force: Boolean = false,
    bookmark: List[String] = Nil, branch: List[String] = Nil,
    newBranch: Boolean = false): Boolean = {
    val data = runCommand[Boolean]("push", option(dest) ++ option("rev", rev.map(_.node)) ++
      option("force", force) ++ option("new-branch", newBranch) ++
      option("bookmark", bookmark) ++ option("branch", branch))(processBoolean)
    data
  }

  def remove(files: List[QPath]): Boolean = {
    runCommand[Boolean]("remove", List("--force") ++ files.map(_.path))(processBoolean)
  }

  def resolve(file: List[QPath] = Nil, all: Boolean = false,
    mark: Boolean = false, unmark: Boolean = false): Boolean = {
    val data = runCommand[Boolean]("resolve", file.map(_.path) ++
      option("all", all) ++ option("mark", mark) ++ option("unmark", unmark))(processBoolean)
    data
  }

  def resolveListFiles(file: List[QPath] = Nil, all: Boolean = false,
    mark: Boolean = false, unmark: Boolean = false): List[(QStatus, QPath)] = {
    val data = runCommand[String]("resolve", List("--list"))(processOutput)
    data.split("\n").toList.map(parseStateLine)
  }

  def root(): File = {
    runCommand[File]("root") {
      (code, output, error) => new File(processOutput(code, output, error).trim())
    }
  }

  def status(change: Option[QNodeId], ignored: Boolean = false, clean: Boolean = false,
    all: Boolean = false): List[(QStatus, QPath)] = {
    runCommand("status", "-0" :: option("change", change) ++
      option("ignored", ignored) ++ option("clean", clean) ++ option("all", all)) {
      (code, output, error) =>
        {
          val data = processOutput(code, output, error)
          val resources = data.split("\u0000").toList.filter(_.size != 0).map(line => parseStateLine(line))
          resources
        }
    }
  }

  def tag(name: String, rev: Option[QNodeId] = None, message: Option[String] = None,
    force: Boolean = false, local: Boolean = false, remove: Boolean = false,
    date: Option[Date] = None, user: Option[String] = None) {
    val data = runCommand[String]("tag", option("rev", rev) ++ option("message", message)
      ++ option("force", force) ++ option("local", local) ++ option("remove", remove) ++
      option("user", user) ++ dateOption(date) ++ List(name))(processOutput)
  }

  def tags(): List[(String, Int, QNodeId, Boolean)] = {
    val data = runCommand[String]("tags", List("--debug"))(processOutput)
    data.split("\n").toList.map(line => {
      val tagLocal = line.endsWith(" local")
      val line1 = if (tagLocal) line.dropRight(6) else line
      val (name, rev, node) = parseTailRev(line1)
      (name.trim, rev, node, tagLocal)
    })
  }

  def tip(): QRevision = {
    val data = runCommand[String]("tip", List("--template", TEMPLATE))(processOutput)
    val cset :: Nil = parseRevs(data)
    cset
  }

  def update(rev: Option[QNodeId] = None, clean: Boolean = false,
    check: Boolean = false, date: Option[Date] = None) = {
    if (clean && check) throw new IllegalArgumentException("clean and check cannot both be true")
    val data = runCommand[(Int, Int, Int, Int)]("update", option("clean", clean) ++ option("check", check) ++
      dateOption(date) ++ option("rev", rev)) {
      (code, output, error) =>
        {
          processBoolean(code, output, error) //check 0 or 1 returned
          /*
          merging a
          0 files updated, 0 files merged, 0 files removed, 1 files unresolved
          use 'hg resolve' to retry unresolved file merges
           */
          val filesPattern = """(\d+).+, (\d+).+, (\d+).+, (\d+)""".r
          val numbers = for (m <- filesPattern findFirstMatchIn output) yield (m.group(1), m.group(2), m.group(3), m.group(4))
          numbers match {
            case Some((updated,  merged, removed, unresolved)) => (updated.toInt,  merged.toInt, removed.toInt, unresolved.toInt)
            case None => throw new RuntimeException
          }
        }
    }
    data
  }

  lazy val version: QVersion = {
    val data = runCommand[String]("version", List("--quiet"))(processOutput)
    CommandServerFactory.parseVersion(data)
  }

  //Implementation
  private def parseStateLine(line: String): (QStatus, QPath) = {
    if (line == null || line.size < 3) {
      throw new IllegalArgumentException("Unexpected status: " + line);
    }
    if (line(1) != ' ') {
      throw new IllegalArgumentException("Unexpected status: " + line);
    }
    (parseState(line.head), new QPath(line.drop(2)))
  }

  private def parseState(char: Char): QStatus = char match {
    case 'A' => ADDED
    case 'C' => CLEAN
    case 'I' => IGNORED
    case '!' => MISSING
    case 'M' => MODIFIED
    case 'R' => REMOVED
    case ' ' => RENAMED
    case '?' => UNKNOWN
    case 'U' => UNRESOLVED
    case unknown => throw new IllegalArgumentException("Unexpected status char: #" + unknown)
  }

  private def apply(revRange: String): QChangeContext = {
    val cset :: Nil = log(revRange = List(revRange))
    new ChangeContext(this, cset)
  }

  def apply(node: QNodeId): QChangeContext = apply(node.node)

  def apply(rev: Int): QChangeContext = apply(rev.toString())

  def close() = {
    commandServer.stop
  }

  /**
   * '25:c63e17701437582cf68d77b8a44cdd876fb1495d' => (Int, QNodeId)
   */
  private def parseRev(value: String): (Int, QNodeId) = {
    require(value.contains(":"))
    val revSplitted = value.split(":", 2) //split only once
    (revSplitted(0).toInt, QNodeId(revSplitted(1)))
  }

  /**
   * 'prefix:      25:c63e17701437582cf68d77b8a44cdd876fb1495d' => (String, Int, QNodeId)
   */
  private def parseTailRev(value: String): (String, Int, QNodeId) = {
    val rev = value.reverse.takeWhile(_ != ' ').reverse
    val revTuple = parseRev(rev)
    (value.dropRight(rev.size), revTuple._1, revTuple._2)
  }

  private def option(name: String, value: Boolean): List[String] = {
    require(!name.startsWith("-"))
    if (value) List("--" + name) else Nil
  }

  private def option[A](name: String, value: Option[A]): List[String] = {
    require(!name.startsWith("-"))
    if (value.isDefined) List("--" + name, value.get.toString) else Nil
  }

  private def option[A](value: Option[A]): List[String] = {
    if (value.isDefined) List(value.get.toString) else Nil
  }

  private def dateOption(date: Option[Date]): List[String] = date match {
    case None => Nil
    case Some(d) => {
      val zone: TimeZone = TimeZone.getDefault()
      val offset = -zone.getOffset(d.getTime()) / 1000
      val formatted = "%s %s".format(d.getTime() / 1000, offset)
      List("--date", formatted)
    }
  }

  /**
   * 'rev', List(1,2) => List('--rev', 1, '--rev', 2)
   */
  private def option[A](name: String, value: List[String]): List[String] = {
    require(!name.startsWith("-"))
    value.flatMap(v => "--" + name :: v :: Nil)
  }

  private def processBoolean(code: Int, output: String, warning: String): Boolean = {
    if (code == 0 || code == 1) code == 0 else throw new CommandException(code, output, warning)
  }

  private def processOutput(code: Int, output: String, warning: String): String = {
    if (code == 0) output else throw new CommandException(code, output, warning)
  }

  /**
   * eat 2 lines header
   * comparing with other
   * searching for changes
   */
  private def eat2Lines(data: String): String = data.dropWhile(_ != '\n').tail.dropWhile(_ != '\n').tail

  private def processIncomingOutgoing(code: Int, output: String, warning: String): List[QRevision] = code match {
    case 1 => Nil
    case 0 => {
      val noHeader = eat2Lines(output)
      parseRevs(noHeader)
    }
    case _ => throw new CommandException(code, output, warning)
  }

  private def processBookmarks(code: Int, output: String, warning: String): List[(String, String)] = code match {
    case 1 => Nil
    case 0 => {
      val noHeader = eat2Lines(output)
      val splitted = if (noHeader.trim == "") Nil else noHeader.trim.split('\n').toList
      val bookmarks = splitted.map(line => {
        //bookmarks may contain spaces
        val t = line.trim.reverse.split(" ", 2).map(_.reverse)
        (t(1).trim, t(0).trim)
      }).filter { case (_, revision) =>
        //filter out debug info
        revision.matches("[0-9a-z]{40}")
      }
      bookmarks
    }
    case _ => throw new CommandException(code, output, warning)
  }
}

