name := "netty4-example-scala"

version := "0.1.0-SNAPSHOT"

organization := "com.github.kxbmap"

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "io.netty"   % "netty-all" % "4.0.0.CR2", 
  "com.jcraft" % "jzlib"     % "1.1.2",
  "javassist"  % "javassist" % "3.12.1.GA"
)

resolvers += Resolver.sonatypeRepo("snapshots")

fork := true

ideaBasePackage := Some("com.github.kxbmap.netty.example")
