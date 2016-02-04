import akka.actor._
import akka.stream.{Materializer, ActorMaterializer}
import akka.http.scaladsl.{HttpExt, Http}
import akka.http.scaladsl.model._
import akka.util.ByteString
import scala.concurrent.Await
import scala.concurrent.duration._
import spray.json._

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val token = args(0)
  println("token " + token)
  val http = Http(system)
  val groups = Service(http, token).listGroups
  println(s"groups $groups")
}

case class MeetupGroup(id: Long, name: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat2(MeetupGroup)
}

object Const {
  val meetupUrl = "https://api.meetup.com/find"
  val timeout = 70 seconds
}

case class Service(http: HttpExt, token: String) {
  import MyJsonProtocol._

  private def tokenParam: String = s"?key=$token"

  private def urlToJsonAst(inputUrl: String): JsValue = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val response = Await.result(http.singleRequest(HttpRequest(uri = inputUrl)), Const.timeout)
    println(s"status ${response.status}")
    val responseContent = Await.result(response.entity.dataBytes.runFold(ByteString(""))(_ ++ _), Const.timeout)
    val responseStr = responseContent.decodeString("UTF8")
    println(s"resp content ${responseStr.substring(0, 100)}")
    responseStr.parseJson
  }

  private def buildUrl(specificPart: String) = s"${Const.meetupUrl}/$specificPart$tokenParam"

  def listGroups: List[MeetupGroup] = {
    urlToJsonAst(buildUrl("groups")).convertTo[List[MeetupGroup]]
  }
}