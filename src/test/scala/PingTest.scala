import com.websocket.http.{Ping, Pong}
import com.websocket.util.UpickleSupport._
import upickle.default._

class PingTest extends BaseRestTest {

  "At the Ping server" should "answer Pong" in {
    Post("/ping", Ping(3)) ~> routes ~> check {
      responseAs[Pong] shouldBe Pong(3)
    }
  }
  "At the Ping(4) server" should "answer Pong(4)" in {
    Post("/ping", Ping(4)) ~> routes ~> check {
      val resp = responseAs[Pong]
      resp.seq shouldBe 4
    }
  }
}
