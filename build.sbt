// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin)

name := "slide"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.8.2",
  "be.doeraene" %%% "scalajs-jquery" % "0.8.1"
)
