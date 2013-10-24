package org.ancelin.play2.couchbase.client

import org.ancelin.play2.couchbase.CouchbaseBucket
import play.api.libs.json.{Json, Writes}
import scala.concurrent.{Future, ExecutionContext}
import net.spy.memcached.ops.OperationStatus
import net.spy.memcached.{PersistTo, ReplicateTo}
import net.spy.memcached.transcoders.Transcoder
import org.ancelin.play2.couchbase.client.CouchbaseFutures._
import play.api.libs.iteratee.{Iteratee, Enumerator}

trait Write {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Set Operations
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def setWithId[T <: {def id:String}](value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    set[T](value.id, value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def setWithKey[T](key: T => String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    set[T](key(value), value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def set[T](key: String, value: T, tc: Transcoder[T])(implicit bucket: CouchbaseBucket, ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.set(key, Constants.expiration, value, tc), ec )
  }

  def set[T](key: String, exp: Int, value: T, tc: Transcoder[T])(implicit bucket: CouchbaseBucket, ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.set(key, exp, value, tc), ec )
  }

  def set[T](key: String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.set(key, exp, Json.stringify(w.writes(value)), persistTo, replicateTo), ec )
  }

  def set[T](data: Enumerator[(String, T)], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ set[T](chunk._1, chunk._2, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  def setWithKey[T](key: T => String, data: Enumerator[T], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ set[T](key(chunk), chunk, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Add Operations
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def addWithId[T <: {def id:String}](value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    add[T](value.id, value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def addWithKey[T](key: T => String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    add[T](key(value), value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def add[T](key: String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.add(key, exp, Json.stringify(w.writes(value)), persistTo, replicateTo), ec )
  }

  def add[T](data: Enumerator[(String, T)], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ add[T](chunk._1, chunk._2, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  def addWithKey[T](key: T => String, data: Enumerator[T], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ add[T](key(chunk), chunk, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Replace Operations
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def replaceWithId[T <: {def id:String}](value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    replace[T](value.id, value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def replaceWithKey[T](key: T => String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    replace[T](key(value), value, exp, persistTo, replicateTo)(bucket, w, ec)
  }

  def replace[T](key: String, value: T, exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.replace(key, exp, Json.stringify(w.writes(value)), persistTo, replicateTo), ec )
  }

  def replace[T](data: Enumerator[(String, T)], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ replace[T](chunk._1, chunk._2, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  def replaceWithKey[T](key: T => String, data: Enumerator[T], exp: Int = Constants.expiration, persistTo: PersistTo = PersistTo.ZERO, replicateTo: ReplicateTo = ReplicateTo.ZERO)(implicit bucket: CouchbaseBucket, w: Writes[T], ec: ExecutionContext): Future[List[OperationStatus]] = {
    data(Iteratee.fold(List[Future[OperationStatus]]()) { (list, chunk) =>
      list :+ replace[T](key(chunk), chunk, exp, persistTo, replicateTo)(bucket, w, ec)
    }).flatMap(_.run).flatMap(Future.sequence(_))
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Flush Operations
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def flush(delay: Int)(implicit bucket: CouchbaseBucket, ec: ExecutionContext): Future[OperationStatus] = {
    waitForOperationStatus( bucket.couchbaseClient.flush(delay), ec )
  }

  def flush()(implicit bucket: CouchbaseBucket, ec: ExecutionContext): Future[OperationStatus] = {
    flush(Constants.expiration)(bucket, ec)
  }
}
