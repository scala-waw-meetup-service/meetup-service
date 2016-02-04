package scalawaw

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.util.ByteString
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

case class MeetupGroup(id: Long, name: String)

trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val meetupGroupFormat = jsonFormat2(MeetupGroup)
}

object Const {
  val meetupUrl = "https://api.meetup.com/find"
  val timeout = 70 seconds
}

case class Service(http: HttpExt, apiKey: String) extends MyJsonProtocol {

  private def tokenParam: String = s"?key=$apiKey"

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