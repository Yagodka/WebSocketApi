import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import com.websocket.actors.SubscribingActor.SubscribedTable
import com.websocket.actors._
import com.websocket.util.UpickleSupport._
import upickle.default._

class SubscribingTest extends BaseRestTest {

  val validCredentials = BasicHttpCredentials("user1234", "password1234")
  val wrongCredentials_1 = BasicHttpCredentials("user2345", "password2345")
  val wrongCredentials_2 = BasicHttpCredentials("user1234", "wrong2345")

  "At the SubscribeTables request" should "answer with subscribed tables" in {
    Post("/subscribing/admin", GetSubscribeTables) ~> addCredentials(validCredentials) ~> routes ~> check {
      val response = responseAs[SubscribesTablesList]
      response.tables should have size 2
      response.tables.head shouldBe SubscribedTable(1, "table - James Bond", 7)
    }
  }
  "At the UnsubscribeTables" should "not answer" in {
    Post("/subscribing/admin", UnsubscribeTables) ~> addCredentials(validCredentials) ~> routes ~> check {
      response.status shouldBe StatusCodes.NoContent
    }

    Post("/subscribing/admin", GetSubscribeTables) ~> addCredentials(validCredentials) ~> routes ~> check {
      responseAs[SubscribesTablesList].tables shouldBe empty
    }
  }

  "At the SubscribeTables request with wrong credentials" should "not answer UserNotAuthorized" in {
    Post("/subscribing/admin", GetSubscribeTables) ~> routes ~> check {
      responseAs[UserNotAuthorized.type] shouldBe UserNotAuthorized
    }
    Post("/subscribing/admin", GetSubscribeTables) ~> addCredentials(wrongCredentials_1) ~> routes ~> check {
      responseAs[UserNotAuthorized.type] shouldBe UserNotAuthorized
    }
    Post("/subscribing/admin", UnsubscribeTables) ~> addCredentials(wrongCredentials_2) ~> routes ~> check {
      responseAs[UserNotAuthorized.type] shouldBe UserNotAuthorized
    }
  }

  it should "working with tables" in {
    Post("/subscribing", AddTable(1, SubscribedTable(0, "table - Foo Fighters", 4))) ~> routes ~> check {
      val response = responseAs[AddedTable]
      response.table shouldBe SubscribedTable(3, "table - Foo Fighters", 4)
    }

    Put("/subscribing", UpdateTable(SubscribedTable(3, "table - Foo Fighters", 5))) ~> routes ~> check {
      responseAs[TableUpdated].table shouldBe SubscribedTable(3, "table - Foo Fighters", 5)
    }

    Put("/subscribing", UpdateTable(SubscribedTable(10, "table - Foo Fighters", 5))) ~> routes ~> check {
      responseAs[UpdateFailed] shouldBe UpdateFailed(10)
    }

    Delete("/subscribing", RemoveTable(3)) ~> routes ~> check {
      responseAs[TableRemoved] shouldBe TableRemoved(3)
    }

    Delete("/subscribing", RemoveTable(10)) ~> routes ~> check {
      responseAs[RemovalFailed] shouldBe RemovalFailed(10)
    }

    Get("/events") ~> routes ~> check {
      val response = responseAs[Seq[SubscriptionProto]]
      response shouldBe
          Vector(AddedTable(1, SubscribedTable(3, "table - Foo Fighters", 4)),
              TableUpdated(SubscribedTable(3, "table - Foo Fighters", 5)),
              UpdateFailed(10),
              TableRemoved(3),
              RemovalFailed(10))
    }
  }
}