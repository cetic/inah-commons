import sbt.Keys.libraryDependencies

name := "inah-commons"
val thisVersion = "0.0.3"
version := thisVersion
scalaVersion := "2.12.8"

val akkaVersion = "2.5.13"
val akkaHttpVersion = "10.1.5"
val sprayVersion = "1.3.2"
val scalaTestVersion = "3.0.0"

mainClass in(Compile, run) := Some("be.cetic.inah.commons.Main")
mainClass in(Compile, packageBin) := Some("be.cetic.inah.commons.Main")
mainClass in(Compile, packageBin) := Some("be.cetic.inah.commons.Main")

assemblyOutputPath in assembly := file("releases/inah-commons-" + thisVersion + ".jar")
test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


libraryDependencies := Seq(
  "io.spray" %% "spray-json" % sprayVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.slick" %% "slick" % "3.3.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.3.1",

  "org.postgresql" % "postgresql" % "42.2.5",
  "org.xerial" % "sqlite-jdbc" % "3.28.0",

  "org.janusgraph" % "janusgraph-core" % "0.4.0",
  "org.janusgraph" % "janusgraph-cassandra" % "0.4.0",
  "org.janusgraph" % "janusgraph-cql" % "0.4.0",
  "org.janusgraph" % "janusgraph-es" % "0.4.0",

  "org.slf4j" % "slf4j-simple" % "1.7.12",
  "org.lightcouch" % "lightcouch" % "0.2.0",

  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)
