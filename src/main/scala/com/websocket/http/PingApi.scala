package com.websocket.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import upickle.default.key

import com.websocket.util.UpickleSupport._

sealed trait PingPong

@key("ping")
final case class Ping(seq: Int) extends PingPong

@key("pong")
final case class Pong(seq: Int) extends PingPong

trait PingApi {

  val pingRoute: Route =
    path("ping") {
      post {
        entity(as[Ping]) { case Ping(seq) =>
          complete {
            Pong(seq)
          }
        }
      }
    }
}
