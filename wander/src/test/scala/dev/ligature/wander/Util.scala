/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.Token
import dev.ligature.wander.libraries.common

class WanderSuiteCommonMode extends munit.FunSuite {
  def check(script: String, expected: WanderValue) =
    assertEquals(
      run(script, common()).getOrElse(???)._1,
      expected
    )
}

// class WanderSuiteInstanceLibrary extends munit.FunSuite {
//   def check(script: String, expected: String) =
//     createLigatureInMemory().use { instance =>
//       val res = run(script, instanceLibrary(instance)).map(printWanderValue)
//       assertIO(res, expected)
//     }
// }
