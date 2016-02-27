package scalawaw

import akka.actor.Cancellable
import akka.stream._
import akka.stream.scaladsl.{ZipWith, GraphDSL, Source}
import scala.concurrent.duration._

object Throttle {
  private def mainTick: Source[Unit, Cancellable] = Source.tick(0 seconds, 1 seconds, ())
  
  private def zipWithNode[A](implicit builder: GraphDSL.Builder[Unit]): FanInShape2[Unit, A, A] =  {
    val zipWith = ZipWith[Unit, A, A]((a: Unit, i: A) => i)
    val zipWithSmallBuffer = zipWith.withAttributes(Attributes.inputBuffer(initial = 1, max = 1))
    builder.add(zipWithSmallBuffer)
  }

  def zipWithThrottleTick[A, B](toZip: Source[A, Unit]): Source[A, Unit] =
    Source.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>
      import GraphDSL.Implicits._
      val zipNode = zipWithNode[A]
      mainTick ~> zipNode.in0
      toZip ~> zipNode.in1
      SourceShape(zipNode.out)
    })
}

