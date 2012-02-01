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

import java.io.ByteArrayInputStream

import org.junit.Assert.assertEquals
import org.junit.Test
import org.quartzsource.meutrino.client.UserInputStream

@Test
class UserInputStreamTest extends AbstractTest {

  private def processBatch(patch: String, size: Int): String = {
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result = input.readBatch(size, "Give me something:")
    new String(result, "UTF-8")
  }

  @Test
  def testBatch {
    assertEquals("1234567890", processBatch("1234567890", 4096))
    assertEquals("1234567890\n", processBatch("1234567890\n", 4096))
    assertEquals("12345", processBatch("1234567890\n", 5))
    assertEquals("", processBatch("", 5))
    assertEquals("\n\n", processBatch("\n\n", 5))
  }

  @Test
  def testBatchLong1 {
    val patch = "1234567890\n"
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result1 = input.readBatch(5, "Give me something:")
    assertEquals("12345", new String(result1, "UTF-8"))
    val result2 = input.readBatch(5, "Give me something:")
    assertEquals("67890", new String(result2, "UTF-8"))
    val result3 = input.readBatch(5, "Give me something:")
    assertEquals("\n", new String(result3, "UTF-8"))
    val result4 = input.readBatch(7, "Give me something:")
    assertEquals("", new String(result4, "UTF-8"))
  }

  @Test
  def testBatchLong2 {
    val patch = "1234567890\n"
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result1 = input.readBatch(7, "Give me something:")
    assertEquals("1234567", new String(result1, "UTF-8"))
    val result2 = input.readBatch(7, "Give me something:")
    assertEquals("890\n", new String(result2, "UTF-8"))
    val result3 = input.readBatch(7, "Give me something:")
    assertEquals("", new String(result3, "UTF-8"))
  }

  private def processLine(patch: String, size: Int): String = {
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result = input.readLine(size, "Give me something:")
    new String(result, "UTF-8")
  }

  @Test
  def testOneLine {
    assertEquals("1234567890", processLine("1234567890", 4096))
    assertEquals("1234567890\n", processLine("1234567890\n", 4096))
    assertEquals("12345", processLine("1234567890\n", 5))
    assertEquals("", processLine("", 5))
    assertEquals("\n", processLine("\n", 5))
    assertEquals("\n", processLine("\n\n", 5))
  }

  @Test
  def testManyLines {
    assertEquals("1234567890\n", processLine("1234567890\naaa\n", 4096))
  }

  @Test
  def testLineLong1 {
    val patch = "1234567890\n"
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result1 = input.readLine(5, "Give me something:")
    assertEquals("12345", new String(result1, "UTF-8"))
    val result2 = input.readLine(5, "Give me something:")
    assertEquals("67890", new String(result2, "UTF-8"))
    val result3 = input.readLine(5, "Give me something:")
    assertEquals("\n", new String(result3, "UTF-8"))
    val result4 = input.readLine(5, "Give me something:")
    assertEquals("", new String(result4, "UTF-8"))
  }

  @Test
  def testLineLong2 {
    val patch = "1234567890\naaaa\n"
    val input = new UserInputStream(new ByteArrayInputStream(patch.getBytes()))
    val result1 = input.readLine(4096, "Give me something:")
    assertEquals("1234567890\n", new String(result1, "UTF-8"))
    val result2 = input.readLine(4096, "Give me something:")
    assertEquals("aaaa\n", new String(result2, "UTF-8"))
    val result3 = input.readLine(4096, "Give me something:")
    assertEquals("", new String(result3, "UTF-8"))
    val result4 = input.readLine(4096, "Give me something:")
    assertEquals("", new String(result4, "UTF-8"))
  }
}
