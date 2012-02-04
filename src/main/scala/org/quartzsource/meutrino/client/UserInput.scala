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

package org.quartzsource.meutrino.client
import org.quartzsource.meutrino.CommandServerException;
import org.quartzsource.meutrino.QInteractiveMerge
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * prompt is used to reply to prompts by the server It receives the max number
 * of bytes to return and the contents of stdout received so far
 *
 * input is used to reply to bulk data requests by the server. It receives the
 * max number of bytes to return
 *
 */
trait UserInput {
  /**
   * Provide line based user input for the given question in the interactive mode.
   * (for the 'L' channel)
   * @param size
   *            - the maximum size of the reply in bytes
   * @param text
   *            - the content of the output, it should contain the question to
   *            be answered.
   * @return the reply to be send to Mercurial
   */
  def readLine(size: Int, text: String): Array[Byte]
  /**
   * Provide plain data in the batch mode.
   * (for the 'I' channel)
   * @param size
   *            - the maximum size of the reply in bytes
   * @param text
   *            - the content of the output, it should contain the question to
   *            be answered.
   * @return the data to be send to Mercurial
   */

  def readBatch(size: Int, text: String): Array[Byte]
}

class UserInputStream(data: InputStream) extends UserInput {
  val buffered = new BufferedInputStream(data)

  def readBatch(size: Int, prompt: String): Array[Byte] = {
    val buffer = new Array[Byte](size)
    val count = buffered.read(buffer)
    //Arrays.copyOf(buffer, count) is this more efficient ?
    buffer.take(count)
  }

  //TODO mutable local data !
  def readLine(size: Int, prompt: String): Array[Byte] = {
    val out = new ByteArrayOutputStream(size)
    var ch: Int = buffered.read()
    while (ch != -1) {
      out.write(ch)
      if (ch == '\n') return out.toByteArray()
      if (out.size == size) return out.toByteArray()
      ch = buffered.read()
    }
    out.toByteArray()
  }

  def close = buffered.close()
}

class NoUserInput extends UserInput {
  def readBatch(size: Int, prompt: String) = throw new CommandServerException("Unexpected input channel.")
  def readLine(size: Int, prompt: String) = throw new CommandServerException("Unexpected input channel.")
}

class MergeInput(human: QInteractiveMerge) extends UserInput {

  def readBatch(size: Int, prompt: String): Array[Byte] = {
    ask(prompt)
  }

  def readLine(size: Int, prompt: String): Array[Byte] = {
    ask(prompt)
  }

  private def ask(text: String): Array[Byte] = {
    val result = new Array[Byte](2)
    result(0) = human.getAnswerFor(text)
    result(1) = '\n'
    result
  }
}
