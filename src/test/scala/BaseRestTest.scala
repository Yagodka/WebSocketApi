import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import com.websocket.http.RestApi

trait BaseRestTest extends FlatSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
  with Eventually
  with ScalaFutures {

  val routes: Route = RestApi().routes

  override protected def beforeAll(): Unit = {
    RestApi().run
    super.beforeAll()
  }
}
