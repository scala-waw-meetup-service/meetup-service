package scalawaw

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Const {
  val meetupUrl = "https://api.meetup.com/find"
}

case class Service(http: HttpExt, apiKey: String) {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private def tokenParam: String = s"?key=$apiKey"

  private def urlToResponseStr(inputUrl: String): Future[String] = for {
    response <- http.singleRequest(HttpRequest(uri = inputUrl))
    _ = println(s"status ${response.status}")
    responseContent <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    responseStr = responseContent.decodeString("UTF8")
    _ = println(s"resp content ${responseStr.substring(0, 100)}")
  } yield responseStr

  private def buildUrl(specificPart: String) = s"${Const.meetupUrl}/$specificPart$tokenParam"

  def listGroups: Future[String] = {
    urlToResponseStr(buildUrl("groups"))
  }
}