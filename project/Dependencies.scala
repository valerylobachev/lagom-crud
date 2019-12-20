import sbt._
import play.sbt.PlayImport._

object Dependencies {
  object Version {
    val macwire = "2.3.3"
    val scalaTest = "3.0.8"
    val commonsIO = "2.6"
    val elastic4s = "7.3.1"
    val playJsonExt = "0.40.2"

    val akkaPersistenceInmemoryVersion = "2.5.15.2"
  }

  val macwire = "com.softwaremill.macwire" %% "macros" % Version.macwire % "provided"

  val tests = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest % Test,
    "commons-io" % "commons-io" % Version.commonsIO % Test,
    //"com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion % Test
  )

  val elastic: Seq[sbt.ModuleID] = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-json-play" % Version.elastic4s,
    //"com.sksamuel.elastic4s" %% "elastic4s-json-jackson" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Version.elastic4s % Test
  )

  val playJsonExt: sbt.ModuleID = "ai.x" %% "play-json-extensions" % Version.playJsonExt

}
