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

  resolvers += Resolver.typesafeRepo("releases"),

  libraryDependencies ++= Seq(
    "com.github.finagle" %% "finch-core"    % "0.10.0",
    "com.github.finagle" %% "finch-circe"   % "0.10.0",
    "io.circe"           %% "circe-generic" % "0.4.1",/* exclude("org.typelevel", "cats"),
    "org.typelevel"      %% "cats"          % "0.6.+",*/
    "com.typesafe" % "config" % "1.3.0",
    //logging
    "ch.qos.logback" % "logback-core"    % "1.1.7",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.slf4j"      % "slf4j-api"       % "1.7.21"
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
  .configs(IntegrationTest)
  .settings(baseSettings ++ buildSettings ++ Defaults.itSettings)