package com.websocket.actors

import akka.actor.Actor
import com.websocket.util.Config

import scala.collection.mutable

case object PublishEvents

object EventsActor {
  private val queue = new mutable.Queue[SubscriptionEvent]
}

class EventsActor extends Actor with Config {

  import EventsActor._

  def receive = {
    case evt @ TableRemoved(_) => queue.enqueue(evt)
    case evt @ RemovalFailed(_) => queue.enqueue(evt)
    case evt @ TableUpdated(_) => queue.enqueue(evt)
    case evt @ UpdateFailed(_) => queue.enqueue(evt)
    case PublishEvents => sender() ! queue.dequeueAll(_ => true)
    case _ =>
  }
}