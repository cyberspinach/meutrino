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
import org.quartzsource.meutrino.client.CommandServerFactory
import java.util.Date

class CommitTest extends AbstractHglibTest {

  @Test
  def testUser {
    append("a", "a")
    val (_, node) = client.commit("first", Some("foo"), addRemove = true)
    val rev = client.log(revRange = List(node.node))(0)
    assertEquals("foo", rev.author)
  }

  @Test(expected = classOf[CommandException])
  def testNoUser {
    client.close
    val withoutAuthor = new CommandServerFactory("hg").open(rootFolder)
    append("a", "a")
    withoutAuthor.commit("first", None, addRemove = true)
  }

  @Test(expected = classOf[CommandException])
  def testEmptyUser {
    client.close
    val withoutAuthor = new CommandServerFactory("hg").open(rootFolder)
    append("a", "a")
    withoutAuthor.commit("first", Some(""), addRemove = true)
  }

  @Test
  def testCloseBranch {
    append("a", "a")
    val (_, node0) = client.commit("first", addRemove = true)
    client.branch(Some("foo"))
    append("a", "a")
    val (_, node1) = client.commit("second")
    val revClose = client.commit("closing foo", closeBranch = true)
    val args: List[String] = List(node0.node, node1.node, revClose._2.node)
    val rev0 :: rev1 :: revisionClose :: Nil = client.log(revRange = args)
    val branches = client.branches().map(b => (b._1.name, b._2, b._3)) //QBranch -> name
    val branchData = (rev0.branch, rev0.rev, rev0.node)
    assertEquals(branches, List(branchData))

    val withClosed = client.branches(closed = true).map(b => (b._1.name, b._2, b._3)) //QBranch -> name
    assertEquals(withClosed, (revisionClose.branch, revisionClose.rev, revisionClose.node) :: branchData :: Nil)
  }

  @Test(expected = classOf[CommandException])
  def testMessageLogfile1 {
    client.commit("foo", logfile = Some("bar"))
  }

  @Test(expected = classOf[CommandException])
  def testMessageLogfile2 {
    client.commit("foo")
  }

  @Test
  def testDate {
    append("a", "a")
    val now = new Date()
    val (_, node) = client.commit("first", Some("foo"), addRemove = true, date = Some(now))
    val rev = client.log(revRange = List(node.node))(0)
    //drop miliseconds
    val millis = now.getTime() / 1000 * 1000
    assertEquals(new Date(millis), rev.date)
  }
}

