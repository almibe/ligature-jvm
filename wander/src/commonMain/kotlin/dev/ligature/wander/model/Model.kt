/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.model

import arrow.core.Either
import dev.ligature.Identifier
import dev.ligature.wander.interpreter.Bindings
import dev.ligature.wander.interpreter.EvalError
import dev.ligature.wander.interpreter.eval

sealed interface Element {
  data class LetStatement(val name: String, val value: Expression): Element
  sealed interface Expression: Element
  sealed interface Value: Expression
  data class Name(val name: String): Expression
  data class Scope(val body: List<Element>): Expression
  data class BooleanLiteral(val value: Boolean): Value
  data class StringLiteral(val value: String): Value
  data class IntegerLiteral(val value: Long): Value
  data class IdentifierLiteral(val value: Identifier): Value
  data class FunctionCall(val name: String, val arguments: List<Expression>): Expression
  data class Conditional(val condition: Expression, val body: Expression)
  data class IfExpression(val ifConditional: Conditional,
                          val elsifConditional: List<Conditional> = listOf(),
                          val elseBody: Expression? = null): Expression
  data class Seq(val values: List<Expression>): Value
  object Nothing: Value

  sealed interface Function: Expression {
    val parameters: List<String>
    fun call(bindings: Bindings): Either<EvalError, Element>
  }
  data class LambdaDefinition(override val parameters: List<String>,
                              val body: List<Element>): Function {
    override fun call(bindings: Bindings): Either<EvalError, Element> =
      eval(body, bindings)
  }
  data class NativeFunction(override val parameters: List<String>,
                            val body: (Bindings) -> Either<EvalError, Value>): Function {
    override fun call(bindings: Bindings): Either<EvalError, Value> =
      body(bindings)
  }
}

fun write(element: Element): String =
  when (element) {
    is Element.BooleanLiteral -> element.value.toString()
    is Element.IdentifierLiteral -> "<${element.value.name}>"
    is Element.IntegerLiteral -> element.value.toString()
    is Element.LambdaDefinition -> "[lambda definition]"
    is Element.NativeFunction -> "[native function]"
    Element.Nothing -> "nothing"
    is Element.Seq -> {
      val sb = StringBuilder("[")
      sb.append(element.values.joinToString {
          write(it)
      })
      sb.append("]")
      sb.toString()
    }
    is Element.StringLiteral -> element.value
    is Element.FunctionCall -> "[function call]"
    is Element.IfExpression -> "[if expression]"
    is Element.Name -> element.name
    is Element.Scope -> "[scope]"
    is Element.LetStatement -> "[let statement]"
  }
