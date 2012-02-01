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

package org.quartzsource.meutrino

import org.junit.Assert._
import org.junit.Test
import org.quartzsource.meutrino.client.CommandServerFactory;

@Test
class CommandServerFactoryTest extends AbstractTest {

  @Test
  def testGetVersion {
    val client = new CommandServerFactory("hg")
    val version = client.getVersion()
    version match {
      case QVersion(1, minor, _) => assertEquals(9, minor)
      case QVersion(2, _, _) => // Ok
      case v => fail("Unsupported version: %s".format(v))
    }
  }

  @Test
  def testParseVersion {
    assertEquals(QVersion(2, 0, 0), CommandServerFactory.parseVersion("Mercurial Distributed SCM (version 2.0) \n"))
    assertEquals(QVersion(2, 0, 1), CommandServerFactory.parseVersion("Mercurial Distributed SCM (version 2.0.1) \n"))
    assertEquals(QVersion(1, 9, 2), CommandServerFactory.parseVersion("Mercurial Distributed SCM (version 1.9.2)\n"))
    assertEquals(QVersion(1, 9, 2), CommandServerFactory.parseVersion("Mercurial Distributed SCM (version 1.9.2)"))
    assertEquals(QVersion(21, 449, 99002), CommandServerFactory.parseVersion("Mercurial Distributed SCM (version 21.449.99002)"))
  }
}
