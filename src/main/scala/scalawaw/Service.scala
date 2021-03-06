package scalawaw

import java.time.{ZoneOffset, LocalDate}

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.util.{Timeout, ByteString}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Const {
  val meetupUrl = "https://api.meetup.com/"
  val meetupHost = "api.meetup.com"
  val meetupPort = 80
}

case class Service(connection: MeetupConnection, apiKey: String) {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(120 seconds)
  val logger = Logging(system, getClass)

  private def tokenParam: String = s"?key=$apiKey"

  private def urlToResponseStr(inputUrl: String): Future[String] = {
    logger.debug(s"getting from url $inputUrl")
    for {
      responseAny <- connection.wrapActor ? HttpRequest(uri = inputUrl)
      response = responseAny.asInstanceOf[HttpResponse]
      _ = logger.debug(s"status ${response.status}")
      responseContent <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      responseStr = responseContent.decodeString("UTF8")
      _ = logger.debug(s"resp content $responseStr")
    } yield responseStr
  }

  private def buildUrl(specificPart: String) = s"${Const.meetupUrl}/$specificPart$tokenParam"

  def listGroups: Future[String] = {
    urlToResponseStr(buildUrl("groups/find"))
  }

  def listEvents(city: String, since: String, to: String): Future[String] = {
    val since2 = LocalDate.parse(since).atStartOfDay().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli
    val to2 = LocalDate.parse(to).plusDays(1).atStartOfDay().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli
    urlToResponseStr(s"${Const.meetupUrl}/2/open_events?key=$apiKey&country=PL&city=$city&time=$since2,$to2")
  }

  def findProfile(id: String): Future[String] = {
    urlToResponseStr(s"${Const.meetupUrl}/members/$id?key=$apiKey&fields=privacy,gender,profile")
  }
}