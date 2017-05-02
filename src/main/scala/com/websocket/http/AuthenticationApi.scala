package com.websocket.http

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask

import com.websocket.actors.{AuthProto, GetLoginType}
import com.websocket.util.UpickleSupport._
import com.websocket.util.Implicits._

trait AuthenticationApi {

  protected def authActor: ActorRef

  val loginRoute: Route =
    path("login") {
      post {
        entity(as[GetLoginType]) { case req @ GetLoginType(_, _) =>
          onSuccess((authActor ? req).mapTo[AuthProto]) { resp =>
            complete(resp)
          }
        }
      }
    }
}
