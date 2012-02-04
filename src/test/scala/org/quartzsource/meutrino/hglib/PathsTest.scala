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
import java.io.File
import org.quartzsource.meutrino.client.AbstractClientTest

class PathsTest extends AbstractClientTest {

  @Test
  def testBasic {
    append(".hg/hgrc", "[paths]\nfoo = bar\n")
    // hgrc isn't watched for changes yet, have to reopen
    val client = factory.open(rootFolder)
    val paths = client.paths()
    assertEquals(1, paths.size)
    val foo = new File(paths("foo")).getCanonicalPath()
    val etalon = new File(rootFolder, "bar").getCanonicalPath()
    assertEquals(etalon, foo)
  }
}

