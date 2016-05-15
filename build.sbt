organization := "com.github.charithe"

name := "akka-stage-actor"

version := "1.0.0"

scalaVersion := "2.11.8"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.4",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.4" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test
)
