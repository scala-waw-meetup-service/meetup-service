name := "meetup-service"

version := "1.0"

scalaVersion := "2.11.7"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= {
  val akkaVersion = "2.4.2"

  Seq(
    "com.typesafe.akka" %% "akka-stream"             % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core"          % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaVersion
  )
}

    