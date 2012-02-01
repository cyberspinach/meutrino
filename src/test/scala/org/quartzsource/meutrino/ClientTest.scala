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

import java.io.File
import java.util.Date

@Test
class ClientTest extends AbstractTest {

  @Test
  def testCreate {
    val client = new CommandServerFactory("hg")
    val rootFolder = getTestFolder()
    val repo = client.create(rootFolder)
    val data = Array("line1", "line2")
    printToFile(new File(rootFolder, "file1.txt"))(p => {
      data.foreach(p.println)
    })
    assertTrue(repo.addRemove(Nil, 100))
    val (_, revision) = repo.commit("Add file1.txt", Some("py4fun"), addRemove = false, date = new Date())
    //println(revision.node)
    repo.close
    //clone
    val cloneFolder = getTestFolder()
    assertFalse(new File(cloneFolder, "file1.txt").exists())
    val repo2 = client.clone(rootFolder.getCanonicalPath(), cloneFolder, false, false)
    assertTrue(new File(cloneFolder, "file1.txt").exists())
    repo2.close
    //no update
    val noupdateFolder = getTestFolder()
    val noupdateRepo = client.clone(rootFolder.getCanonicalPath(), noupdateFolder, true, false)
    assertFalse(new File(noupdateFolder, "file1.txt").exists())
    noupdateRepo.close
  }

}
