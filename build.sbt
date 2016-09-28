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
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import"
)

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
    "com.github.finagle" %% "finch-core"     % "0.11.0-M3",
    "com.github.finagle" %% "finch-circe"    % "0.11.0-M3",
    "com.twitter"        %% "twitter-server" % "1.23.0",
    "com.twitter"        %% "finagle-stats"  % "6.38.0",//monitoring

    "io.circe"           %% "circe-generic"  % "0.5.2",
    //"com.twitter" %% "util-logging" % "6.35.0",

    //experimental
    "io.monix" %% "monix" % "2.0.2",
    "io.monix" %% "monix-cats" % "2.0.2",
    //config
    "com.typesafe" % "config" % "1.3.1",
    //logging
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  ),
  
  doctestTestFramework := DoctestTestFramework.ScalaTest,
  doctestWithDependencies := false
  // Uncomment to enable benchmarking.
  //enablePlugins(JmhPlugin)
  // Uncomment to enable WartRemover linting.
  //wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.Throw)
)

lazy val service = project.in(file("."))
  .settings(name := "resturaunt-chooser")
  .settings(baseSettings ++ buildSettings ++ Defaults.itSettings)