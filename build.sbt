ThisBuild / scalaVersion     := "3.1.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.derekjw"
ThisBuild / organizationName := "Derek Williams"

lazy val root = (project in file("."))
  .settings(
    name := "morningstar",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-M6-2",
      "dev.zio" %% "zio-macros" % "2.0.0-M6-2",
      "dev.zio" %% "zio-test" % "2.0.0-M6-2" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
