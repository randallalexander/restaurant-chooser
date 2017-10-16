import sbt.Keys.resolvers
import sbt.Resolver

val baseSettings = Seq(
  organization := "net.randallalexander",
  scalaVersion := "2.12.3"
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
//  "-Ywarn-unused-import",
  "-Ywarn-value-discard")

val finchVersion = "0.16.0-M3"

//Taken from Finch build file
val shapelessVersion = "2.3.2"
val catsVersion = "1.0.0-MF"
val circeVersion = "0.9.0-M1"
val doobieVersion = "0.5.0-M8"
val fs2Version = "0.10.0-M6"


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

    "org.typelevel"      %% "cats-core" % catsVersion,
    "io.circe"           %% "circe-generic"  % circeVersion,

    //fs2
    "co.fs2" %% "fs2-core" % fs2Version,

    "org.tpolecat" %% "doobie-core"      % doobieVersion,
    "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
    "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
    "com.zaxxer" % "HikariCP" % "2.7.2",//correct version??


    //config
    "com.typesafe" % "config" % "1.3.2",
    //logging
    "io.verizon.journal" %% "core" % "3.0.+"
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
    name := "restaurant-chooser")
  .settings(baseSettings ++ buildSettings)