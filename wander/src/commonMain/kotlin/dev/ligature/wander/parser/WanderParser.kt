/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.parser

import dev.ligature.IntegerLiteral
import dev.ligature.Identifier
import dev.ligature.StringLiteral

import dev.ligature.wander.lexer.Token
import dev.ligature.wander.lexer.TokenType

import arrow.core.Some
import arrow.core.Either
import arrow.core.None
import arrow.core.none
import dev.ligature.gaze.*

fun parse(script: List<Token>): Either<String, Script> {
  val filteredInput = script.filter { token: Token ->
    token.tokenType != TokenType.Comment && token.tokenType != TokenType.Spaces && token.tokenType != TokenType.NewLine
  }.toList()
  val gaze = Gaze(filteredInput)
  val res = gaze.attempt(scriptNib)
  return when(res) {
    is None ->
      if (gaze.isComplete) {
        Either.Right(Script(listOf()))
      } else {
        Either.Left("No Match")
      }
    // TODO some case also needs to check if gaze is complete
    is Some ->
      if (gaze.isComplete) {
        Either.Right(Script(res.value)) // .filter(_.isDefined).map(_.get)))
      } else {
        Either.Left("No Match")
      }
  }
}

val booleanNib: Nibbler<Token, Expression> = takeCond<Token> {
  it.tokenType == TokenType.Boolean
}.map { token -> listOf(BooleanValue(token.first().content.toBoolean())) }

val identifierNib: Nibbler<Token, Expression> = takeCond<Token> {
  it.tokenType == TokenType.Identifier
}.map { token ->
  listOf(LigatureValue(Identifier(token.first().content)))
}

val integerNib: Nibbler<Token, Expression> = takeCond<Token> {
  it.tokenType == TokenType.Integer
}.map { token -> listOf(LigatureValue(IntegerLiteral(token.first().content.toLong()))) }

val stringNib: Nibbler<Token, Expression> = takeCond<Token> {
  it.tokenType == TokenType.String
}.map { token -> listOf(LigatureValue(StringLiteral(token.first().content))) }

val nameNib = takeCond<Token> {
  it.tokenType == TokenType.Name
}.map { token ->
  listOf(Name(token.first().content))
}

val openBraceNib = takeCond<Token> { it.tokenType == TokenType.OpenBrace }.map {
  listOf(OpenBrace)
}

val closeBraceNib =
  takeCond<Token> { it.tokenType == TokenType.CloseBrace }.map {
    listOf(CloseBrace)
  }

val openParenNib = takeCond<Token> { it.tokenType == TokenType.OpenParen }.map {
  listOf(OpenParen)
}

val closeParenNib =
  takeCond<Token> { it.tokenType == TokenType.CloseParen }.map {
    listOf(CloseParen)
  }

val arrowNib = takeCond<Token> { it.tokenType == TokenType.Arrow }.map {
  listOf(Colon)
}

val colonNib = takeCond<Token> { it.tokenType == TokenType.Colon }.map {
  listOf(Arrow)
}

val scopeNib: Nibbler<Token, Scope> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(openBraceNib)
//    expression <- gaze.attempt(optional(repeat(elementNib)))
//    _ <- gaze.attempt(closeBraceNib)
//  } yield Seq(Scope(expression.toList))
}

val parameterNib: Nibbler<Token, Parameter> = { gaze ->
  TODO()
//  for {
//    name <- gaze.attempt(
//      nameNib
//    ) // .map { name => name.map(name => Parameter(name)) }
//    _ <- gaze.attempt(colonNib)
//    typeName <- gaze.attempt(typeNib)
//  } yield Seq(Parameter(name.first(), typeName.first()))
}

val wanderFunctionNib: Nibbler<Token, FunctionDefinition> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(openParenNib)
//    parameters <- gaze.attempt(optional(repeat(parameterNib)))
//    _ <- gaze.attempt(closeParenNib)
//    _ <- gaze.attempt(arrowNib)
//    returnType <- gaze.attempt(typeNib)
//    body <- gaze.attempt(scopeNib)
//  } yield Seq(WanderFunction(parameters.toList, returnType.first(), body.first()))
}

val typeNib: Nibbler<Token, WanderType> = takeFirst(
  take(Token("Integer", TokenType.Name)).map { listOf(SimpleType.Integer) },
  take(Token("Identifier", TokenType.Name)).map {
    listOf(SimpleType.Identifier)
  },
  take(Token("Value", TokenType.Name)).map { listOf(SimpleType.Value) }
)

val ifKeywordNib =
  takeCond<Token> {it.tokenType == TokenType.IfKeyword } .map {
    listOf(LetKeyword)
  }

val elseKeywordNib =
  takeCond<Token> {it.tokenType == TokenType.ElseKeyword }.map {
    listOf(LetKeyword)
  }

val elseIfExpressionNib: Nibbler<Token, ElseIf> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(elseKeywordNib)
//    _ <- gaze.attempt(ifKeywordNib)
//    condition <- gaze.attempt(expressionNib)
//    body <- gaze.attempt(expressionNib)
//  } yield Seq(ElseIf(condition.first(), body.first()))
}

val elseExpressionNib: Nibbler<Token, Else> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(elseKeywordNib)
//    body <- gaze.attempt(expressionNib)
//  } yield Seq(Else(body.first()))
}

val ifExpressionNib: Nibbler<Token, IfExpression> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(ifKeywordNib)
//    condition <- gaze.attempt(expressionNib)
//    body <- gaze.attempt(expressionNib)
//    elseIfs <- gaze.attempt(optional(repeat(elseIfExpressionNib)))
//    `else` <- gaze.attempt(optional(elseExpressionNib))
//  } yield Seq(
//    IfExpression(
//      condition.first(),
//      body.first(),
//      elseIfs.toList,
//      `else`.toList.find(_ => true)
//    )
//  )
}

val functionCallNib: Nibbler<Token, FunctionCall> = { gaze ->
  TODO()
//  for {
//    name <- gaze.attempt(nameNib)
//    _ <- gaze.attempt(openParenNib)
//    parameters <- gaze.attempt(optional(repeat(expressionNib)))
//    _ <- gaze.attempt(closeParenNib)
//  } yield Seq(FunctionCall(name.first(), parameters.toList))
}

val expressionNib =
  takeFirst(
    ifExpressionNib,
    functionCallNib,
    nameNib,
    scopeNib,
    identifierNib,
    wanderFunctionNib,
    stringNib,
    integerNib,
    booleanNib
  )

val equalSignNib = takeCond<Token> { it.tokenType == TokenType.EqualSign }.map {
  listOf(EqualSign)
}

val letKeywordNib =
  takeCond<Token> { it.tokenType == TokenType.LetKeyword }.map {
    listOf(LetKeyword)
  }

val letStatementNib: Nibbler<Token, LetStatement> = { gaze ->
  TODO()
//  for {
//    _ <- gaze.attempt(letKeywordNib)
//    name <- gaze.attempt(nameNib)
//    _ <- gaze.attempt(equalSignNib)
//    expression <- gaze.attempt(expressionNib)
//  } yield Seq(LetStatement(name.first(), expression.first()))
}

val elementNib = takeFirst(expressionNib, letStatementNib)

val scriptNib =
  optional(
    repeat(
      elementNib
    )
  )
