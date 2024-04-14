name := """healthcareproject2"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.13"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"


libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick" % "6.0.0-M2",
  "org.playframework" %% "play-slick-evolutions" % "6.0.0-M2",
  "com.h2database" % "h2" % "2.2.224",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "org.postgresql" % "postgresql" % "42.2.24",
  specs2 % Test
)

libraryDependencies += "com.vonage" % "client" % "6.2.0"