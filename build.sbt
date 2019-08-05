name := "HMT bifolio browser hack"

crossScalaVersions in ThisBuild := Seq("2.11.8", "2.12.4")
scalaVersion := (crossScalaVersions in ThisBuild).value.last


name := "bifolio"
organization := "org.homermultitext"
version := "0.0.1"

licenses += ("GPL-3.0",url("https://opensource.org/licenses/gpl-3.0.html"))
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",

  "com.github.pathikrit" %% "better-files" % "3.5.0",
  
  "edu.holycross.shot.cite" %% "xcite" % "4.1.0",
  "edu.holycross.shot" %% "citebinaryimage" % "3.1.0"

)
