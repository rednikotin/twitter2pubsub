val name = "twitter2pubsub"

val scalaV = "2.12.4"

lazy val root: Project =
  Project(name, file("."))
    .settings(scalaVersion := scalaV)
    .settings(scalariformSupportformatSettings)
    .settings(
      libraryDependencies ++= Seq(
        "com.google.cloud" % "google-cloud-pubsub" % "0.30.0-beta",
        "com.google.auth" % "google-auth-library-oauth2-http" % "0.9.0",
        "net.databinder.dispatch" %% "dispatch-core" % "0.13.2",
        "net.databinder.dispatch" %% "dispatch-json4s-jackson" % "0.13.2",
        "org.slf4j" % "slf4j-api" % "1.7.25",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "com.typesafe" % "config" % "1.3.2"
      ))

import scalariform.formatter.preferences._

def formattingPreferences =
  FormattingPreferences()
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentConstructorArguments, true)

lazy val scalariformSupportformatSettings = SbtScalariform.scalariformSettings ++ Seq(
  SbtScalariform.ScalariformKeys.preferences in Compile := formattingPreferences,
  SbtScalariform.ScalariformKeys.preferences in Test := formattingPreferences
)