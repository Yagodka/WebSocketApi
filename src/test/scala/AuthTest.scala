import com.websocket.actors.{GetLoginType, LoginFailed, LoginType}
import com.websocket.util.UpickleSupport._
import upickle.default._

class AuthTest extends BaseRestTest {

  "At the correct login server" should "answer LoginType" in {
    Post("/login", GetLoginType("user1234", "password1234")) ~> routes ~> check {
      responseAs[LoginType] shouldBe LoginType("admin")
    }
  }

  "At the wrong login server" should "answer LoginFailed" in {
    Post("/login", GetLoginType("user1234", "wrong1234")) ~> routes ~> check {
      responseAs[LoginFailed.type] shouldBe LoginFailed
    }
  }
}
