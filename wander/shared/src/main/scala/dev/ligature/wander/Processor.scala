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
  takeUntil,
  takeWhile,
  repeat
}

def process(terms: Seq[Term]): Either[WanderError, Expression] = {
  if terms.isEmpty then
    Right(Expression.Nothing)
  else
    process(terms(0))
}

def process(term: Term): Either[WanderError, Expression] =
  term match {
    case Term.NothingLiteral => Right(Expression.Nothing)
    case Term.Pipe => ???
    case Term.QuestionMark => Right(Expression.Nothing)
    case Term.IdentifierLiteral(value) => Right(Expression.IdentifierValue(value))
    case Term.Array(terms) => processArray(terms)
    case Term.Set(terms) => processSet(terms)
    case Term.BooleanLiteral(value) => Right(Expression.BooleanValue(value))
    case Term.Record(decls) => processRecord(decls)
    case Term.LetExpression(decls, body) => processLetExpression(decls, body)
    case Term.IntegerLiteral(value) => Right(Expression.IntegerValue(value))
    case Term.NameTerm(value) => Right(Expression.NameExpression(value))
    case Term.StringLiteral(value) => Right(Expression.StringValue(value))
    case Term.Application(terms) => Right(Expression.Nothing)//???
    case Term.Lambda(parameters, body) => processLambda(parameters, body)
    case Term.IfExpression(conditional, ifBody, elseBody) => processIfExpression(conditional, ifBody, elseBody)
  }

def processIfExpression(conditional: Term, ifBody: Term, elseBody: Term): Either[WanderError, Expression.IfExpression] = {
  val res = for {
    c <- process(conditional)
    i <- process(ifBody)
    e <- process(elseBody)
  } yield (c, i, e)
  res match {
    case Right((c, i, e)) => Right(Expression.IfExpression(c, i, e))
    case Left(e) => Left(e)
  }
}

def processLambda(parameters: Seq[Name], body: Term): Either[WanderError, Expression.Lambda] = {
  process(body) match {
    case Left(value) => Left(value)
    case Right(value) => Right(Expression.Lambda(parameters, value))
  }
}

def processLetExpression(decls: Seq[(Name, Term)], body: Term): Either[WanderError, Expression.LetExpression] = {
  val expressions = decls.map((name, term) => {
    process(term) match {
      case Left(value) => ???
      case Right(expression) => (name, expression)
    }
  })
  process(body) match {
    case Left(value) => ???
    case Right(body) => Right(Expression.LetExpression(expressions, body))
  }
}

def processRecord(decls: Seq[(Name, Term)]): Either[WanderError, Expression.Record] = {
  val expressions = decls.map((name, term) => {
    process(term) match {
      case Left(value) => ???
      case Right(expression) => (name, expression)
    }
  })
  Right(Expression.Record(expressions))
}

def processArray(terms: Seq[Term]): Either[WanderError, Expression.Array] = {
  val expressions = terms.map(t => {
    process(t) match {
      case Left(value) => ???
      case Right(value) => value
    }
  })
  Right(Expression.Array(expressions))
}

def processSet(terms: Seq[Term]): Either[WanderError, Expression.Set] = {
  val expressions = terms.map(t => {
    process(t) match {
      case Left(value) => ???
      case Right(value) => value
    }
  })
  Right(Expression.Set(expressions))
}
