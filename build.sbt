import com.typesafe.tools.mima.core._

Global / onChangedBuildSource := ReloadOnSourceChanges
// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.1" // your current series x.y

ThisBuild / organization := "com.github.zainab-ali"
ThisBuild / organizationName := "Zainab Ali"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("zainab-ali", "Zainab Ali")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := true

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala3 = "3.4.2"
ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(
    name := "aquascape",
    fork := true,
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % "3.10.2",
      "co.fs2" %%% "fs2-io" % "3.10.2",
      ("org.creativescala" %%% "doodle-core" % "0.22.0"),
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "org.typelevel" %% "cats-effect-testkit" % "3.5.4" % Test,
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      ("org.scalameta" %%% "scalameta" % "4.9.9" % Test)
        .cross(CrossVersion.for3Use2_13),
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
      ("com.lihaoyi" %%% "pprint" % "0.9.0" % Test)
        .cross(CrossVersion.for3Use2_13)
    ),
    buildInfoKeys := Seq[BuildInfoKey](ThisBuild / baseDirectory),
    buildInfoPackage := "aquascape",
    mimaBinaryIssueFilters += ProblemFilters
      .exclude[DirectMissingMethodProblem](
        "aquascape.drawing.Config.minProgressWidth"
      )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.creativescala" %% "doodle-java2d" % "0.22.0",
      ("org.scalameta" %% "scalafmt-core" % "3.8.3" % Test)
        .cross(CrossVersion.for3Use2_13)
    )
  )
  .jsSettings(
    Test / fork := false,
    libraryDependencies += ("org.creativescala" %%% "doodle-svg" % "0.22.0")
      .excludeAll(
        "com.lihaoyi"
      ), // Both doodle-svg and pprint include sourcecode.
    mimaPreviousArtifacts := Set.empty
  )
  .enablePlugins(BuildInfoPlugin)

import laika.format._
import laika.ast.Path.Root

lazy val docs = project
  .in(file("site"))
  .dependsOn(core.jvm)
  .settings(
    tlSiteKeepFiles := false,
    tlSiteHelium := tlSiteHelium.value.site
      .internalCSS(Root / "main.css"),
    tlSite := Def
      .sequential(
        Compile / clean,
        (core.jvm / Test / test),
        mdoc.toTask(""),
        laikaSite
      )
      .value
  )
  .enablePlugins(TypelevelSitePlugin)
