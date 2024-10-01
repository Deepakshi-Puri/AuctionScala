name := """Auction Site"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick" % "6.0.0-M2",
  "org.playframework" %% "play-slick-evolutions" % "6.0.0-M2",
  "mysql" % "mysql-connector-java" % "8.0.33",
  specs2 % Test
)