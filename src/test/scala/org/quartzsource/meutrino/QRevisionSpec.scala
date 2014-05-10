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

import org.scalatest.FunSuite
import scala.collection.mutable.Stack
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FunSpec
import java.util.Date

@RunWith(classOf[JUnitRunner])
class QRevisionSpec extends FunSpec with Matchers {

  describe("A QRevision") {

    it("should accept proper values") {
      val rev = QRevision(List("1", "1234567890aaaaaaaaaa1234567890bbbbbbbbbb", "tip",
        "default", "py4fun", "foo\nbar", "1326905676.0-3600"))
      rev should have(
        'rev(1),
        'node(QNodeId("1234567890aaaaaaaaaa1234567890bbbbbbbbbb")),
        'tags(List("tip")),
        'branch("default"),
        'author("py4fun"),
        'desc("foo\nbar"),
        'date(new Date(1326905676L * 1000L)),
        'mdate("1326905676.0-3600"))
    }

    it("should throw IllegalArgumentException if an empty list was provided") {
      val thrown = intercept[IllegalArgumentException] {
        QRevision(Nil)
      }
      thrown.getMessage should include("Revision must contain 7 strings")
    }

    it("should throw IllegalArgumentException for short list") {
      val thrown = intercept[IllegalArgumentException] {
        QRevision(List("1", "1234567890aaaaaaaaaa1234567890bbbbbbbbbb", "tip"))
      }
      thrown.getMessage should include("Revision must contain 7 strings")
    }

    it("should throw IllegalArgumentException for long list") {
      val thrown = intercept[IllegalArgumentException] {
        QRevision(List("1", "1234567890aaaaaaaaaa1234567890bbbbbbbbbb", "tip",
          "default", "py4fun", "foo\nbar", "1326905676.0-3600", "too much"))
      }
      thrown.getMessage should include("Revision must contain 7 strings")
    }

    it("should not accept null") {
      val thrown = intercept[IllegalArgumentException] {
        QRevision(null)
      }
      thrown.getMessage should equal("requirement failed")
    }
  }

}
