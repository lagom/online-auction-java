//
// Copyright (C) 2016 Lightbend Inc. <https://www.lightbend.com>
//

// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.3.0-RC2")
// Needed for importing the project into Eclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "3.0.0")
// The ConductR plugin
addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "2.2.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
