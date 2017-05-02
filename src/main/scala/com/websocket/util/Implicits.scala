package com.websocket.util

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor

object Implicits {
  implicit val system: ActorSystem = ActorSystem("WebSocketApi")
  implicit val exec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.duration._
  implicit val timeout: Timeout = Timeout(5 seconds)
}
