import sbt._
import Keys._

object BuildSettings {
    import Dependencies._
    import Resolvers._

    val buildOrganization = "org.quartzsource.meutrino"
    val buildVersion = "0.1"
    val buildScalaVersion = "2.10.4"

    val globalSettings = Seq(
        organization := buildOrganization,
        version := buildVersion,
        scalaVersion := buildScalaVersion,
        scalacOptions += "-deprecation",
        fork in test := true,
        libraryDependencies ++= Seq(scalatest, junit),
        resolvers := Seq(jbossRepo, sonatypeRepo))

    val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
    val sonatypeRepo = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
    val jbossRepo = "JBoss" at "http://repository.jboss.org/nexus/content/groups/public/"
}

object Dependencies {
    val scalatest = "org.scalatest" %% "scalatest" % "2.1.6" % "test"
    val junit = "junit" % "junit" % "4.7" % "test"
    
    val junitInterface = "com.novocode" % "junit-interface" % "0.6" % "test"
    val commonsIo = "commons-io" % "commons-io" % "2.1" % "test"

}

object MeutrinoBuild extends Build {
    import BuildSettings._
    import Dependencies._
    import Resolvers._

    override lazy val settings = super.settings ++ globalSettings

    lazy val root = Project("meutrino",
                           file("."),
                           settings = projectSettings ++
                           Seq(libraryDependencies ++= Seq(junitInterface, commonsIo)))
}
