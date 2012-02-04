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
import org.quartzsource.meutrino.QBranch
import org.quartzsource.meutrino.client.AbstractClientTest

class BranchesTest extends AbstractClientTest {
  @Test
  def testEmpty {
    assertEquals(Nil, client.branches())
  }

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    client.branch(Some("foo"))
    append("a", "a")
    val (rev1, node1) = client.commit("second")
    val branches = client.branches()
    val cset0 = client.log(revRange = List(node0.node)).head
    val cset1 = client.log(revRange = List(node1.node)).head
    assertEquals((QBranch(cset0.branch, false, false), cset0.rev, cset0.node).toString(), branches(1).toString())
    assertEquals((QBranch(cset1.branch, true, false), cset1.rev, cset1.node).toString(), branches(0).toString())
  }
}

