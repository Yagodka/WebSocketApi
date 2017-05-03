package com.websocket.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.websocket.actors.AuthenticationActor.Users
import com.websocket.util.Config
import com.websocket.util.Implicits._
import slick.jdbc.H2Profile.api._
import upickle.default.key

import scala.concurrent.Future

sealed trait AuthProto

@key("login")
final case class GetLoginType(username: String, password: String) extends AuthProto

@key("login_successful")
final case class LoginType(@key("user_type") userType: String) extends AuthProto

final case class HasAdminPermissions(username: String, password: String) extends AuthProto

@key("not_authorized")
case object UserNotAuthorized extends AuthProto

@key("login_failed")
case object LoginFailed extends AuthProto

object AuthenticationActor {

  final case class User(name: String, password: String, userType: String)

  class Users(tag: Tag) extends Table[User](tag, "USERS") {

    def name = column[String]("NAME", O.PrimaryKey)

    def password = column[String]("PWD")
    def userType = column[String]("TYPE")

    def * = (name, password, userType).mapTo[User]
  }
}

class AuthenticationActor extends Actor with Config {

  lazy val users = TableQuery[Users]

  def receive = {
    case GetLoginType(n, p) => loginByName(n, p).pipeTo(sender())
    case HasAdminPermissions(u, p) => hasAdminPermissions(u, p).pipeTo(sender())
    case other => unhandled(other)
  }

  def loginByName(name: String, psw: String): Future[AuthProto] = database.run {
    val userType = users.filter(u =>
      u.name === name &&
        u.password === psw)
      .map(_.userType).result.headOption

    userType map {
      case Some(t) => LoginType(t)
      case None => LoginFailed
    }
  }

  def hasAdminPermissions(name: String, psw: String): Future[Boolean] = database.run {
    val user = users.filter(u =>
        u.name === name &&
        u.password === psw &&
        u.userType.toLowerCase === "admin")
      .result.headOption

    user map {
      case Some(_) => true
      case None => false
    }
  }
}