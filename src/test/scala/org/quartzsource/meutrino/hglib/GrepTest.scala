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

import org.junit.Test
import org.quartzsource.meutrino.client.AbstractClientTest
import org.quartzsource.meutrino.{GrepReply, QPath}
import org.junit.Assert._

class GrepTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a\n")
    append("b", "ab\n")
    client.commit("first", addRemove = true)

    //no match
    assertEquals(Nil, client.grep("c"))

    assertEquals(List(GrepReply("a", "0", List("a")), GrepReply("b", "0", List("ab"))), client.grep("a"))

    assertEquals(List(GrepReply("a", "0", List("a"))), client.grep("a", List(QPath("a"))))

    assertEquals(List(GrepReply("b", "0", List("ab"))), client.grep("b"))
  }

  @Test
  def testOptions {
    append("a", "a\n")
    append("b", "ab\n")
    client.commit("first", addRemove = true, user = Some("test"))

    assertEquals(List(GrepReply("a", "0", List("+", "a")), GrepReply("b", "0", List("+", "ab"))),
      client.grep("a", all = true))

    assertEquals(List(GrepReply("a", "0", Nil), GrepReply("b", "0", Nil)),
      client.grep("a", filesWithMatches = true))

    assertEquals(List(GrepReply("a", "0", List("1", "a")), GrepReply("b", "0", List("1", "ab"))),
      client.grep("a", line = true))

    assertEquals(List(GrepReply("a", "0", List("test", "a")), GrepReply("b", "0", List("test", "ab"))),
      client.grep("a", user = true))

    assertEquals(List(GrepReply("a", "0", List("1", "+", "test")), GrepReply("b", "0", List("1", "+", "test"))),
      client.grep("a", all = true, user = true, line = true, filesWithMatches = true))
  }
}

