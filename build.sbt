name := "ScheduledCallableExecuter"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.6",
)

libraryDependencies += "joda-time" % "joda-time" % "2.9.9"