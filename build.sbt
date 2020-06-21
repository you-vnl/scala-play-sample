import scala.collection

name := """play2-hands-on"""
organization := "com.vnl"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.2"
val circeVersion = "0.13.0"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")


libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.2.3",
  "com.h2database" % "h2" % "1.4.200",
  "org.scalikejdbc" %% "scalikejdbc" % "3.4.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.4.0",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.8.0-scalikejdbc-3.4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "org.scalikejdbc" %% "scalikejdbc-test" % "3.4.0" % Test,
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.6.6",
  "com.typesafe.akka" %% "akka-actor" % "2.6.6",
  "com.softwaremill.sttp.client" %% "core" % "2.2.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

enablePlugins(ScalikejdbcPlugin)
javaOptions in Test += "-Dconfig.file=conf/test.conf"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.vnl.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.vnl.binders._"
