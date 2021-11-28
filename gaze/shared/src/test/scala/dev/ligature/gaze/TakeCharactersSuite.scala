/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import munit.FunSuite

import scala.collection.mutable.ArrayBuffer

private val abcStep = takeCharacters('a', 'b', 'c')
private val spaceStep = takeCharacters(' ')
private val fiveStep = takeCharacters('5')
private val emptyStep = takeCharacters()

class TakeCharactersSuite extends FunSuite {
  test("empty input") {
    val gaze = Gaze.from("")
    assertEquals(gaze.attempt(abcStep), None)
    assertEquals(gaze.attempt(spaceStep), None)
    assertEquals(gaze.attempt(emptyStep), None)
    assert(gaze.isComplete())
  }

  test("single 5 input") {
    val gaze = Gaze.from("5")
    assertEquals(gaze.attempt(abcStep), None)
    assertEquals(gaze.attempt(spaceStep), None)
    assertEquals(gaze.attempt(emptyStep), None)
    assert(!gaze.isComplete())
    assertEquals(gaze.attempt(fiveStep), Some("5".toSeq))
    assert(gaze.isComplete())
  }

  test("single 4 input") {
    val gaze = Gaze.from("4")
    assertEquals(gaze.attempt(fiveStep), None)
    assert(!gaze.isComplete())
  }

  test("multiple 5s input") {
    val gaze = Gaze.from("55555")
    val res = gaze.attempt(fiveStep) match {
      case Some(m) => m.mkString.toInt
      case None    => throw new Error("Should not happen")
    }
    assertEquals(res, 55555)
  }

  test("abcd test") {
    val gaze = Gaze.from("abc d")
    assertEquals(gaze.attempt(abcStep), Some("abc".toSeq))
    assertEquals(gaze.attempt(spaceStep), Some(" ".toSeq))
    assertEquals(gaze.attempt(abcStep), None)
    assert(!gaze.isComplete())
  }
}
