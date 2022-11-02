/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.lig.lexer

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import dev.ligature.gaze.*
import dev.ligature.lig.LigError
import dev.ligature.lig.LigNibblers

sealed interface LigToken {
  //  data class Term(val value: String): LigToken
  //  object Equals: LigToken
  data class Identifier(val name: String) : LigToken
  data class GeneratedIdentifier(val name: String) : LigToken
  //  object Copy: LigToken
  object NewLine : LigToken
  object WhiteSpace : LigToken
  data class IntegerLiteral(val value: String) : LigToken
  data class StringLiteral(val value: String) : LigToken
  data class BytesLiteral(val value: String) : LigToken
}

fun tokenize(input: String): Either<LigError, List<LigToken>> {
  val gaze = Gaze.from(input)
  val tokens = mutableListOf<LigToken>()
  while (!gaze.isComplete) {
    val res =
        gaze.attempt<LigToken>(
            takeFirst(
                TokenNibblers.identifierNibbler,
                TokenNibblers.generatedIdentifierNibbler,
                TokenNibblers.whiteSpaceNibbler,
                TokenNibblers.newLineNibbler,
                TokenNibblers.stringNibbler,
                TokenNibblers.bytesNibbler,
                TokenNibblers.integerNibbler,
            ))
    when (res) {
      null -> {
        return Left(LigError("Could not parse ligature @${gaze.location}"))
      }
      else -> {
        tokens.addAll(res)
      }
    }
  }
  return Right(tokens)
}

object TokenNibblers {
  val whiteSpaceNibbler: Nibbler<Char, LigToken> =
      LigNibblers.whiteSpaceNibbler.map { listOf(LigToken.WhiteSpace) }

  val newLineNibbler: Nibbler<Char, LigToken> =
      LigNibblers.newLineNibbler.map { listOf(LigToken.NewLine) }

  val identifierNibbler =
      LigNibblers.identifierNibbler.map { listOf(LigToken.Identifier(it.joinToString(""))) }

  val generatedIdentifierNibbler =
      LigNibblers.generatedIdentifierNibbler.map {
        listOf(LigToken.GeneratedIdentifier(it.joinToString("")))
      }

  val integerNibbler =
      LigNibblers.integerNibbler.map { listOf(LigToken.IntegerLiteral(it.joinToString(""))) }

  val bytesNibbler =
      LigNibblers.bytesNibbler.map { listOf(LigToken.BytesLiteral(it.joinToString(""))) }

  val stringNibbler =
      LigNibblers.stringNibbler.map { listOf(LigToken.StringLiteral(it.joinToString(""))) }
}
