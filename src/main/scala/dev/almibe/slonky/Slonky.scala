/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.almibe.slonky

import cats.effect.Resource
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

trait Slonky {
  def instance: Resource[Task, SlonkyInstance]
}

trait SlonkyInstance {
  def read: Resource[Task, SlonkyReadTx]
  def write: Resource[Task, SlonkyWriteTx]
}

trait SlonkyReadTx {
  def keyExists(key: ByteVector): Task[Boolean]
  def prefixExists(prefix: ByteVector): Task[Boolean]
  def get(key: ByteVector): Task[Option[ByteVector]]
  def prefixScan(prefix: ByteVector): Observable[(ByteVector, ByteVector)]
  def rangeScan(from: ByteVector, to: ByteVector): Observable[(ByteVector, ByteVector)]
  def scanAll(): Observable[(ByteVector, ByteVector)]
}

trait SlonkyWriteTx {
  def keyExists(key: ByteVector): Task[Boolean]
  def prefixExists(prefix: ByteVector): Task[Boolean]
  def get(key: ByteVector): Task[Option[ByteVector]]
  def put(key: ByteVector, value: ByteVector): Task[Unit]
  def remove(key: ByteVector): Task[Unit]
  def cancel(): Task[Unit]
}