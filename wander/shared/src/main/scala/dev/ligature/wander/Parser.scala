/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.gaze.{
  Gaze,
  Nibbler,
  optional,
  take,
  takeAll,
  takeCond,
  takeFirst,
  takeString,
  repeat
}
import dev.ligature.wander.Token
import dev.ligature.gaze.Result

case class Name(name: String)

enum Term:
  case NameTerm(value: Name)
  case IdentifierLiteral(value: Identifier)
  case IntegerLiteral(value: Long)
  case StringLiteral(value: String)
  case BooleanLiteral(value: Boolean)
  case NothingLiteral
  case QuestionMark
  case Array(value: Seq[Term])
  case Set(value: Seq[Term])
  case Record(entires: Seq[(Name, Term)])
  case LetExpression(decls: Seq[(Name, Term)], body: Term)
  case Application(terms: Seq[Term])
  case Lambda(
    parameters: Seq[Name],
    body: Term)
  case IfExpression(
    conditional: Term,
    ifBody: Term,
    elseBody: Term)
  case Pipe

def parse(script: Seq[Token]): Either[WanderError, Term] = {
  val filteredInput = script.filter {
    _ match
      case Token.Spaces(_) | Token.NewLine | Token.Comment => false
      case _ => true
  }
  val gaze = Gaze(filteredInput)
  val res: Result[Term] = gaze.attempt(scriptNib)
  res match {
    case Result.NoMatch =>
      if (gaze.isComplete) {
        Right(Term.NothingLiteral)
      } else {
        Left(WanderError(s"Error Parsing - No Match - Next Token: ${gaze.next()}"))
      }
    case Result.Match(res) =>
      if (gaze.isComplete) {
        Right(res)
      } else {
        Left(WanderError(s"Error Parsing - No Match - Next Token: ${gaze.next()}"))
      }
    case Result.EmptyMatch => ???
  }
}

val nothingNib: Nibbler[Token, Term] = gaze =>
  gaze.next() match
    case Result.Match(Token.NothingKeyword) => Result.Match(Term.NothingLiteral)
    case _ => Result.NoMatch

val questionMarkTermNib: Nibbler[Token, Term] = gaze =>
  gaze.next() match
    case Result.Match(Token.QuestionMark) => Result.Match(Term.QuestionMark)
    case _ => Result.NoMatch

val booleanNib: Nibbler[Token, Term.BooleanLiteral] = gaze =>
  gaze.next() match
    case Result.Match(Token.BooleanLiteral(b)) => Result.Match(Term.BooleanLiteral(b))
    case _ => Result.NoMatch

val identifierNib: Nibbler[Token, Term.IdentifierLiteral] = gaze =>
  gaze.next() match
    case Result.Match(Token.Identifier(i)) => Result.Match(Term.IdentifierLiteral(i))
    case _ => Result.NoMatch

val integerNib: Nibbler[Token, Term.IntegerLiteral] = gaze =>
  gaze.next() match
    case Result.Match(Token.IntegerLiteral(i)) => Result.Match(Term.IntegerLiteral(i))
    case _ => Result.NoMatch

val stringNib: Nibbler[Token, Term.StringLiteral] = gaze =>
  gaze.next() match
    case Result.Match(Token.StringLiteral(s)) => Result.Match(Term.StringLiteral(s))
    case _ => Result.NoMatch

val nameNib: Nibbler[Token, Term.NameTerm] = gaze =>
  gaze.next() match
    case Result.Match(Token.Name(n)) => Result.Match(Term.NameTerm(Name(n)))
    case _ => Result.NoMatch

// val scopeNib: Nibbler[Token, Term.Scope] = { gaze =>
//   for {
//     _ <- gaze.attempt(take(Token.OpenBrace))
//     expression <- gaze.attempt(optional(repeat(elementNib)))
//     _ <- gaze.attempt(take(Token.CloseBrace))
//   } yield Seq(Term.Scope(expression.toList))
// }

val parameterNib: Nibbler[Token, Name] = { gaze =>
  for {
    name <- gaze.attempt(nameNib)
  } yield name.value
}

val lambdaNib: Nibbler[Token, Term.Lambda] = { gaze =>
  for {
    _ <- gaze.attempt(take(Token.Lambda))
    parameters <- gaze.attempt(optional(repeat(parameterNib)))
    _ <- gaze.attempt(take(Token.Arrow))
    body <- gaze.attempt(expressionNib)
  } yield Term.Lambda(parameters, body) //TODO handle this body better
}

// val typeNib: Nibbler[Token, WanderType] = takeFirst(
//   take(Token("Integer", TokenType.Name)).map(_ => List(WanderType.Integer)),
//   take(Token("Identifier", TokenType.Name)).map { _ =>
//     List(WanderType.Identifier)
//   },
//   take(Token("Value", TokenType.Name)).map(_ => List(WanderType.Value))
// )

val elseExpressionNib: Nibbler[Token, Term] = { gaze =>
  for {
    _ <- gaze.attempt(take(Token.ElseKeyword))
    body <- gaze.attempt(expressionNib)
  } yield body
}

val ifExpressionNib: Nibbler[Token, Term.IfExpression] = { gaze =>
  for {
    _ <- gaze.attempt(take(Token.IfKeyword))
    condition <- gaze.attempt(expressionNib)
    body <- gaze.attempt(expressionNib)
    `else` <- gaze.attempt(optional(elseExpressionNib))
  } yield
    Term.IfExpression(
      condition,
      body,
      `else`
    )
}

//NOTE: this will return either an Application or a Name.
val applicationNib: Nibbler[Token, Term] = { gaze =>
  val res = for
    name <- gaze.attempt(nameNib) //TODO this should also allow literals
    terms <- gaze.attempt(optional(repeat(expressionNib)))
  yield (name, terms)
  res match {
    case Result.NoMatch => Result.NoMatch
    case Result.Match((name, terms)) =>
      if terms.isEmpty then
        Result.Match(name)
      else Result.Match(Term.Application(Seq(name) ++ terms))
    case Result.EmptyMatch => ???
  }
}

val setNib: Nibbler[Token, Term.Set] = { gaze =>
  for
    _ <- gaze.attempt(take(Token.Hash))
    _ <- gaze.attempt(take(Token.OpenBracket))
    values <- gaze.attempt(optional(repeat(expressionNib)))
    _ <- gaze.attempt(take(Token.CloseBracket))
  yield Term.Set(values)
}

val arrayNib: Nibbler[Token, Term.Array] = { gaze =>
  for
    _ <- gaze.attempt(take(Token.OpenBracket))
    values <- gaze.attempt(optional(repeat(expressionNib)))
    _ <- gaze.attempt(take(Token.CloseBracket))
  yield Term.Array(values)
}

val fieldNib: Nibbler[Token, (Name, Term)] = { gaze =>
  val res = for
    name <- gaze.attempt(nameNib)
    _ <- gaze.attempt(take(Token.EqualSign))
    expression <- gaze.attempt(expressionNib)
  yield (name, expression)
  res match {
    case Result.Match((Term.NameTerm(name), term: Term)) => Result.Match((name, term))
    case _ => Result.NoMatch
  } 
}

val recordNib: Nibbler[Token, Term.Record] = { gaze =>
  for
    _ <- gaze.attempt(take(Token.OpenBrace))
    decls <- gaze.attempt(repeat(fieldNib))
    _ <- gaze.attempt(take(Token.CloseBrace))
  yield Term.Record(decls)
}

val letExpressionNib: Nibbler[Token, Term.LetExpression] = { gaze =>
  for {
    _ <- gaze.attempt(take(Token.LetKeyword))
    decls <- gaze.attempt(repeat(fieldNib))
    _ <- gaze.attempt(take(Token.InKeyword))
    body <- gaze.attempt(expressionNib)
    _ <- gaze.attempt(take(Token.EndKeyword))
  } yield Term.LetExpression(decls, body)
}

val expressionNib =
  takeFirst(
    ifExpressionNib,
    applicationNib,
    nameNib,
    identifierNib,
    lambdaNib,
    stringNib,
    integerNib,
    recordNib,
    letExpressionNib,
    arrayNib,
    setNib,
    booleanNib,
    nothingNib,
    questionMarkTermNib
  )

val scriptNib = expressionNib
