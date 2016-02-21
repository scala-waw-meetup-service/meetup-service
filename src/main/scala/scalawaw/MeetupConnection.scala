package scalawaw

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class SourceRef(actor: ActorRef)
case object CompleteMsg

case class MeetupConnection(http: HttpExt) {
  val connection = http.outgoingConnection(Const.meetupHost, Const.meetupPort)

  def runConnection(wrapActor: ActorRef) = Future {
    val source = Source.actorRef[HttpRequest](1, OverflowStrategy.fail)
      .mapMaterializedValue(wrapActor ! SourceRef(_))
    val sink = Sink.actorRef[HttpResponse](wrapActor, CompleteMsg)
    connection.runWith(source, sink)
  }
}

object WrapActor {
  def props() = Props(classOf[WrapActor])
}
class WrapActor extends Actor with ActorLogging {
  var sourceActor: ActorRef

  def receive: Receive = {
    case SourceRef(actor) => sourceActor = actor
    case request: HttpRequest => sourceActor ! request
    case response: HttpResponse => 
  }
}