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
import org.quartzsource.meutrino.ADDED
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.client.AbstractClientTest

class CopyTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)

    assertTrue(client.copy(QPath("a"), QPath("b")))
    assertEquals(List((ADDED, QPath("b"))), client.status())
    append("c", "a")
    assertTrue(client.copy(QPath("a"), QPath("c"), after = true))
    assertEquals(List((ADDED, QPath("b")), (ADDED, QPath("c"))), client.status())
  }
}

