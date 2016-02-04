package scalawaw

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Main extends App with MyJsonProtocol {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  //implicit val executor = system.dispatcher

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  def callMeetup: Future[List[MeetupGroup]] = {
    val apiKey = "63962396461756829383a1f2f33b48"
    println("token " + apiKey)
    val http = Http(system)
    val groups = Service(http, apiKey).listGroups
    println(s"groups $groups")
    Future(groups) //TODO listGroups should return Future
  }

  val routes = {
    pathPrefix("groups") {
      get {
        complete {
          "aaa"
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}

