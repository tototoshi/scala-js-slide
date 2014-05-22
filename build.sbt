import sbt._
import Keys._
import play.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._

name := "slide"

version := "0.1-SNAPSHOT"

lazy val js = project.in(file("js")).settings(
  name := "scala-js-slide-js",
  resolvers += Resolver.url("scala-js-releases", url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(Resolver.ivyStylePatterns)
).settings(scalajsDefaultSettings:_*)

lazy val scalajsDefaultSettings: Seq[Setting[_]] = scalaJSSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %% "scalajs-dom" % "0.2",
    "org.scala-lang.modules.scalajs" %% "scalajs-jquery" % "0.4",
    "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test"
  )
 )

lazy val copyJS = Command.command("copyJS") { (state) =>
  scala.sys.process.Process(List("cp", "js/target/scala-2.10/scala-js-slide-js-opt.js", "server/public/javascripts/")).!
  state
}

lazy val server = project.in(file("server"))
.settings(play.Project.playScalaSettings:_*)
.settings(
  name := "scala-js-slide-server",
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % "2.0.0",
    "org.scalikejdbc" %% "scalikejdbc-play-plugin" % "2.2.0",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "net.databinder" %% "pamflet-knockoff" % "0.5.0",
    "com.github.tototoshi" %% "play-json4s-jackson" % "0.2.0"
  ),
  commands ++= Seq(copyJS)
 )
.aggregate(js)
