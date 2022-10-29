/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.lig

import dev.ligature.gaze.*
import dev.ligature.gaze.takeString

object LigNibblers {
  val whiteSpaceNibbler = takeCharacters(' ', '\t')

  val newLineNibbler = takeAll(takeFirst(takeString("\n"), takeString("\r\n")))

  val identifierNibbler =
      between(
          takeString("<"),
          takeWhile { c -> Regex("[a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;%=]").matches(c.toString()) },
          takeString(">"))

  val integerNibbler = takeAll(optional(takeString("-")), takeWhile { it.isDigit() })

  val bytesNibbler =
      takeAll(
          takeString("0"),
          takeString("x"),
          takeWhile { Regex("[a-fA-F0-9]").matches(it.toString()) })

  val stringContentNibbler: Nibbler<Char, Char> = { gaze: Gaze<Char> ->
    // Full pattern \"(([^\x00-\x1F\"\\]|\\[\"\\/bfnrt]|\\u[0-9a-fA-F]{4})*)\"
    val commandChars = 0x00.toChar()..0x1f.toChar()
    val validHexChar = { c: Char -> (c in '0'..'9') || (c in 'a'..'f') || (c in 'A'..'F') }
    val hexNibbler = takeWhile(validHexChar) // TODO should probably only read pairs in
    val sb = mutableListOf<Char>()
    var fail = false
    var complete = false
    while (!complete && !fail && !gaze.isComplete) {
      val c = gaze.next()
      if (commandChars.contains(c) || c == null) {
        fail = true
      } else if (c == '\\') {
        sb.add(c)
        when (val next = gaze.next()) {
          null -> fail = true
          '\\',
          '"',
          'b',
          'f',
          'n',
          'r',
          't' -> sb.add(next)
          'u' -> {
            sb.add(next)
            when (val res = gaze.attempt(hexNibbler)) {
              null -> fail = true
              else -> {
                if (res.size == 4) {
                  sb.addAll(res)
                } else {
                  fail = true
                }
              }
            }
          }
          else -> fail = true
        }
      } else {
        sb.add(c)
      }
      if (gaze.peek() == '"') {
        complete = true
      }
    }
    if (fail) {
      null
    } else {
      sb.toList()
    }
  }

  val stringNibbler = between(takeString("\""), stringContentNibbler)

  // TODO this needs cleaned up
  private val validPrefixName: CharArray =
      (('a'..'z').toMutableList() +
              ('A'..'Z').toMutableList() +
              ('0'..'9').toMutableList() +
              listOf('_'))
          .joinToString("")
          .toCharArray()

  val prefixNameNibbler =
      takeCharacters(
          *validPrefixName) // matches _a-zA-Z0-9, TODO probably shouldn't make names that start
                            // with numbers
  val copyNibbler = takeString("^") // matches ^

  val idGenNibbler = takeString("{}") // matches {}
  // val identifierIdGenNibbler = ??? //matches <{}> <prefix:{}> <{}:postfix> <pre:{}:post> etc
  // val prefixedIdentifierNibbler = ??? //matches prefix:value:after:prefix
  // val prefixedIdGenNibbler = ??? // matches prefix:value:after:prefix:{}
}
