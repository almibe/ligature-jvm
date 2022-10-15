/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.library

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.Eval
import dev.ligature.Identifier
import dev.ligature.IntegerLiteral
import dev.ligature.Statement
import dev.ligature.StringLiteral
import dev.ligature.wander.interpreter.*
import dev.ligature.wander.model.Element
import dev.ligature.wander.model.write

interface Logger {
  fun log(message: String): Element.Nothing
}

/**
 * Create a Bindings instance with functions
 */
fun common(logger: Logger = object : Logger {
  override fun log(message: String): Element.Nothing {
    println(message)
    return Element.Nothing
  }
}): Bindings {
  val stdLib = Bindings()

  stdLib
    .bindVariable(
      "log",
      Element.NativeFunction(
        listOf("message")
      ) { bindings ->
        when (val message = bindings.read("message", Element::class)) {
          is Right -> {
            val output = write(message.value)
            logger.log(output)
            Right(Element.Nothing)
          }
          is Left -> TODO()
        }
      }
    )

  stdLib
    .bindVariable(
      "ensure",
      Element.NativeFunction(
        listOf("arg")) { bindings ->
          when (val arg = bindings.read("arg", Element.BooleanLiteral::class)) {
            is Right -> {
              val value = arg.value
                if (value.value) {
                  Right(Element.Nothing)
                } else {
                  Left(EvalError("Ensure failed."))
                }
            }
            is Left -> Left(EvalError("Ensure failed."))
          }
      })

  stdLib
    .bindVariable(
      "not",
      Element.NativeFunction(
        listOf("bool"))
      { bindings: Bindings ->
        val bool = bindings.read("bool", Element.BooleanLiteral::class)
        if (bool is Right) {
          val value = bool.value
          Right(Element.BooleanLiteral(!value.value))
        } else {
          TODO()
        }
      }
    )

  stdLib
    .bindVariable(
      "and",
      Element.NativeFunction(
        listOf(
          "boolLeft",
          "boolRight"
        )) { bindings: Bindings ->
        val left = bindings.read("boolLeft", Element.BooleanLiteral::class)
        val right = bindings.read("boolRight", Element.BooleanLiteral::class)
        if (left is Right && right is Right) {
          val l = left.value
          val r = right.value
          Right(Element.BooleanLiteral(l.value && r.value))
        } else {
          Left(EvalError("Function `and` requires two booleans"))
        }
      }
    )

  stdLib.bindVariable(
    "or",
    Element.NativeFunction(
      listOf(
        "boolLeft",
        "boolRight"
      )) { bindings: Bindings ->
      val left = bindings.read("boolLeft", Element.BooleanLiteral::class)
      val right = bindings.read("boolRight", Element.BooleanLiteral::class)
      if (left is Right && right is Right) {
        val l = left.value
        val r = right.value
        Right(Element.BooleanLiteral(l.value || r.value))
      } else {
        Left(EvalError("Function `or` requires two booleans"))
      }
    }
  )

//  stdLib.bindVariable(
//    Name("range"),
//    NativeFunction(
//      listOf(Parameter(Name("start")), Parameter(Name("stop")))) { bindings: Bindings ->
//        //TODO for now just return 0 to 9, eventually read start and stop params
//        TODO()
//      }
//    )
//
  stdLib.bindVariable(
    "each",
    Element.NativeFunction(
      listOf("seq", "fn")) { bindings ->
      val seq = bindings.read("seq", Element.Seq::class)
      val fn = bindings.read("fn", Element.Function::class)
      if (seq is Right && fn is Right) {
        val s = seq.value
        val f = fn.value
        if (f.parameters.size == 1) {
          val argName = f.parameters.first()
          s.values.forEach {
            when (val r = eval(it, bindings)) {
              is Right -> {
                bindings.addScope()
                bindings.bindVariable(argName, r.value)
                val evalRes = f.call(bindings)
                bindings.removeScope()
                if (evalRes is Left) {
                  return@NativeFunction evalRes
                }
              }
              is Left -> return@NativeFunction r
            }
          }
          Right(Element.Nothing)
        } else {
          Left(EvalError("Second argument to function `each` require a single parameter."))
        }
      } else {
        Left(EvalError("Function `each` requires two arguments."))
      }
    }
  )

//TODO count function
//TODO filter function
//TODO reduce function
//TODO first function
//TODO rest function

  stdLib.bindVariable(
    "filter",
    Element.NativeFunction(
      listOf("seq", "fn")) { bindings ->
      val seq = bindings.read("seq", Element.Seq::class)
      val fn = bindings.read("fn", Element.Function::class)
      if (seq is Right && fn is Right) {
        val s = seq.value
        val f = fn.value
        if (f.parameters.size == 1) {
          val argName = f.parameters.first()
          val res = mutableListOf<Element.Expression>()
          s.values.forEach {
            //TODO bind `it` to the name saved above
            when (val r = eval(it, bindings)) {
              is Right -> {
                bindings.addScope()
                bindings.bindVariable(argName, r.value)
                val evalRes = f.call(bindings)
                bindings.removeScope()
                when (evalRes) {
                  is Right -> {
                    val value = evalRes.value
                    if (value is Element.BooleanLiteral && value.value)
                    res.add(it)
                  }
                  is Left -> return@NativeFunction evalRes
                }
              }
              is Left -> return@NativeFunction r
            }
          }
          Right(Element.Seq(res))
        } else {
          TODO("report error, incorrect parameters")
        }
      } else {
        Left(EvalError("Could not filter."))
      }
    }
  )

  stdLib.bindVariable(
    "map",
    Element.NativeFunction(
      listOf("seq", "fn")) { bindings ->
        val seq = bindings.read("seq", Element.Seq::class)
        val fn = bindings.read("fn", Element.Function::class)
        if (seq is Right && fn is Right) {
          val s = seq.value
          val f = fn.value
          if (f.parameters.size == 1) {
            val argName = f.parameters.first()
            val res = mutableListOf<Element.Expression>()
            s.values.forEach {
              //TODO bind `it` to the name saved above
              when (val r = eval(it, bindings)) {
                is Right -> {
                  bindings.addScope()
                  bindings.bindVariable(argName, r.value)
                  val evalRes = f.call(bindings)
                  bindings.removeScope()
                  when (evalRes) {
                    //TODO fix below, I think I need a way to convert from Value to Element
                    is Right -> res.add(evalRes.value as Element.Expression)
                    is Left -> return@NativeFunction evalRes
                  }
                }
                is Left -> return@NativeFunction r
              }
            }
            Right(Element.Seq(res))
          } else {
            TODO("report error, incorrect parameters")
          }
        } else {
          Left(EvalError("Could not map."))
        }
      }
  )

  stdLib.bindVariable("graph", Element.NativeFunction(listOf()) {
    //TODO make sure no arguments are passed
    Right(Element.Graph())
  })

  fun seqToStatement(seq: Element.Seq): Either<EvalError, Statement> {
    if (seq.values.size == 3) {
      val e = seq.values[0] as Element.IdentifierLiteral
      val a = seq.values[1] as Element.IdentifierLiteral
      val v = when (val v = seq.values[2] as Element.Value) {
        is Element.BooleanLiteral -> return Left(EvalError("Booleans are not a valid Ligature Values."))
        is Element.Graph -> return Left(EvalError("Graphs are not a valid Ligature Values."))
        is Element.IdentifierLiteral -> v.value
        is Element.IntegerLiteral -> IntegerLiteral(v.value)
        Element.Nothing -> return Left(EvalError("Nothing is not a valid Ligature Value."))
        is Element.Seq -> return Left(EvalError("Seqs are not a valid Ligature Values."))
        is Element.StringLiteral -> StringLiteral(v.value)
      }
      return Right(Statement(e.value, a.value, v))
    } else {
      return Left(EvalError("${write(seq)} is not a valid statement."))
    }
  }

  stdLib.bindVariable("add", Element.NativeFunction(listOf("graph", "statement")) { bindings ->
    val graphBinding = bindings.read("graph", Element.Graph::class)
    val statementBinding = bindings.read("statement", Element.Seq::class)
    if (graphBinding is Right && statementBinding is Right) {
      val graph = graphBinding.value
      when (val statement = seqToStatement(statementBinding.value)) {
        is Right -> {
          graph.statements.add(statement.value)
          Right(graph)
        }
        is Left -> {
          statement
        }
      }
    } else {
      Left(EvalError("add requires a graph and a statement"))
    }
  })

  return stdLib
}
