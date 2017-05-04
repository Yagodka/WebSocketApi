package com.websocket.http

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import com.websocket.actors.{AuthenticationActor, EventsActor, SubscribingActor}
import com.websocket.util.Config
import com.websocket.util.Implicits._

case class RestApi() extends Config with Routes {

  override protected def eventsActor: ActorRef =
    system.actorOf(Props(classOf[EventsActor]))

  override protected def authActor: ActorRef =
    system.actorOf(Props(classOf[AuthenticationActor]))

  override protected def subscribeActor: ActorRef =
    system.actorOf(Props(classOf[SubscribingActor], eventsActor))

  def run = database.run(ddlSetup).flatMap { _ =>
    Http().bindAndHandle(routes, httpHost, httpPort)
  }
}
