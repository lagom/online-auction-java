organization in ThisBuild := "com.example"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.7"

lazy val itemApi = project("item-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val itemImpl = project("item-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistence,
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(itemApi)

lazy val biddingApi = project("bidding-api")
  .settings(version := "1.0-SNAPSHOT")
  .settings(
    libraryDependencies += lagomJavadslApi
  )

lazy val biddingImpl = project("bidding-impl")
  .settings(version := "1.0-SNAPSHOT")
  // .enablePlugins(LagomJava)
  .dependsOn(biddingApi, itemApi)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistence,
      lagomJavadslTestKit
    ),
    javacOptions += "-Xlint",
    maxErrors := 10000

  )

lazy val searchApi = project("search-api")
  .settings(version := "1.0-SNAPSHOT")
  .settings(
    libraryDependencies += lagomJavadslApi
  )

lazy val searchImpl = project("search-impl")
  .settings(version := "1.0-SNAPSHOT")
  // .enablePlugins(LagomJava)
  .dependsOn(searchApi, itemApi, biddingApi)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistence,
      lagomJavadslTestKit
    )
  )

lazy val transactionApi = project("transaction-api")
  .settings(version := "1.0-SNAPSHOT")
  .dependsOn(itemApi)
  .settings(
    libraryDependencies += lagomJavadslApi
  )

lazy val transactionImpl = project("transaction-impl")
  .settings(version := "1.0-SNAPSHOT")
  // .enablePlugins(LagomJava)
  .dependsOn(transactionApi, biddingApi)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistence,
      lagomJavadslTestKit
    )
  )

lazy val userApi = project("user-api")
  .settings(version := "1.0-SNAPSHOT")
  .settings(
    libraryDependencies += lagomJavadslApi
  )

lazy val userImpl = project("user-impl")
  .settings(version := "1.0-SNAPSHOT")
  .enablePlugins(LagomJava)
  .dependsOn(userApi)
  .settings(
    libraryDependencies += lagomJavadslPersistence
  )

lazy val webGateway = project("web-gateway")
  .settings(version := "1.0-SNAPSHOT")
  .enablePlugins(PlayJava && LagomPlay)
  .dependsOn(transactionApi, biddingApi, itemApi, searchApi, userApi)
  .settings(
    libraryDependencies += lagomJavadslClient
  )

def project(id: String) = Project(id, base = file(id))
  .settings(eclipseSettings: _*)
  .settings(javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))

// Configuration of sbteclipse
// Needed for importing the project into Eclipse
lazy val eclipseSettings = Seq(
  EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
  EclipseKeys.withBundledScalaContainers := false,
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  EclipseKeys.eclipseOutput := Some(".target"),
  EclipseKeys.withSource := true,
  EclipseKeys.withJavadoc := true,
  // avoid some scala specific source directories
  unmanagedSourceDirectories in Compile := Seq((javaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((javaSource in Test).value)
)

lagomCassandraCleanOnStart := false
