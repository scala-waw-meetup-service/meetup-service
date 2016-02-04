package scalawaw

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  //implicit val executor = system.dispatcher

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val apiKey = "63962396461756829383a1f2f33b48"
  val http = Http(system)

  def callMeetup: Future[String] = for {
    groups <- Service(http, apiKey).listGroups
    _ = println("token " + apiKey)
    _ = println(s"groups $groups")
  } yield groups

  val routes = {
    pathPrefix("groups") {
      get {
        complete {
          callMeetup
        }
      }
    }
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}

