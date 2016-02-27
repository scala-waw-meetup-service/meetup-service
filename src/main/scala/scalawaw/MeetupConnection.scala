package scalawaw

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

case class SourceRef(actor: ActorRef)
case object CompleteMsg

case class MeetupConnection(http: HttpExt, wrapActor: ActorRef) {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val connection = http.newHostConnectionPool[Long](Const.meetupHost, Const.meetupPort)

  def runConnection() = Future {
    val source = Source.actorRef[(HttpRequest, Long)](1, OverflowStrategy.fail)
      .mapMaterializedValue(wrapActor ! SourceRef(_))
    val sink = Sink.actorRef[(Try[HttpResponse], Long)](wrapActor, CompleteMsg)
    connection.runWith(source, sink)
  }
}

object MeetupConnection {
  def create(http: HttpExt, wrapActor: ActorRef): MeetupConnection = {
    val conn = MeetupConnection(http, wrapActor)
    conn.runConnection()
    conn
  }
}

object WrapActor {
  def props() = Props(classOf[WrapActor])
}
class WrapActor extends Actor with ActorLogging {
  var requestIdSeq: Long = 0
  var sourceActorOpt: Option[ActorRef] = None
  val requestsSenders = scala.collection.mutable.Map[Long, ActorRef]()

  def sourceActor = sourceActorOpt.get

  def nextId = {
    requestIdSeq += 1
    requestIdSeq
  }

  def receive: Receive = {
    case SourceRef(actor) => sourceActorOpt = Some(actor)
    case request: HttpRequest =>
      val id = nextId
      sourceActor ! (request, id)
      requestsSenders.put(id, sender())
    case (responseTry: Try[_], id: Long) =>
      responseTry.map(response => requestsSenders.get(id).foreach(sndr => sndr ! response))
  }
}