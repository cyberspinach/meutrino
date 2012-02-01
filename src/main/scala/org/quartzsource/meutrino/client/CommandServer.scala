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
import java.nio.charset.Charset
import java.io.File
import java.io.InputStream
import org.quartzsource.meutrino.CommandServerException
import org.quartzsource.meutrino.ResponseException

private sealed trait Channel { def ch: Char }
private case class InputChannel(ch: Char, length: Int) extends Channel {
  if (length < 0) throw new CommandServerException("Unexpected length: %s".format(length))
}
private case class OutputChannel(ch: Char, data: Array[Byte]) extends Channel
private case class ReturnChannel(result: Int) extends Channel {
  val ch = 'r'
}
private case class OptionalChannel(ch: Char) extends Channel { //to be ignored
  require(ch.isLower)
}

class CommandServer(val path: File, process: Process, sync: Boolean = false) {
  require(path != null)
  require(process != null)
  val input = process.getInputStream()
  val error = process.getErrorStream()
  val outputStream = process.getOutputStream()
  val charSet = Charset.forName("UTF-8")
  readHelloMessage()

  private def readHelloMessage() {
    //hello message is one chunk
    readChannel() match {
      case OutputChannel('o', data) => {
        val message = new String(data, "UTF-8")
        val fields: Map[String, String] = message.split("\n").toList.map(line => {
          val parsedLine = line.split(":")
          (parsedLine(0) -> parsedLine(1).trim)
        }).toMap
        if (fields("encoding") != "UTF-8")
          throw new CommandServerException("Unexpected encoding: '%s'".format(fields("encoding")))
        if (!fields("capabilities").contains("runcommand"))
          throw new CommandServerException("'runcommand' capability is required.")
      }
      case error => throw new ResponseException("Unexpected hello message '%s'".format(error.toString()))
    }
  }

  private[this] def readChannel(): Channel = {
    def read(stream: InputStream, length: Int, errorMesage: String): Array[Byte] = {
      val data: Array[Byte] = new Array(length)
      val count = input.read(data)
      if (count != length) throw new CommandServerException(errorMesage)
      data
    }
    val struct = read(input, 5, "Unexpected end of channel struct.")
    val ch = struct(0).toChar
    val result = readInt(struct, 1)
    ch match {
      case 'I' | 'L' => InputChannel(ch, result)
      case 'o' => {
        val data = read(input, result, "Unexpected end of input channel data.")
        OutputChannel(ch, data)
      }
      case 'e' => {
        val data = read(error, result, "Unexpected end of output channel data.")
        OutputChannel(ch, data)
      }
      case 'r' => {
        val returnCode = readInt(read(input, result, "Unexpected end of result channel struct."), 0)
        ReturnChannel(returnCode)
      }
      case ch if ch.isUpper => throw new ResponseException("Unexpected data on required channel '%s'".format(ch))
      case ch => OptionalChannel(ch)
    }
  }

  private def rawCommand(args: List[String], userInput: UserInput): (Int, String, String) = {

    def writeBlock(data: Array[Byte]) {
      val l = data.length
      val bytes = Array[Byte]((l >>> 24).toByte, (l >>> 16).toByte, (l >>> 8).toByte, l.toByte)
      outputStream.write(bytes)
      outputStream.write(data)
      outputStream.flush()
    }

    def runChannel(out: Array[Byte], err: Array[Byte]): (Int, Array[Byte], Array[Byte]) = {
      val channel = readChannel()
      channel match {
        case ch: ReturnChannel => (ch.result, out, err)
        case ch: OptionalChannel => runChannel(out, err)
        case OutputChannel('o', data) => runChannel(out ++ data, err)
        case OutputChannel('e', data) => runChannel(out, err ++ data)
        case InputChannel(kind, length) => {
          val prompt = new String(out, charSet)
          val userData: Array[Byte] = kind match {
            case 'L' => userInput.readLine(length, prompt)
            case 'I' => userInput.readBatch(length, prompt)
          }
          writeBlock(userData)
          runChannel(out, err)
        }
      }
    }

    outputStream.write("runcommand\n".getBytes(charSet))
    writeBlock(args.mkString("\0").getBytes(charSet))
    val (code, resultOut, resultErr) = runChannel(Array[Byte](), Array[Byte]())
    (code, new String(resultOut, "UTF-8"), new String(resultErr, "UTF-8"))
  }

  //TODO do not use synchronized methods
  def runCommand[A](args: List[String], userInput: UserInput)(f: (Int, String, String) => A): A = {
    val (code, out, err) = if (sync) {
      this.synchronized {
        rawCommand(args, userInput)
      }
    } else {
      rawCommand(args, userInput)
    }
    f(code, out, err)
  }

  private[this] def readInt(struct: Array[Byte], start: Int): Int = {
    require(start == struct.length - 4)
    val ch1 = struct(start) & 255
    val ch2 = struct(start + 1) & 255
    val ch3 = struct(start + 2) & 255
    val ch4 = struct(start + 3) & 255
    (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0)
  }

  def stop() = {
    //close streams - input, output, error
    process.destroy()

    //should we block here ?
    process.waitFor()

    //println("CommandServer at %s stopped.".format(path.getCanonicalPath()))
  }
}
