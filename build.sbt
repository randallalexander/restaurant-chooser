import sbt.Keys.resolvers
import sbt.Resolver

val baseSettings = Seq(
  organization := "net.randallalexander",
  scalaVersion := "2.12.1"
)

val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:higherKinds",
  "-target:jvm-1.8",
  "-unchecked",
  "-Xlint",
  "-Xfuture",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard")

val finchVersion = "0.14.0"
val monixVersion = "2.3.0"

//Taken from Finch build file
val shapelessVersion = "2.3.2"
val catsVersion = "0.9.0"
val circeVersion = "0.7.0"


lazy val buildSettings = Seq(
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Xfatal-warnings"))
  },
  scalacOptions in (Test, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Xfatal-warnings"))
  },

  resolvers ++= Seq(Resolver.typesafeRepo("releases"), "twttr" at "https://maven.twttr.com/"),

  libraryDependencies ++= Seq(
    "com.github.finagle" %% "finch-core"     % finchVersion,
    "com.github.finagle" %% "finch-circe"    % finchVersion,

    "org.typelevel" %% "cats-core" % catsVersion,
    "io.circe"           %% "circe-generic"  % circeVersion,

    //experimental
    "io.monix" %% "monix" % monixVersion,
    "io.monix" %% "monix-cats" % monixVersion,
    //config
    "com.typesafe" % "config" % "1.3.1",
    //logging
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  ),

  doctestTestFramework := DoctestTestFramework.ScalaTest,
  doctestWithDependencies := false
  // Uncomment to enable benchmarking.
  //enablePlugins(JmhPlugin)
  //enable WartRemover linting.
  //wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.Throw)
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "resturaunt-chooser")
  .settings(baseSettings ++ buildSettings)