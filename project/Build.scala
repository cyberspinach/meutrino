import sbt._
import Keys._

object BuildSettings {
    import Dependencies._
    import Resolvers._

    val buildOrganization = "org.quartzsource.meutrino"
    val buildVersion = "0.1"
    val buildScalaVersion = "2.9.1"

    val globalSettings = Seq(
        organization := buildOrganization,
        version := buildVersion,
        scalaVersion := buildScalaVersion,
        scalacOptions += "-deprecation",
        fork in test := true,
        libraryDependencies ++= Seq(scalatest, junit),
        resolvers := Seq(scalaToolsRepo, jbossRepo,
                         akkaRepo, sonatypeRepo))

    val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
    val sonatypeRepo = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
    val scalaToolsRepo = "Scala Tools" at "http://scala-tools.org/repo-snapshots/"
    val jbossRepo = "JBoss" at "http://repository.jboss.org/nexus/content/groups/public/"
    val akkaRepo = "Akka" at "http://akka.io/repository/"
}

object Dependencies {
    val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    val junit = "junit" % "junit" % "4.7" % "test"
    
    val junitInterface = "com.novocode" % "junit-interface" % "0.6" % "test"

    val akka = "se.scalablesolutions.akka" % "akka-actor" % "1.2"
    val akkaHttp = "se.scalablesolutions.akka" % "akka-http" % "1.2"

}

object MeutrinoBuild extends Build {
    import BuildSettings._
    import Dependencies._
    import Resolvers._

    override lazy val settings = super.settings ++ globalSettings

    lazy val root = Project("meutrino",
                           file("."),
                           settings = projectSettings ++
                           Seq(libraryDependencies ++= Seq(akka, junitInterface)))
}
