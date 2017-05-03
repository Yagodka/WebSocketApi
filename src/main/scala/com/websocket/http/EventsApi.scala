package com.websocket.http

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import com.websocket.actors.{PublishEvents, SubscriptionProto}
import com.websocket.util.Implicits._
import com.websocket.util.UpickleSupport._

trait EventsApi {

  protected def eventsActor: ActorRef

  val eventsRoute: Route =
    path("events") {
      get {
        onSuccess((eventsActor ? PublishEvents).mapTo[Seq[SubscriptionProto]]) { events =>
          complete(events)
        }
      }
    }
}
