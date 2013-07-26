package es

import akka.actor.{ActorRef, Props, ActorSystem}
import org.ancelin.play2.couchbase.Couchbase
import org.ancelin.play2.couchbase.store.{EventStored, CouchbaseEventSourcing}
import models.Formatters
import play.api.Play.current
import play.api.{Logger, Mode, Play}

object Bootstrap {

  val system: ActorSystem = ActorSystem("es-system")
  val bucket = Couchbase.bucket("es")

  implicit val ec = Couchbase.couchbaseExecutor

  val couchbaseEventSourcing = CouchbaseEventSourcing( system, bucket)
    .registerEventFormatter(Formatters.CreditCardValidatedFormatter)
    .registerEventFormatter(Formatters.CreditCardValidationRequestedFormatter)
    .registerEventFormatter(Formatters.OrderAcceptedFormatter)
    .registerEventFormatter(Formatters.OrderFormatter)
    .registerEventFormatter(Formatters.OrderSubmittedFormatter)
    .registerSnapshotFormatter(Formatters.StateFormatter)

  val processor = couchbaseEventSourcing.processorOf(Props(new OrderProcessor with EventStored))
  val validator = system.actorOf(Props(new CreditCardValidator(processor)))
  val ordersHandler = system.actorOf(Props(new OrdersHandler))

  def bootstrap() = {
    couchbaseEventSourcing.replayAll()
  }

  def shutdown() = {
    system.shutdown()
  }
}
