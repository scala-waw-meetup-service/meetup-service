package scalawaw

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Const {
  val meetupUrl = "https://api.meetup.com/"
}

case class Service(http: HttpExt, apiKey: String) {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private def tokenParam: String = s"?key=$apiKey"

  private def urlToResponseStr(inputUrl: String): Future[String] = {
    println(inputUrl)
    for {
      response <- http.singleRequest(HttpRequest(uri = inputUrl))
      _ = println(s"status ${response.status}")
      responseContent <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      responseStr = responseContent.decodeString("UTF8")
      _ = println(s"resp content ${responseStr}")
    } yield responseStr
  }

  private def buildUrl(specificPart: String) = s"${Const.meetupUrl}/$specificPart$tokenParam"

  def listGroups: Future[String] = {
    urlToResponseStr(buildUrl("groups/find"))
  }

  def listEvents: Future[String] = {
    urlToResponseStr(buildUrl("2/open_events") + "&country=PL&city=Warsaw&time=1d,20d")
  }
}