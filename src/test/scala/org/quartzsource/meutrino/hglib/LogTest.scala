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
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.client.AbstractClientTest

class LogTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    append("a", "a")
    val (rev1, node1) = client.commit("second")
    val reversed = client.log()
    val revs = reversed.reverse
    assertEquals(2, revs.size)
    assertEquals(node1, revs(1).node)
    assertEquals(node1, revs(1).getNode()) //Java style

    assertEquals(client.log(revRange = List("0"))(0), revs(0))

    assertEquals(client.log(), client.log(files = List(QPath("a"))))

    //TODO self.assertEquals(self.client.log(), self.client.log(hidden=True))
  }
}

