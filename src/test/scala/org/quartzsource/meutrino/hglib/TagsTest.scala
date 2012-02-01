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

class TagsTest extends AbstractHglibTest {

  @Test
  def testBasic {
    append("a", "a")
    val (rev, node) = client.commit("first", addRemove = true)
    client.tag("my tag")
    client.tag("local tag", rev = Some(node), local = true)
    if (client.version().major < 2) {
      //filecache that was introduced in 2.0 makes us see the local tag
    } else {
      val tags = client.tags()
      val etalon = List(("tip", 1, client.tip().node, false),
        ("my tag", 0, node, false),
        ("local tag", 0, node, true))
      assertEquals(etalon.toString(), tags.toString())
      assertEquals(etalon, tags)
    }
  }
}

