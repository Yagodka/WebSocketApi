package com.websocket

import com.websocket.http.RestApi
import com.websocket.util.Implicits._

import scala.io.StdIn

object Main extends App {
  val httpServer = RestApi().run

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    httpServer
      .flatMap(_.unbind())
      .onComplete(_ => util.Implicits.system.terminate())
}