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
import org.quartzsource.meutrino.client.AbstractClientTest

class DiffTest extends AbstractClientTest {

  @Test
  def testBasic {
    append("a", "a\n")
    client.add(List(QPath("a")))
    val diff1 = """diff -r 000000000000 a
--- /dev/null
+++ b/a
@@ -0,0 +1,1 @@
+a
"""
    assertEquals(diff1, client.diff(noDates = true))
    assertEquals(diff1, client.diff(files = List(QPath("a")), noDates = true))
    val (rev0, node0) = client.commit("first")
    val diff2 = """diff -r 000000000000 -r %s a
--- /dev/null
+++ b/a
@@ -0,0 +1,1 @@
+a
""".format(node0.short)
    assertEquals(diff2, client.diff(change = Some(node0), noDates = true))
    append("a", "a\n")
    val (rev1, node1) = client.commit("second")
    val diff3 = """diff -r %s a
--- a/a
+++ b/a
@@ -1,1 +1,2 @@
 a
+a
""".format(node0.short)
    assertEquals(diff3, client.diff(revs = List(node0), noDates = true))
    val diff4 = """diff -r %s -r %s a
--- a/a
+++ b/a
@@ -1,1 +1,2 @@
 a
+a
""".format(node0.short, node1.short)
    assertEquals(diff4, client.diff(revs = List(node0, node1), noDates = true))
  }

  @Test
  def testBasicPlain {
    write(".hg/hgrc", "[defaults]\ndiff=--git\n")
    testBasic
  }
}

