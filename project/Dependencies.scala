import sbt._
import play.sbt.PlayImport._

object Dependencies {
  object Version {

    val scala = "2.12.6"

    val macwire = "2.3.3"

    val scalaTest = "3.0.5"
    val scalaTestPlusPlay = "3.1.2"
    val commonsIO = "2.6"

    val elastic4s = "7.3.1"
    val playJsonExt = "0.40.2"
  }

  val macwire = "com.softwaremill.macwire" %% "macros" % Version.macwire % "provided"

  val tests = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % Version.scalaTestPlusPlay % Test,
    "commons-io" % "commons-io" % Version.commonsIO % Test
  )

  val elastic: Seq[sbt.ModuleID] = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-json-play" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Version.elastic4s % Test
  )

  val playJsonExt: sbt.ModuleID = "ai.x" %% "play-json-extensions" % Version.playJsonExt

  val core = tests :+ guice :+ ws
}
