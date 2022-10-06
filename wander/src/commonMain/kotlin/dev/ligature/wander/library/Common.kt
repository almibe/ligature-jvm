/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.library

import arrow.core.Either.Left
import arrow.core.Either.Right
import dev.ligature.StringLiteral
import dev.ligature.wander.interpreter.Bindings
import dev.ligature.wander.interpreter.Value
import dev.ligature.wander.parser.*

interface Logger {
  fun log(message: String): Value.Nothing
}

/**
 * Create a Bindings instance with functions
 */
fun common(logger: Logger = object : Logger {
  override fun log(message: String): Value.Nothing {
    println(message)
    return Value.Nothing
  }
}): Bindings {
  val stdLib = Bindings()

  stdLib
    .bindVariable(
      "log",
      Value.NativeFunction(
        listOf("message")
      ) { bindings ->
        when (val message = bindings.read("message")) {
          is Right -> {
            val output = ((message.value as LigatureValue).value as StringLiteral).value
            logger.log(output)
            Right(Nothing)
          }

          is Left -> TODO()
        }
      }
    )

  stdLib
    .bindVariable(
      Name("ensure"),
      NativeFunction(
        listOf(Parameter(Name("arg")))) { bindings ->
          when (val arg = bindings.read(Name("arg"))) {
            is Right -> {
              val value = arg.value
              if (value is BooleanValue) {
                if (value.value) {
                  Right(Nothing)
                } else {
                  TODO()
                }
              } else {
                TODO()
              }
            }
            is Left -> TODO()
          }
      })

  stdLib
    .bindVariable(
      Name("not"),
      NativeFunction(
        listOf(Parameter(Name("bool")))
      ) //, WanderType.Boolean)),
      { bindings: Bindings ->
        val bool = bindings.read(Name("bool"))
        if (bool is Right) {
          val value = bool.value as BooleanValue
          Right(BooleanValue(!value.value))
        } else {
          TODO()
        }
      }
    )

  stdLib
    .bindVariable(
      Name("and"),
      NativeFunction(
        listOf(
          Parameter(Name("boolLeft")),//, WanderType.Boolean),
          Parameter(Name("boolRight"))//, WanderType.Boolean)
        )) { bindings: Bindings ->
        val left = bindings.read(Name("boolLeft"))
        val right = bindings.read(Name("boolRight"))
        if (left is Right && right is Right) {
          val l = left.value as BooleanValue
          val r = right.value as BooleanValue
          Right(BooleanValue(l.value && r.value))
        } else {
          Left(ScriptError("and requires two booleans"))
        }
      }
    )

  stdLib.bindVariable(
    Name("or"),
    NativeFunction(
      listOf(
        Parameter(Name("boolLeft")),//, WanderType.Boolean),
        Parameter(Name("boolRight"))//, WanderType.Boolean)
      )) { bindings: Bindings ->
      val left = bindings.read(Name("boolLeft"))
      val right = bindings.read(Name("boolRight"))
      if (left is Right && right is Right) {
        val l = left.value as BooleanValue
        val r = right.value as BooleanValue
        Right(BooleanValue(l.value || r.value))
      } else {
        Left (ScriptError("or requires two booleans"))
      }
    }
  )

  stdLib.bindVariable(
    Name("range"),
    NativeFunction(
      listOf(Parameter(Name("start")), Parameter(Name("stop")))) { bindings: Bindings ->
        //TODO for now just return 0 to 9, eventually read start and stop params
        TODO()
      }
    )

  stdLib.bindVariable(
    Name("each"),
    NativeFunction(
      listOf(Parameter(Name("seq")), Parameter(Name("fn")))) { bindings ->
      val seq = bindings.read(Name("seq"))
      val fn = bindings.read(Name("fn"))
      if (seq is Right<*> && fn is Right<*>) {
        val s = seq.value as Seq
        val f = fn.value as FunctionDefinition
        if (f.parameters.size == 1) {
          val argName = f.parameters.first().name
          s.contents.forEach {
            when (val r = it.eval(bindings)) {
              is Right -> {
                bindings.addScope()
                bindings.bindVariable(argName, r.value.result)
                val evalRes = f.eval(bindings)
                println("Finished eval - $evalRes")
                bindings.removeScope()
                if (evalRes is Left) {
                  return@NativeFunction evalRes
                }
              }
              is Left -> return@NativeFunction r
            }
          }
          Right(Nothing)
        } else {
          TODO("report error, incorrect parameters")
        }
      } else {
        Left (ScriptError("Could not map."))
      }
    }
  )

  //TODO count function

  stdLib.bindVariable(
    Name("map"),
    NativeFunction(
      listOf(Parameter(Name("seq")), Parameter(Name("fn")))) { bindings ->
        val seq = bindings.read(Name("seq"))
        val fn = bindings.read(Name("fn"))
        if (seq is Right<*> && fn is Right<*>) {
          val s = seq.value as Seq
          val f = fn.value as FunctionDefinition
          if (f.parameters.size == 1) {
            val argName = f.parameters.first().name
            val res = mutableListOf<Expression>()
            s.contents.forEach {
              //TODO bind `it` to the name saved above
              val r = it.eval(bindings)
              when (r) {
                is Right -> {
                  bindings.addScope()
                  bindings.bindVariable(argName, r.value.result)
                  val evalRes = f.eval(bindings)
                  bindings.removeScope()
                  when (evalRes) {
                    is Right -> res.add(evalRes.value.result)
                    is Left -> return@NativeFunction evalRes
                  }
                }
                is Left -> return@NativeFunction r
              }
            }
            Right(Seq(res))
          } else {
            TODO("report error, incorrect parameters")
          }
        } else {
          Left (ScriptError("Could not map."))
        }
      }
  )
  return stdLib
}