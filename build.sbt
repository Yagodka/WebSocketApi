lazy val root = (project in file(".")).
  settings(
    organization := "com.newground",
    name := "WebSocketApi",
    version := "1.0",
    scalaVersion := "2.12.1",
    libraryDependencies ++= Seq(
      akkaDeps,
      akkaHttpDeps,
      akkaTestDeps,
      slickDeps,
      testDeps,
      utilDeps
    ).flatten,
    scalacOptions := Seq(
      "-unchecked",
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:_")
  )

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

lazy val akkaV = "2.4.17"
lazy val akkaHttpV = "10.0.4"

lazy val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV
)

lazy val akkaHttpDeps = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV
)

lazy val akkaTestDeps = Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV
)

lazy val slickDeps = Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "com.h2database" % "h2" % "1.4.195"
)

lazy val testDeps = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

lazy val utilDeps = Seq(
  "com.lihaoyi" %% "upickle" % "0.4.4",
  "de.heikoseeberger" %% "akka-sse" % "2.0.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
 )

fork in run := true
connectInput in run := true
cancelable in Global := true



