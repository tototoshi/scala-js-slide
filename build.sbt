// Turn this project into a Scala.js project by importing these settings
scalaJSSettings

name := "slide"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %% "scalajs-dom" % "0.4",
  "org.scala-lang.modules.scalajs" %% "scalajs-jquery" % "0.4",
  "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test"
)
