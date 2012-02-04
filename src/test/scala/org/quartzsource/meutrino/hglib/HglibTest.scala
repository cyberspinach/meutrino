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
import org.quartzsource.meutrino.client.CommandServerFactory
import org.quartzsource.meutrino.client.AbstractClientTest

class HglibTest extends AbstractClientTest {

  @Test
  def testCloseFds {
    //open a second instance of CommandServer for the same repository
    val client2 = new CommandServerFactory("hg").open(rootFolder)
    client.close
    client2.close
  }
}

