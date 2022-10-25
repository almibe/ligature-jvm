/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.interpreter

import arrow.core.Either
import arrow.core.getOrElse
import dev.ligature.Dataset
import dev.ligature.wander.library.commonLib
import dev.ligature.wander.model.Element

data class TestData(
    val category: String,
    val dataset: Dataset,
    val testInstances: List<TestInstance>
)

data class TestInstance(
  val description: String,
  val script: String,
  val result: Either<EvalError, Element>
)

//NOTE: New lines are hard coded as \n because sometimes on Windows
//the two types of new lines get mixed up in the codebase between the editor and Scalafmt.
//Not ideal, but it works consistently at least.
val newLine = "\n" //sys.props("line.separator")

val testData = listOf(
  TestData(
    category = "Primitives",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = primitivesTestData
  ),
  TestData(
    category = "Assignment",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = assignmentTestData
  ),
  TestData(
    category = "Closures",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = closureTestData
  ),
  TestData(
    category = "Boolean Functions",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = booleanExpression
  ),
  TestData(
    category = "If Expressions",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = ifExpression
  ),
  TestData(
    category = "Common Lib",
    dataset = Dataset.create("test").getOrElse { throw Error("Unexpected error.") },
    testInstances = commonLib
  )
  // TODO add error cases
)
