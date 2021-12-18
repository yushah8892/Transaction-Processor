
name := "TransactionProcesser"

version := "0.1"

scalaVersion := "2.11.0"

val AkkaVersion = "2.5.21"


lazy val commonSettings = Seq(
  organization := "com.thesis",
  version := "0.1.0-SNAPSHOT"
)

//lazy val app = (project in file(".")).
//  settings(commonSettings: _*).
//  settings(
//    name := "fat-jar-test"
//  ).
//  enablePlugins(AssemblyPlugin)
enablePlugins(JavaAppPackaging)

resolvers in Global ++= Seq(
  "Sbt plugins"                   at "https://dl.bintray.com/sbt/sbt-plugin-releases",
  "Maven Central Server"          at "https://repo1.maven.org/maven2",
  "TypeSafe Repository Releases"  at "https://repo.typesafe.com/typesafe/releases/",
  "TypeSafe Repository Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/"
)


libraryDependencies ++= {
  Seq(

    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "net.liftweb" %% "lift-json" % "3.1.0",
    "net.debasishg" %% "redisclient" % "3.41",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.59",
  "org.apache.kafka" %% "kafka" % "2.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.typesafe" % "config" % "1.3.1"
  )
}

mappings in Universal += {
  val conf = (resourceDirectory in Compile).value / "application.conf"
  conf -> "conf/application.conf"
}
//mainClass in assembly := Some("com.thesis.consumer.consumerFromKafka")

//mappings in Docker += {
//  val conf = (resourceDirectory in Compile).value / "application.conf"
//  conf -> "/opt/docker/conf/application.conf"
//}