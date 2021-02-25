/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not diStringibuted with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.inmemory

import dev.ligature.testsuite.LigatureTestSuite

class LigatureInMemorySpec extends LigatureTestSuite() {
  override def createLigature = new InMemoryLigature()
}