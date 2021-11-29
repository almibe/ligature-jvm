/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.lexer

import dev.ligature.gaze.{
  Gaze,
  Nibbler,
  take,
  takeAll,
  takeCond,
  takeFirst,
  takeString,
  takeUntil,
  takeWhile,
  repeat
}
import dev.ligature.lig.LigNibblers

enum TokenType:
  case Boolean, Spaces, Identifier, Integer, Comment, NewLine, String,
  LetKeyword, EqualSign, Name

case class Token(val content: String, val tokenType: TokenType)

case class TokenizeError(message: String)

def tokenize(input: String): Either[TokenizeError, Seq[Token]] = {
  val gaze = Gaze.from(input)
  gaze.attempt(tokensNib) match {
    case None  => Left(TokenizeError("Error"))
    case Some(res) => Right(res)
  }
}

val stringTokenNib =
  LigNibblers.stringNibbler.map(results => Seq(Token(results(1).mkString, TokenType.String)))

val newLineTokenNib = takeString("\n").map(res => Seq(Token(res.mkString, TokenType.NewLine)))

val commentTokenNib = takeAll(takeString("#"), takeUntil('\n')).map(results =>
  Seq(Token(results.mkString, TokenType.Comment))
)

// val booleanTokenNib = takeFirst(takeString("true"), takeString("false")).map(
//   Token(_, TokenType.Boolean)
// )

/** This nibbler matches both names and keywords. After the initial match all
  * keywords are checked and if none match and name is returned.
  */
val nameTokenNib = takeAll(
  takeCond { (c: Char) => c.isLetter || c == '_' },
  takeWhile[Char] { (c: Char) => c.isLetter || c.isDigit || c == '_' }
)
//  .map(_.mkString)
  .map { value =>
    value.mkString match {
      case "true"        => Seq(Token("true", TokenType.Boolean))
      case "false"       => Seq(Token("false", TokenType.Boolean))
      case "let"         => Seq(Token("let", TokenType.LetKeyword))
      case value: String => Seq(Token(value, TokenType.Name))
    }
  }

val equalSignTokenNib = takeString("=").map(res => Seq(Token(res.mkString, TokenType.EqualSign)))

val integerTokenNib = LigNibblers.numberNibbler.map(res => Seq(Token(res.mkString, TokenType.Integer)))

val spacesTokenNib = takeWhile[Char](_ == ' ').map(res => Seq(Token(res.mkString, TokenType.Spaces)))

val identifierTokenNib =
  LigNibblers.identifierNibbler.map(res => Seq(Token(res.mkString, TokenType.Identifier)))

val tokensNib: Nibbler[Char, Token] = repeat(
  takeFirst(
    spacesTokenNib,
    nameTokenNib,
    integerTokenNib,
    newLineTokenNib,
    identifierTokenNib,
    stringTokenNib,
    commentTokenNib,
    equalSignTokenNib
  )
)
