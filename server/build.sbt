// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "rocks.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear        := Some(2021),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "2.13.6",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-Xfatal-warnings",
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    scalafmtOnCompile := true,
    dynverSeparator   := "_", // the default `+` is not compatible with docker tags
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val echo =
  project
    .in(file("."))
    .enablePlugins(
      AkkaGrpcPlugin,
      AutomateHeaderPlugin,
      BuildInfoPlugin,
      DockerPlugin,
      JavaAppPackaging
    )
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaDiscovery,
        library.akkaHttp,
        library.akkaHttp2Support,
        library.akkaHttpSprayJson,
        library.akkaMgmt,
        library.akkaSlf4j,
        library.akkaStreamTyped,
        library.disruptor,
        library.log4jCore,
        library.log4jSlf4j,
        library.pureConfig,
        library.munit           % Test,
        library.munitScalaCheck % Test,
      ),
      // BuildInfo settings
      buildInfoKeys    := Seq[BuildInfoKey](version, scalaVersion),
      buildInfoPackage := "version",
      // Docker settings
      dockerBaseImage     := "adoptopenjdk:11-jre-hotspot",
      dockerRepository    := Some("hseeberger"),
      dockerExposedPorts  := Seq(8080, 8558, 25520),
      Docker / maintainer := organizationName.value,
      // Publish settings
      Compile / packageDoc / publishArtifact := false, // speed up building Docker images
      Compile / packageSrc / publishArtifact := false, // speed up building Docker images
    )
    .settings(commandAliases)

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

lazy val commandAliases =
  addCommandAlias(
    "r1",
    """|reStart
       |---
       |-Dakka.management.http.hostname=[::1]
       |-Decho.http-server.interface=[::1]
       |""".stripMargin
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka       = "2.6.15"
      val akkaHttp   = "10.2.4"
      val akkaMgmt   = "1.1.0"
      val munit      = "0.7.26"
      val disruptor  = "3.4.4"
      val log4j      = "2.14.1"
      val pureConfig = "0.16.0"
    }
    val akkaDiscovery     = "com.typesafe.akka" %% "akka-discovery"       % Version.akka
    val akkaHttp          = "com.typesafe.akka" %% "akka-http"            % Version.akkaHttp
    val akkaHttp2Support  = "com.typesafe.akka" %% "akka-http2-support"   % Version.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val akkaMgmt        = "com.lightbend.akka.management" %% "akka-management"   % Version.akkaMgmt
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"        % Version.akka
    val akkaStreamTyped = "com.typesafe.akka"             %% "akka-stream-typed" % Version.akka
    val disruptor       = "com.lmax"                       % "disruptor"         % Version.disruptor
    val log4jCore       = "org.apache.logging.log4j"       % "log4j-core"        % Version.log4j
    val log4jSlf4j      = "org.apache.logging.log4j"       % "log4j-slf4j-impl"  % Version.log4j
    val munit           = "org.scalameta"                 %% "munit"             % Version.munit
    val munitScalaCheck = "org.scalameta"                 %% "munit-scalacheck"  % Version.munit
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig
  }
