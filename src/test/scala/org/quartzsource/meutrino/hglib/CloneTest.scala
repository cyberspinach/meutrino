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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File
import org.quartzsource.meutrino.client.AbstractClientTest

class CloneTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    val repo = factory.clone(rootFolder.getCanonicalPath(), new File(rootFolder, "cloned"))
    assertFalse(client.root() == repo.root())
    val etalon = client.log(revRange = List(rev0.toString())).head
    val cs = repo.log(revRange = List(rev0.toString())).head
    assertEquals(etalon.toString(), cs.toString())
    assertEquals(client.log(), repo.log())
  }
}

