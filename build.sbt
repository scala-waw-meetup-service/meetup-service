name := "meetup-service"

version := "1.0"

scalaVersion := "2.11.7"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= {
  val akkaStreamVersion = "2.0.1"

  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamVersion
  )
}

    