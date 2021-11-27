/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import munit.FunSuite

private val fiveStep = takeWhile { _ == '5' }
private val eatAllStep = takeWhile { _ => true }
private val spaceStep = takeWhile { c => c == ' ' || c == '\t' }
private val digitStep = takeWhile { _.isDigit }

class PredicateStepSpec extends FunSuite {
  test("empty input") {
    val gaze = Gaze.from("")
    assertEquals(gaze.attempt(fiveStep), None)
    assertEquals(gaze.attempt(eatAllStep), None)
    assertEquals(gaze.attempt(spaceStep), None)
    assertEquals(gaze.attempt(digitStep), None)
    assert(gaze.isComplete())
  }

  test("single 5 input") {
    val gaze = Gaze.from("5")
    assertEquals(gaze.attempt(fiveStep), Some("5"))
    assert(gaze.isComplete())
  }

  test("single 4 input") {
    val gaze = Gaze.from("4")
    assertEquals(gaze.attempt(fiveStep), None)
    assert(!gaze.isComplete())
  }

  test("multiple 5s input") {
    val gaze = Gaze.from("55555")
    val res = gaze.attempt(fiveStep)
    assertEquals(res, Some("55555"))
  }

  test("eat all nibbler test") {
    val gaze = Gaze.from("hello world")
    assertEquals(gaze.attempt(eatAllStep), Some("hello world"))
    assert(gaze.isComplete())
  }
}
