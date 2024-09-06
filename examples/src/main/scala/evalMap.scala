/*
 * Copyright 2023 Zainab Ali
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

package docs.reference

import aquascape.*
import aquascape.examples.*
import aquascape.examples.syntax.given
import cats.Show
import cats.effect.*
import cats.effect.IO
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceEvalMap")
object evalMap {

  // TODO: Share this.
  def takeFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to take)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val basic = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMap(c => IO(c.toInt).trace())
          .stage("evalMap")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val evalTap = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalTap(c => IO(c.toInt).trace().void)
          .stage("evalTap")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val evalMapTake = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(2)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMap(c => IO(c.toInt).trace())
          .take(n)
          .stage(s"evalMap(…).take($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val evalMapChunk = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMapChunk(c => IO(c.toInt).trace())
          .stage("evalMapChunk")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val evalMapChunkHead = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMapChunk(c => IO(c.toInt).trace())
          .head
          .stage(s"evalMapChunk(…).head")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val error = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMap(_ => IO.raiseError(Err).trace())
          .stage("evalMap")
          .compile
          .drain
          .compileStage("compile.drain")
      )
  }

  @JSExport
  val errorEvalMap = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        def isEven(i: Int): IO[Boolean] = IO(i % 2 == 0)
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMap(c => IO.raiseWhen(c == 'b')(Err).as(c.toInt).trace())
          .stage("evalMap(…toInt…)")
          .evalMap(i => isEven(i).trace())
          .stage("evalMap(…isEven…)")
          .compile
          .drain
          .compileStage("compile.drain")
      }
  }
  @JSExport
  val errorEvalMapChunk = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        def isEven(i: Int): IO[Boolean] = IO(i % 2 == 0)
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .evalMapChunk(c => IO.raiseWhen(c == 'b')(Err).as(c.toInt).trace())
          .stage("evalMapChunk(…toInt…)")
          .evalMap(i => isEven(i).trace())
          .stage("evalMap(…isEven…)")
          .compile
          .drain
          .compileStage("compile.drain")
      }
  }

}