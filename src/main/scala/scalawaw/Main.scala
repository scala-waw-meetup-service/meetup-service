package scalawaw

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val apiKey = "63962396461756829383a1f2f33b48"
  val http = Http(system)
  val wrapActor = system.actorOf(WrapActor.props())
  val connection = MeetupConnection.create(http, wrapActor)

  val routes = {
    logRequestResult("foo") {
      path("events") {
        parameters('city, 'since, 'to) { (city, since, to) =>
          get {
            complete {
              Service(connection, apiKey).listEvents(city, since, to)
            }
          }
        }
      } ~
        path("profile") {
          parameters('id) { (id) =>
            get {
              complete {
                Service(connection, apiKey).findProfile(id)
              }
            }
          }
        }
    }
  }

  http.bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}

