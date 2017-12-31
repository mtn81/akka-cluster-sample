lazy val akkaVersion = "2.5.8"
lazy val akkaHttpVersion = "10.0.10"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      version := "1.0",
      scalaVersion := "2.12.3",

      //docker settings
      maintainer in Docker := "mtn81",
      dockerBaseImage := "williamyeh/java8"
    )),
    name := "akka-sample",
    version := "1.0",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )

lazy val sharding = (project in file("sharding"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "akka-sample-sharding",
    version := "1.0",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),

    mainClass in Compile := Some("myapp.sharding.MyShardingApp2")
  )


lazy val api = (project in file("api"))
  .enablePlugins(JavaAppPackaging)
  .dependsOn(sharding)
  .settings(
    name := "akka-sample-api",
    version := "1.0",
    scalaVersion := "2.12.2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,

      // akka-http
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    ),

    mainClass in Compile := Some("myapp.ApiServer")
  )
