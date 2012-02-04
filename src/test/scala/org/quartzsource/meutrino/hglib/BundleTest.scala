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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.quartzsource.meutrino.client.AbstractClientTest

class BundleTest extends AbstractClientTest {
  @Test
  def testNoChanges {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    assertFalse(client.bundle("bundle", destRepo = Some(".")))
  }

  @Test
  def testBasic {
    append("a", "a")
    val (rev0, node0) = client.commit("first", addRemove = true)
    client.clone(dest = Some("other"))
    append("a", "a")
    val (rev1, node1) = client.commit("second")
    assertTrue(client.bundle("bundle", destRepo = Some("other")))
  }
}

