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

import org.junit.Assert._
import org.junit.Test

@Test
class BasicRepositoryTest extends AbstractRepositoryTest {

  @Test
  def testRepositoryRoot {
    val path = repo.root()
    assertEquals(path.getCanonicalPath(), new File(".").getCanonicalPath())
  }

  @Test
  def testRepositoryStatus {
    val list: List[(QStatus, QPath)] = repo.status().toList
    //list.foreach(println(_))
  }

  @Test
  def testRepositoryAddEmpty {
    val done = repo.add(Nil)
    assertTrue(done)
  }

  @Test
  def testRepositoryAddNonExisting {
    assertFalse(repo.add(List(new QPath("src/newName.scala"))))
  }

  @Test
  def testRepositoryAddExisting {
    val done = repo.add(List(QPath(".hgignore")))
    assertTrue(done)
  }

  @Test
  def testRepositoryAddRemove {
    repo.addRemove(Nil, 100)
  }

  @Test
  def testRepositoryCat {
    val data: String = repo.cat(QPath(".hgignore"))
    assertTrue(data, data.contains("syntax: regexp"))
  }
}
