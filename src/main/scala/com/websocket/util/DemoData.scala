package com.websocket.util

import com.websocket.actors.AuthenticationActor.User
import com.websocket.actors.SubscribingActor.SubscribedTable

object DemoData {

  val demoUsers = Seq(
      User("user1234", "password1234", "admin"),
      User("user2345", "password2345", "user"))
  val demoTables = Seq(
    SubscribedTable(1, "table - James Bond", 7),
    SubscribedTable(2, "table - Mission Impossible", 4))
}
