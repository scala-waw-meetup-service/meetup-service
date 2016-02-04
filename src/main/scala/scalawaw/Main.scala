package scalawaw

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val apiKey = "63962396461756829383a1f2f33b48"
  println("token " + apiKey)
  val http = Http(system)
  val groups = Service(http, apiKey).listGroups
  println(s"groups $groups")
}

