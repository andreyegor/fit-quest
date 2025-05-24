import sbtassembly.AssemblyPlugin.autoImport._

val Http4sVersion = "0.23.30"
val MunitVersion = "1.1.0"
val LogbackVersion = "1.5.18"
val MunitCatsEffectVersion = "2.1.0"
val DoobieVersion = "1.0.0-RC9"
val ScryptoVersion = "3.1.0"
val CirceVersion = "0.14.13"
val Argon2JvmVersion = "2.11"
val JwtVersion = "10.0.4"

lazy val root = (project in file("."))
  .settings(
    organization := "t-health",
    name := "t-health-backend",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.6.4",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.scorexfoundation" %% "scrypto" % ScryptoVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "com.github.jwt-scala" %% "jwt-circe" % JwtVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "de.mkammerer" % "argon2-jvm" % Argon2JvmVersion
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    javacOptions ++= Seq("-encoding", "UTF-8"),
    scalacOptions ++= Seq("-encoding", "UTF-8")
  )

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

enablePlugins(JavaServerAppPackaging)
