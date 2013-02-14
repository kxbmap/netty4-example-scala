name := "netty4-example-scala"

version := "0.1.0-SNAPSHOT"

organization := "com.github.kxbmap"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

libraryDependencies += "io.netty" % "netty-all" % "4.0.0.Beta1"

fork := true

ideaBasePackage := Some("com.github.kxbmap.netty.example")
