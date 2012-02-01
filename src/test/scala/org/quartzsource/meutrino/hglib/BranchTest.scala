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
import org.quartzsource.meutrino.QBranch

class BranchTest extends AbstractHglibTest {
  @Test
  def testEmpty {
    assertEquals(QBranch("default", true, false), client.branch())
  }

  @Test
  def testBasic {
    assertEquals(QBranch("foo"), client.branch(Some("foo")))
    append("a", "a")
    val (rev0, node) = client.commit("first", addRemove = true)
    val changeSet = client.log(revRange = List(node.node)).head
    assertEquals("foo", changeSet.branch)
    assertEquals(List((QBranch(changeSet.branch, true, false), changeSet.rev, changeSet.node)), client.branches())
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testResetWithName {
    client.branch(Some("foo"), clean = true)
    fail()
  }

  @Test
  def testReset {
    client.branch(Some("foo"))
    assertEquals("default", client.branch(clean = true).name)
  }

  @Test(expected = classOf[CommandException])
  def testExists {
    append("a", "a")
    client.commit("first", addRemove = true)
    client.branch(Some("foo"))
    append("a", "a")
    client.commit("second")
    client.branch(Some("default"))
    fail()
  }

  @Test
  def testForce {
    append("a", "a")
    client.commit("first", addRemove = true)
    client.branch(Some("foo"))
    append("a", "a")
    client.commit("second")
    assertEquals("default", client.branch(Some("default"), force = true).name)
  }
}

