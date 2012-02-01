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

import java.io.File
import org.junit.Test
import org.junit.Assert._
import org.quartzsource.meutrino.client.CommandServerFactory
import org.quartzsource.meutrino.hglib.AbstractHglibTest
import org.quartzsource.meutrino.client.CommandServerConfig

@Test
class StressTest extends AbstractHglibTest {

  @Test
  def testDifferentRepositories {
    val workers = (1 to 5).map(i => {
      val rootFolder: File = getTestFolder(i)
      val repo = factory.create(rootFolder)
      new WriterWorker(i, repo)
    })
    workers.map(worker => {
      val thread = new Thread(worker)
      thread.start()
      thread
    }).foreach(_.join)
    assertTrue(workers.forall(_.success))
  }

  private trait Result {
    def success: Boolean
  }
  private class WriterWorker(number: Int, repo: QRepository) extends Runnable with Result {
    var success = false

    def run() {
      val start = repo.log().size
      for (i <- 1 to 5) {
        append("a", "a\n", folder = repo.root())
        repo.commit("first " + number, Some("foo"), addRemove = true)
      }
      val end = repo.log().size
      assertEquals(start + 5, end)
      for (i <- 1 to 10) repo.log()
      success = true //no exception
    }
  }
  private class ReaderWorker(number: Int, repo: QRepository) extends Runnable with Result {
    var success = false

    def run() {
      for (i <- 1 to 100) repo.log()
      success = true //no exception
    }
  }

  /**
   * many thread may not use the same instance of repository
   * (the communication over the pipe becomes broken)
   */
  @Test
  def testSameRepository {
    //assertFalse(sameRepo(false)) //no lock
    assertTrue(sameRepo(true)) //sync with lock
  }

  def sameRepo(syncWithLock: Boolean): Boolean = {
    val rootFolder: File = getTestFolder()
    val conf = Map(("ui" -> Map("username" -> "py4fun")))
    val factory = new CommandServerFactory("hg", new CommandServerConfig(config = conf, sync = syncWithLock))
    val repo = factory.create(rootFolder)

    val writer = new WriterWorker(1, repo)
    val workers = writer :: (2 to 5).map(i => {
      new ReaderWorker(i, repo)
    }).toList
    workers.map(worker => {
      val thread = new Thread(worker)
      thread.start()
      thread
    }).foreach(_.join)
    workers.forall(_.success)
  }
}
