name := "play-formnolia"
organization := "com.iterable"
organizationName := "Iterable"
startYear := Some(2018)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.4"
crossScalaVersions := Seq(scalaVersion.value, "2.11.12")

val PlayVersion = "2.6.12"

libraryDependencies ++=
  Seq(
    // compile dependencies
    "com.typesafe.play" %% "play" % PlayVersion,
    "com.propensive" %% "magnolia" % "0.7.1",
    // test dependencies
    "com.typesafe.play" %% "play-test" % PlayVersion % Test,
    "be.venneborg" %% "play26-refined" % "0.1.0" % Test,
    "org.scalatest" %% "scalatest" % "3.0.4" % Test
  )

enablePlugins(AutomateHeaderPlugin)
scalafmtOnCompile in ThisBuild := true
