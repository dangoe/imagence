import sbt.Keys.libraryDependencies

lazy val scalatestVersion = "3.0.1"
lazy val scalamockVersion = "3.6.0"

lazy val defaults = Seq(
  version := "1.1.0-SNAPSHOT",
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalamockVersion % "test"
  )
)

lazy val api = (project in file("imagence-api")).
  settings(defaults: _*).
  settings(
    name := "imagence-api"
  )

lazy val testsupport = (project in file("imagence-testsupport")).
  settings(defaults: _*).
  settings(
    name := "imagence-testsupport",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion
    )
  ).
  dependsOn(api)

lazy val core = (project in file("imagence-core")).
  settings(defaults: _*).
  settings(
    name := "imagence-core",
    libraryDependencies ++= Seq(
      "org.imgscalr" % "imgscalr-lib" % "4.2",
      "com.jhlabs" % "filters" % "2.0.235-1"
    )
  ).
  dependsOn(api, testsupport % "compile->compile;test->test")

lazy val extraPdf = (project in file("imagence-extra-pdf")).
  settings(defaults: _*).
  settings(
    name := "imagence-extra-pdf",
    libraryDependencies ++= Seq(
      "org.apache.pdfbox" % "pdfbox-tools" % "2.0.1"
    )
  ).
  dependsOn(core)

lazy val main = (project in file(".")).aggregate(api, testsupport, core, extraPdf)
