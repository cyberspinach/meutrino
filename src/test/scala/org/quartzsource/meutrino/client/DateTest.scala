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

package org.quartzsource.meutrino.client

import org.junit.Assert._
import org.junit.Test
import java.util.Date

class DateTest extends AbstractClientTest {

  @Test
  def testDate {
    append("a", "a")
    val now = new Date()
    val (_, node) = client.commit("first", Some("foo"), addRemove = true, date = Some(now))
    val rev = client.log(revRange = List(node.node))(0)
    //drop miliseconds
    val millis = now.getTime() / 1000 * 1000
    assertEquals(new Date(millis), rev.date)
  }
}

