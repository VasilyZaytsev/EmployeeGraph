ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.8"

val scalatic = "org.scalactic" %% "scalactic" % "3.2.12"
val scalatest = "org.scalatest" %% "scalatest" % "3.2.12"
val stFlatSpec = "org.scalatest" %% "scalatest-flatspec" % "3.2.12"

lazy val root = (project in file("."))
  .settings(
    name := "SberGraph",
    libraryDependencies += scalatic,
    libraryDependencies += scalatest % Test,
    libraryDependencies += stFlatSpec % Test
  )
