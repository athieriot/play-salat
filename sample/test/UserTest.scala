import com.github.athieriot.EmbedConnection
import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.test.Helpers._
import models._
import scala._
import scala.Predef._

class UserTest extends Specification with EmbedConnection {

  val port: Int = 12345
  val FakeMongoApp = FakeApplication(additionalConfiguration = Map("mongodb.default.port" -> port.toString),
                                     additionalPlugins = Seq("se.radley.plugin.salat.SalatPlugin"))

  "Embedded database" should {
    "be able to save a User" in {
      running(FakeMongoApp) {
        User.save(User(
          username = "leon",
          password = "1234",
          address = Some(Address("Örebro", "123 45", "Sweden"))
        ))
        User.count() must be greaterThan 0
      }
    }
  }
}
