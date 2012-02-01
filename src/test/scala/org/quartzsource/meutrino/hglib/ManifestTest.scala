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
import org.quartzsource.meutrino.QNodeId
import org.quartzsource.meutrino.QPath

class ManifestTest extends AbstractHglibTest {

  @Test
  def testBasic {
    append("a", "a")
    append("b", "b")
    val manifest = List(((QNodeId("047b75c6d7a3ef6a2243bd0e99f94f6ea6683597"), "644", false, false, QPath("a"))),
      ((QNodeId("62452855512f5b81522aa3895892760bb8da9f3f"), "644", false, false, QPath("b"))))
    client.commit("first", Some("foo"), addRemove = true)
    assertEquals(List(QPath("a"), QPath("b")), client.manifestAll())
    assertEquals(manifest, client.manifest())
  }
}

