package com.websocket.util

import com.typesafe.config.{ConfigFactory, Config => OriginConfig}
import com.websocket.actors.AuthenticationActor.Users
import com.websocket.actors.SubscribingActor.Tables
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery

trait Config {
  protected lazy val config: OriginConfig = ConfigFactory.load()

  // REST
  private val httpConfig: OriginConfig = config.getConfig("http")
  val httpHost = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  // DB
  val database = Database.forConfig("h2mem1")
  val ddlSetup = DBIO.seq(
    TableQuery[Users].schema.create,
    TableQuery[Users] ++= DemoData.demoUsers,
    TableQuery[Tables].schema.create,
    TableQuery[Tables] ++= DemoData.demoTables)
}
