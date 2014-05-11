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

import scala.collection.immutable.SortedMap
import scala.beans.BeanProperty

case class QVersion(@BeanProperty major: Int, @BeanProperty minor: Int, @BeanProperty fix: Int) {
  require(major >= 1)
  require(minor >= 0)
  require(fix >= 0)

  override def toString(): String = s"${major}.${minor}.${fix}"
}

/**
 * Relative path of a file in the local repository
 */
case class QPath(@BeanProperty path: String) extends Ordered[QPath] {
  require(path != null)
  require(path.size > 0)
  require(path.head != File.separatorChar)

  override def toString: String = path
  override def compare(other: QPath): Int = {
    path compare other.path
  }
}

object QNodeId {
  val NodeRE = "[0-9a-f]{40}".r
  def isValid(node: String): Boolean  = {
    NodeRE.pattern.matcher(node).matches
  }
}
case class QNodeId(@BeanProperty node: String) {
  require(node != null)
  require(QNodeId.isValid(node), s"Wrong node: '${node}'")

  @BeanProperty lazy val short = node.substring(0, 12)

  override def toString(): String = node
}

sealed abstract class QStatus(val char: Char) {
  override def toString(): String = s"'${char}'"
}
case object ADDED extends QStatus('A')
case object CLEAN extends QStatus('C')
case object IGNORED extends QStatus('I')
case object MISSING extends QStatus('!')
case object MODIFIED extends QStatus('M')
case object REMOVED extends QStatus('R')
case object RENAMED extends QStatus(' ')
case object UNKNOWN extends QStatus('?')
case object UNRESOLVED extends QStatus('U')

case class QBranch(@BeanProperty name: String, @BeanProperty active: Boolean = true,
  @BeanProperty closed: Boolean = false) {
  require(name != null)

  override def toString(): String = name
}

case class QBookmark(@BeanProperty name: String, @BeanProperty active: Boolean) {
  require(name != null)

  override def toString(): String = name
}

case class QRevision(data: List[String]) {
  require(data != null)
  require(data.size == 7, s"Revision must contain 7 strings but was: ${data.size} (${data.mkString})")
  //BeanProperty annotation is used for happy Java developers
  @BeanProperty lazy val rev: Int = data(0).toInt
  @BeanProperty lazy val node: QNodeId = QNodeId(data(1))
  @BeanProperty lazy val tags: List[String] = data(2).split(" ").toList
  @BeanProperty lazy val branch: String = data(3)
  @BeanProperty lazy val author: String = data(4)
  @BeanProperty lazy val desc: String = data(5)
  @BeanProperty lazy val mdate: String = data(6)

  @BeanProperty lazy val date: Date = {
    // truncate the timezone
    val posixtime = mdate.split("""\.""", 2).head.toLong * 1000
    new Date(posixtime)
  }
  lazy val toInt: Int = rev

  override def toString(): String = s"<revision ${data.mkString(", ")}>"
}
object QRevision {
  /**
   * Create null revision
   */
  def apply(): QRevision = new QRevision(List("-1", "0000000000000000000000000000000000000000", "", "", "", "", "0.0"))
}

/**
 * A changecontext object makes access to data related to a particular
 * changeset convenient.
 */
trait QChangeContext {
  def rev: Int
  def node: QNodeId
  def hex: String = node.node
  def tags: List[String]
  def branch: String
  def author: String
  def description: String
  def date: Date
  def toInt: Int = rev
  def toBoolean: Boolean = rev != -1
  def toList: List[QPath] = manifest.map(_._1).toList
  def toMap: SortedMap[QPath, QNodeId] = manifest
  def status(ignored: Boolean = false, clean: Boolean = false): Map[QStatus, List[QPath]]
  def files: List[QPath]
  def modified: List[QPath]
  def added: List[QPath]
  def removed: List[QPath]
  def ignored: List[QPath]
  def clean: List[QPath]

  def manifest: SortedMap[QPath, QNodeId]

  def parents: List[QChangeContext]
  def p1: QChangeContext
  def p2: QChangeContext

  def bookmarks: List[String]

  /**
   * return contexts for each child changeset
   */
  def children: List[QChangeContext]
  def ancestors: List[QChangeContext]
  def descendants: List[QChangeContext]

  /**
   * return the ancestor context of self and node
   */
  def ancestor(node: QNodeId): QChangeContext
}

/**
 * controls the behaviour when Mercurial prompts what to do with regard
 * to a specific file, e.g. when one parent modified a file and the other
 * removed it. It is a function that gets a
 * single argument which is the contents of stdout. It should return one
 * of the expected choices (a single character).
 */
trait QInteractiveMerge {
  def getAnswerFor(text: String): Byte
}

//TODO QInterceptor is not used
trait QInterceptor {
  def report(code: Int, output: String, error: String): Unit
}
