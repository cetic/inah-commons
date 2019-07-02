name := "inah-commons"
scalaVersion := "2.12.8"

val akkaVersion = "2.5.13"
val akkaHttpVersion = "10.1.5"
val sprayVersion = "1.3.2"
val scalaTestVersion = "3.0.0"

mainClass in(Compile, run) := Some("be.cetic.inah.commons.Main")
mainClass in(Compile, packageBin) := Some("be.cetic.inah.commons.Main")
mainClass in(Compile, packageBin) := Some("be.cetic.inah.commons.Main")

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
  "io.spray" %%  "spray-json" % sprayVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" %  akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test,


  "com.michaelpollmeier" %% "gremlin-scala" % "3.3.1.2",
  "org.apache.tinkerpop" % "gremlin-driver" % "3.3.1",
  "org.apache.tinkerpop" % "tinkergraph-gremlin" % "3.3.1",

  "org.janusgraph" % "janusgraph-core" % "0.3.1",
  "org.slf4j" % "slf4j-simple" % "1.7.12",
  "org.lightcouch" % "lightcouch" % "0.2.0",


)
