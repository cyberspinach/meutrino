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
import org.quartzsource.meutrino.CommandServerException
import org.quartzsource.meutrino.QInteractiveMerge
import org.quartzsource.meutrino.QPath

class MergeTest extends AbstractHglibTest {
  append("a", "a")
  val (rev0, node0) = client.commit("first", addRemove = true)
  append("a", "a")
  val (rev1, node1) = client.commit("change")

  @Test
  def testBasic {
    client.update(Some(node0))
    append("b", "a")
    val (rev2, node2) = client.commit("new file", addRemove = true)
    assertTrue(client.merge(Some(node1)))
    val (rev, node) = client.commit("first", addRemove = true)
    val diff = """diff -r %s -r %s a
--- a/a
+++ b/a
@@ -1,1 +1,1 @@
-a
\ No newline at end of file
+aa
\ No newline at end of file
""".format(node2.short, node.short)
    assertEquals(diff, client.diff(change = Some(node), noDates = true))
  }

  @Test(expected = classOf[CommandServerException])
  def testMergePromptAbort {
    client.update(Some(node0))
    client.remove(List(QPath("a")))
    client.commit("remove")
    client.merge()
  }

  @Test
  def testMergePromptNoninteractive {
    client.update(Some(node0))
    client.remove(List(QPath("a")))
    val (rev, node) = client.commit("remove")

    assertTrue(client.merge(nonInteractive = true))

    val diff = """diff -r %s a
--- /dev/null
+++ b/a
@@ -0,0 +1,1 @@
+aa
\ No newline at end of file
""".format(node.short)
    assertEquals(diff, client.diff(noDates = true))
  }

  @Test
  def testMergeInteractive {
    client.update(Some(node0))
    client.remove(List(QPath("a")))
    val (rev, node) = client.commit("remove")

    assertTrue(client.merge(interaction = Some(new CancelInteraction())))

    val diff = """diff -r %s a
--- /dev/null
+++ b/a
@@ -0,0 +1,1 @@
+aa
\ No newline at end of file
""".format(node.short)
    assertEquals(diff, client.diff(noDates = true))
  }

  private class CancelInteraction extends QInteractiveMerge {
    def getAnswerFor(text: String): Byte = 'c'
  }

  @Test
  def testMergePreview {
    client.update(Some(node0))
    client.remove(List(QPath("a")))
    val (rev, node) = client.commit("remove")
    val previewed = client.mergePreview(node1)
    assertEquals(1, previewed.size)
    assertEquals(node1, previewed.head.node)
    assertEquals("No merge must be done. Both parents must stay.", 2, client.heads().size)
  }
}

