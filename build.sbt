name := "netty4-example-scala"

version := "0.1.0-SNAPSHOT"

organization := "com.github.kxbmap"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "io.netty"      % "netty-all" % "4.0.0.Final",
  "com.jcraft"    % "jzlib"     % "1.1.2"       % Runtime,
  "org.javassist" % "javassist" % "3.18.0-GA"   % Runtime
)

resolvers += Resolver.sonatypeRepo("snapshots")

fork := true

ideaBasePackage := Some("com.github.kxbmap.netty.example")
