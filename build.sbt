val baseSettings = Seq(
  organization := "net.randallalexander",
  scalaVersion := "2.11.8"
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
  "-Ywarn-value-discard"
)

val finchVersion = "0.11.0-M4"
val twitterServerVersion = "1.20.0"
val finagleVersion = "6.35.0"
val circeVersion = "0.5.3"
val monixVersion = "2.0.2"
/*
  Cats version driven by finch.  Careful
  when upgrading due to binary incompatibilities
  between Cats 0.5.x and 0.6.x
 */


val buildSettings = Seq(
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
    "com.twitter"        %% "twitter-server" % twitterServerVersion,
    "com.twitter"        %% "finagle-stats"  % finagleVersion,//monitoring

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

lazy val service = project.in(file("."))
  .settings(name := "resturaunt-chooser")
  .settings(baseSettings ++ buildSettings ++ Defaults.itSettings)