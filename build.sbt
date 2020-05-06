import sbt.Keys.libraryDependencies

name := "inah-commons"
val thisVersion = "0.3.5"
version := thisVersion
scalaVersion := "2.12.8"

val akkaVersion = "2.6.0"
val akkaHttpVersion = "10.1.5"
val sprayVersion = "1.3.2"
val scalaTestVersion = "3.0.0"

enablePlugins(JavaAppPackaging, DockerPlugin)
import NativePackagerHelper._



mainClass in(Compile, run) := Some("be.cetic.inah.commons.DbScripts")
mainClass in(Compile, packageBin) := Some("be.cetic.inah.commons.DbScripts")


assemblyOutputPath in assembly := file("releases/inah-commons-" + thisVersion + ".jar")
test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}


libraryDependencies := Seq(

 //Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion % Provided,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion % Provided,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.1.0" % Provided,

  //sql
  "org.postgresql" % "postgresql" % "42.2.5" % Provided,
  "org.xerial" % "sqlite-jdbc" % "3.28.0" % Provided,
  "com.typesafe.slick" %% "slick" % "3.3.1" % Provided,
  "com.typesafe.slick" %% "slick-codegen" % "3.3.1" % Provided,

  //Spray
  "io.spray" %% "spray-json" % sprayVersion % Provided,

  //Cors
  "ch.megard" %% "akka-http-cors" % "0.4.2" % Provided,

  //Tests
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "org.scalacheck" %% "scalacheck" % "1.13.5"  % Provided,

  //Auth Hash
  "de.mkammerer" % "argon2-jvm" % "2.6" % Provided,

  //Janusgraph
  "org.janusgraph" % "janusgraph-core" % "0.4.0" % Provided,
  "org.janusgraph" % "janusgraph-cassandra" % "0.4.0" % Provided,
  "org.janusgraph" % "janusgraph-cql" % "0.4.0" % Provided,
  "org.janusgraph" % "janusgraph-es" % "0.4.0" % Provided,

  //CouchDb
  "org.lightcouch" % "lightcouch" % "0.2.0" % Provided,

  //Log
  "org.slf4j" % "slf4j-simple" % "1.7.12" % Provided,

  //Scala
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)


test in assembly := {}

packageName in Docker := packageName.value
version in Docker := version.value
//dockerLabels := Map("maintainer" -> "NoReply@steveking.site")
dockerBaseImage := "openjdk"
dockerRepository := Some("teereence")
defaultLinuxInstallLocation in Docker := "/usr/local"
daemonUser in Docker := "daemon"
mappings in Universal ++= directory(baseDirectory.value / "src" / "main" / "resources")