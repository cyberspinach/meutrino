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
import org.quartzsource.meutrino.QChangeContext
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.QRevision
import org.quartzsource.meutrino.QRepository
import org.quartzsource.meutrino.QNodeId
import org.quartzsource.meutrino.QStatus
import org.quartzsource.meutrino.IGNORED
import org.quartzsource.meutrino.ADDED
import org.quartzsource.meutrino.REMOVED
import org.quartzsource.meutrino.MODIFIED
import org.quartzsource.meutrino.CLEAN
import scala.collection.immutable.TreeMap
import scala.collection.immutable.SortedMap

final class ChangeContext(repo: QRepository, cset: QRevision) extends QChangeContext {
  lazy val rev = cset.rev
  lazy val node: QNodeId = cset.node
  lazy val tags: List[String] = cset.tags.filterNot(_ == "tip")
  lazy val branch: String = cset.branch
  lazy val author: String = cset.author
  lazy val description: String = cset.desc
  lazy val date: String = throw new UnsupportedOperationException()

  def this(repo: QRepository, revset: String) = {
    this(repo, repo.log(revRange = List(revset)).head)
  }

  override def toString() = "<changectx %s>".format(node.node.substring(0, 12))

  override def equals(other: Any): Boolean = other match {
    case that: ChangeContext => rev == that.rev
    case _ => false
  }

  override def hashCode(): Int = rev.hashCode

  def status(ignored: Boolean = false, clean: Boolean = false): Map[QStatus, List[QPath]] = {
    val origin: List[(QStatus, QPath)] = repo.status(change = Some(node), ignored = ignored, clean = clean)
    val grouped = origin.groupBy(tuple => tuple._1) //group by status
    val stat = grouped.map { case (status, paths) => (status, paths.map(t => t._2)) } //remove status from values
    //TODO decide what to do with unknown, ignored and clean status
    stat
  }

  lazy val files = modified ++ added ++ removed
  lazy val modified = status().getOrElse(MODIFIED, Nil)
  lazy val added = status().getOrElse(ADDED, Nil)
  lazy val removed = status().getOrElse(REMOVED, Nil)

  def ignored = status(ignored = true).getOrElse(IGNORED, Nil)
  def clean = status(clean = true).getOrElse(CLEAN, Nil)

  lazy val manifest: SortedMap[QPath, QNodeId] = {
    val list: List[(QNodeId, String, Boolean, Boolean, QPath)] = repo.manifest(Some(node))
    val selection: List[(QPath, QNodeId)] = list.map { case (node, _, _, _, path) => (path, node) }
    TreeMap(selection: _*)
  }

  lazy val parents: List[QChangeContext] = {
    val par = repo.parents(rev = Some(node))
    if (par.isEmpty) {
      new ChangeContext(repo, QRevision()) :: Nil
    } else {
      par.map(parent => new ChangeContext(repo, parent))
    }
  }

  def p1: QChangeContext = parents.head
  def p2: QChangeContext = parents match {
    case _ :: parent2 :: Nil => parent2
    case _ => new ChangeContext(repo, QRevision())
  }

  lazy val bookmarks = {
    val books = repo.bookmarks().filter(bm => bm._2 == rev)
    books.map(_._1.name)
  }

  private def logChildren(name: String): List[QChangeContext] = {
    repo.log(revRange = List("%s(%s)".format(name, node))).map(new ChangeContext(repo, _))
  }

  def children: List[QChangeContext] = {
    logChildren("children")
  }

  def ancestors: List[QChangeContext] = {
    logChildren("ancestors")
  }

  def descendants: List[QChangeContext] = {
    logChildren("descendants")
  }

  def ancestor(node2: QNodeId): QChangeContext = {
    val revRange = List("ancestor(%s,%s)".format(node, node2.node))
    val cset :: Nil = repo.log(revRange = revRange).map(new ChangeContext(repo, _))
    cset
  }
}

