val baseSettings = Seq(
  organization := "net.randallalexander",
  scalaVersion := "2.11.8"
)

val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xfuture",
  "-target:jvm-1.8",
  "-Xfatal-warnings"
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

  //libraryDependencies ++= Seq(),
  
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