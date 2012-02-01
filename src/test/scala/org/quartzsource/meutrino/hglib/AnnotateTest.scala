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

class AnnotateTest extends AbstractHglibTest {

  @Test
  def testBasic {
    append("a", "a\n")
    val node0 = client.commit("first", addRemove = true)
    append("a", "a\n")
    val node1 = client.commit("second")
    val annotation = client.annotate(new QPath("a"))
    assertEquals(2, annotation.size)
    assertEquals(List((0, "a"), (1, "a")), annotation)
  }

  @Test
  def testTwoColons {
    append("a", "a: b\n")
    val node0 = client.commit("first", addRemove = true)
    val annotation = client.annotate(new QPath("a"))
    assertEquals(List((0, "a: b")), annotation)
  }
}

