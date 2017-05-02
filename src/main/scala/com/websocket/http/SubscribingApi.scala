package com.websocket.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import com.websocket.actors._
import com.websocket.util.UpickleSupport._
import com.websocket.util.Implicits._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait SubscribingApi {

  protected def subscribeActor: ActorRef

  protected def authActor: ActorRef

  def credentialsOfRequest(req: HttpRequest): Option[HasAdminPermissions] =
    for {
      Authorization(credentials) <- req.header[Authorization]
      basicCredentials = credentials.asInstanceOf[BasicHttpCredentials]
    } yield HasAdminPermissions(basicCredentials.username, basicCredentials.password)

  implicit def rejectionHandler = RejectionHandler.newBuilder().handle {
    case AuthorizationFailedRejection => complete(UserNotAuthorized)
      }.result()

  def hasAdminPermissions(request: HttpRequest): Future[Boolean] =
    credentialsOfRequest(request) match {
      case Some(r) => (authActor ? r).mapTo[Boolean]
      case None => Future.successful(false)
    }

  val subscriptionRoute: Route = handleRejections(rejectionHandler) {
    path("subscribing" / "admin") {
      post {
        extractRequest { request =>
          authorizeAsync(_ => hasAdminPermissions(request)) {
            entity(as[GetSubscribeTables]) { req =>
              onSuccess((subscribeActor ? req).mapTo[SubscribesTablesList]) { resp =>
                complete(resp)
              }
            } ~ entity(as[UnsubscribeTables]) { req =>
              subscribeActor ! req
              complete(StatusCodes.NoContent)
            }
          }
        }
      }
    } ~ path("subscribing") {
      post {
        entity(as[AddTable]) { req =>
          onComplete((subscribeActor ? req).mapTo[SubscriptionProto]) { resp =>
            complete(StatusCodes.OK, resp)
          }
        }
      } ~
        put {
          entity(as[UpdateTable]) { req =>
            onComplete((subscribeActor ? req).mapTo[UpdateEvent]) {
              case Success(resp @ TableUpdated(_)) => complete(StatusCodes.OK, resp)
              case Success(resp @ UpdateFailed(_)) => complete(StatusCodes.NotFound, resp)
              case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
            }
          }
        } ~
        delete {
          entity(as[RemoveTable]) { req =>
            onComplete((subscribeActor ? req).mapTo[RemoveEvent]) {
              case Success(resp @ TableRemoved(_)) => complete(StatusCodes.OK, resp)
              case Success(resp @ RemovalFailed(_)) => complete(StatusCodes.NotFound, resp)
              case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
            }
          }
        }
    }
  }
}
