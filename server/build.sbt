val fs2DataVersion = "1.6.0"
val pureConfigVersion = "0.17.1"
val http4sVersion = "0.23.16"

ThisBuild / tlBaseVersion := "0.0"

ThisBuild / licenses := List(License.Apache2)

ThisBuild / organizationName := "Lucas Satabin"

organization := "org.gnieh"

name := "mastodon-tools"

scalaVersion := "2.13.10"

libraryDependencies ++= List(
  "com.softwaremill.sttp.client3" %% "circe" % "3.8.3",
  "com.softwaremill.sttp.client3" %% "armeria-backend-fs2" % "3.8.3",
  "org.gnieh" %% "fs2-data-csv-generic" % fs2DataVersion,
  "org.gnieh" %% "fs2-data-json-circe" % fs2DataVersion,
  "co.fs2" %% "fs2-io" % "3.3.0",
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-sttp" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-generic" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-ip4s" % pureConfigVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.3",
  "com.kailuowang" %% "mau" % "0.3.1"
)
