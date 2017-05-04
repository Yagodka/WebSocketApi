package com.websocket.actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.pipe
import com.websocket.actors.SubscribingActor.{SubscribedTable, Tables}
import com.websocket.util.Config
import upickle.default.key
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future
import com.websocket.util.Implicits._

sealed trait SubscriptionProto

@key("subscribe_tables")
case object GetSubscribeTables extends SubscriptionProto

@key("unsubscribe_tables")
case object UnsubscribeTables extends SubscriptionProto

@key("table_list")
case class SubscribesTablesList(tables: Seq[SubscribedTable]) extends SubscriptionProto

@key("add_table")
final case class AddTable(@key("after_id") afterId: Int, table: SubscribedTable) extends SubscriptionProto

@key("table_added")
final case class AddedTable(@key("after_id") afterId: Int, table: SubscribedTable) extends SubscriptionProto

@key("update_table")
final case class UpdateTable(table: SubscribedTable) extends SubscriptionProto

@key("table_updated")
final case class TableUpdated(table: SubscribedTable) extends SubscriptionProto

@key("update_failed")
final case class UpdateFailed(id: Int) extends SubscriptionProto

@key("remove_table")
final case class RemoveTable(id: Int) extends SubscriptionProto

@key("removal_failed")
final case class RemovalFailed(id: Int) extends SubscriptionProto

@key("table_removed")
final case class TableRemoved(id: Int) extends SubscriptionProto

object SubscribingActor {

  final case class SubscribedTable(id: Int = 0, name: String, participants: Int)

  class Tables(tag: Tag) extends Table[SubscribedTable](tag, "TABLES") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def participants = column[Int]("PARTICIPANTS")

    def * = (id, name, participants).mapTo[SubscribedTable]
  }

}

class SubscribingActor(eventsActor: ActorRef) extends Actor with Config {

  lazy val tables = TableQuery[Tables]

  def receive = {
    case GetSubscribeTables => getSubscribeTables.pipeTo(sender())
    case UnsubscribeTables => unsubscribe
    case AddTable(afterId, SubscribedTable(_, n, px)) => pipeEvent(addTable(afterId, n, px))
    case UpdateTable(table) => pipeEvent(updateTable(table))
    case RemoveTable(id) => pipeEvent(removeTable(id))
    case _ =>
  }

  def pipeEvent(event: Future[SubscriptionProto]) = {
    event pipeTo eventsActor
    event pipeTo sender()
  }

  // todo Maybe this is a very simple option, but is it more difficult?
  def addTable(afterId: Int, name: String, participants: Int) = {
    val insertQuery = tables returning tables.map(_.id) into ((item, id) => item.copy(id = id))
    val action = insertQuery += SubscribedTable(0, name, participants)
    database.run(action) map {
      newTable => AddedTable(afterId, newTable)
    }
  }

  def updateTable(table: SubscribedTable) = {
    val updateQuery = tables.filter(_.id === table.id).update(table)
    database.run(updateQuery) map {
      case 0 => UpdateFailed(table.id)
      case _ => TableUpdated(table)
    }
  }

  def removeTable(id: Int) = {
    val deleteQuery = tables.filter(_.id === id).delete
    database.run(deleteQuery) map {
      case 0 => RemovalFailed(id)
      case _ => TableRemoved(id)
    }
  }

  def getSubscribeTables: Future[SubscribesTablesList] =
    database.run {
      tables.result
    }.map(SubscribesTablesList)

  def unsubscribe: Future[Int] =
    database.run {
      tables.delete
    }
}
