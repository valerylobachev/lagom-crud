import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

// Copyright settings
def copyrightSettings: Seq[Setting[_]] = Seq(
  organizationName := "Valery Lobachev",
  startYear := Some(2018),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
)

organization in ThisBuild := "biz.lobachev"
version in ThisBuild := "0.1.0-SNAPSHOT"
maintainer in ThisBuild := "valery@lobachev.biz"

scalaVersion in ThisBuild := "2.12.9"

def commonSettings: Seq[Setting[_]] = Seq(
  unmanagedClasspath in Runtime += baseDirectory.value / "conf",
  mappings in Universal ++= directory(baseDirectory.value / "conf"),
  scriptClasspath := "../conf/" +: scriptClasspath.value,
)

lazy val root = (project in file("."))
  .settings(name := "lagom-crud")
  .settings(copyrightSettings: _*)
  .aggregate(
    `annette-shared`,
    `bpm-repository-api`,
    `bpm-repository-impl`,
  )

lazy val `annette-shared` = (project in file("annette-shared"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      lagomScaladslTestKit,
      Dependencies.playJsonExt

    ) ++ Dependencies.tests ++ Dependencies.elastic
  )
  .settings(copyrightSettings: _*)


lazy val `bpm-repository-api` = (project in file("bpm-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared` % "compile->compile;test->test")

lazy val `bpm-repository-impl` = (project in file("bpm-repository-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`bpm-repository-api`, `annette-shared` % "compile->compile;test->test")



lagomCassandraCleanOnStart in ThisBuild := false

// Kafka not used yet
lagomKafkaEnabled in ThisBuild := false
