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
import org.quartzsource.meutrino.IGNORED
import org.quartzsource.meutrino.RENAMED
import org.quartzsource.meutrino.ADDED
import org.quartzsource.meutrino.REMOVED
import org.quartzsource.meutrino.QPath
import org.quartzsource.meutrino.MODIFIED
import org.quartzsource.meutrino.MISSING
import org.quartzsource.meutrino.CLEAN
import java.io.File
import org.quartzsource.meutrino.UNKNOWN
import org.quartzsource.meutrino.client.AbstractClientTest

class StatusTest extends AbstractClientTest {

  @Test
  def testEmpty {
    assertEquals(Nil, client.status())
  }

  @Test
  def testOneOfEach {
    append(".hgignore", "ignored")
    append("ignored", "a")
    append("clean", "a")
    append("modified", "a")
    append("removed", "a")
    append("missing", "a")
    client.commit("first", addRemove = true)
    append("modified", "a")
    append("added", "a")
    client.add(List(QPath("added")))
    new File(rootFolder, "missing").delete()
    client.remove(List(QPath("removed")))
    append("untracked", "untracked")

    val l = List((MODIFIED -> QPath("modified")),
      (ADDED -> QPath("added")),
      (REMOVED -> QPath("removed")),
      (CLEAN -> QPath(".hgignore")),
      (CLEAN -> QPath("clean")),
      (MISSING -> QPath("missing")),
      (UNKNOWN -> QPath("untracked")),
      (IGNORED -> QPath("ignored")))

    val st = client.status(all = true)
    val diff = st.diff(l)
    assertEquals(diff.toString, 0, diff.size)
  }

  @Test
  def testCopy {
    append("source", "a")
    client.commit("first", addRemove = true)
    client.copy(QPath("source"), QPath("dest"))
    val l = List((ADDED -> QPath("dest")), (RENAMED -> QPath("source")), (CLEAN -> QPath("source")))
    assertEquals(l, client.status(all = true))
  }

  @Test
  def testCopyOriginSpace {
    append("s ource", "a")
    client.commit("first", addRemove = true)
    client.copy(QPath("s ource"), QPath("dest"))
    val l = List((ADDED -> QPath("dest")), (RENAMED -> QPath("s ource")), (CLEAN -> QPath("s ource")))
    assertEquals(l, client.status(all = true))
  }

}

