name := "play-formnolia"
organization := "com.iterable"
organizationName := "Iterable"
startYear := Some(2018)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage in ThisBuild := Some(url("https://github.com/Iterable/play-formnolia"))
scmInfo in ThisBuild := Some(
  ScmInfo(url("https://github.com/Iterable/play-formnolia"), "scm:git@github.com:Iterable/play-formnolia.git")
)
developers in ThisBuild := List(
  Developer("gmethvin", "Greg Methvin", "greg@methvin.net", new URL("https://github.com/gmethvin"))
)

scalaVersion := "2.12.6"
crossScalaVersions := Seq(scalaVersion.value, "2.11.12")

val PlayVersion = "2.6.18"

libraryDependencies ++=
  Seq(
    // compile dependencies
    "com.typesafe.play" %% "play" % PlayVersion,
    "com.propensive" %% "magnolia" % "0.7.1",
    // test dependencies
    "com.typesafe.play" %% "play-test" % PlayVersion % Test,
    "be.venneborg" %% "play26-refined" % "0.3.0" % Test,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

enablePlugins(AutomateHeaderPlugin)
scalafmtOnCompile in ThisBuild := true

import ReleaseTransformations._

publishTo := sonatypePublishTo.value
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
