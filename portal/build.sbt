
lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb)

name := "portal"

version := "1.0-SNAPSHOT"

resolvers += Resolver.mavenLocal

scalaVersion := Option(System.getenv("scala.version")).getOrElse("2.11.7")

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.webjars" % "requirejs" % "2.1.11-1",
  ws,
  "uk.gov.dvla.iep.sdl" % "pdf-generator" % "1.2.0-SNAPSHOT",
  "uk.gov.dvla.iep" % "sml-token-domain" % "1.3.4-SNAPSHOT",
  "joda-time" % "joda-time" % "2.9.1"
)
