package com.websocket.http

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}

import scala.util.control.NonFatal

trait Routes extends PingApi with AuthenticationApi with SubscribingApi with EventsApi {

  private val exnHandler = ExceptionHandler {
    case NonFatal(err) =>
      extractRequest { _ =>
        val msg = Option(err.getMessage)
          .getOrElse(s"${err.getClass.getName} (no error message)")

        complete(
          HttpResponse(
            StatusCodes.InternalServerError,
            entity = s"Internal Server Error (500): $msg"
          )
        )
      }
  }

  val routes: Route = handleExceptions(exnHandler) {
    pingRoute ~ loginRoute ~ subscriptionRoute ~ eventsRoute
  }
}
