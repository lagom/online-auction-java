organization in ThisBuild := "com.example"

scalaVersion in ThisBuild := "2.12.8"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java


lazy val root = (project in file("."))
  .settings(name := "online-auction-java")
  .aggregate(
    tools, testkit, security,
    itemApi, itemImpl,
    biddingApi, biddingImpl,
    userApi, userImpl,
    transactionApi, transactionImpl,
    searchApi, searchImpl,
    webGateway)
  .settings(commonSettings)

lazy val security = (project in file("security"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslServer % Optional
    )
  )


lazy val testkit = (project in file("testkit"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslPersistenceCassandra
    )
  )
  .dependsOn(tools)

lazy val itemApi = (project in file("item-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val itemImpl = (project in file("item-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker,
      cassandraExtras
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(
    tools,
    testkit % "test",
    itemApi,
    biddingApi
  )

lazy val biddingApi = (project in file("bidding-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security)

lazy val biddingImpl = (project in file("bidding-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(biddingApi, itemApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker
    ),
    maxErrors := 10000

  )

lazy val searchApi = (project in file("search-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val searchImpl = (project in file("search-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      lagomJavadslKafkaClient,
      lombok
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.JUnit, elasticsearch)
  )
  .dependsOn(tools, searchApi, itemApi, biddingApi)

lazy val tools = (project in file("tools"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )


lazy val transactionApi = (project in file("transaction-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, itemApi)

lazy val transactionImpl = (project in file("transaction-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(
    transactionApi,
    itemApi,
    tools,
    testkit % "test"
  ).settings(
  version := "1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    lagomJavadslPersistenceCassandra,
    lagomJavadslTestKit,
    lagomJavadslKafkaBroker,
    cassandraExtras
  )
)

lazy val userApi = (project in file("user-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val userImpl = (project in file("user-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(userApi, tools,
    testkit % "test"
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      "de.svenkubiak" % "jBCrypt" % "0.4.1",
      lagomJavadslKafkaBroker,
      cassandraExtras,
      lombok
    )
  )

lazy val webGateway = (project in file("web-gateway"))
  .settings(commonSettings)
  .enablePlugins(PlayJava, LagomPlay, SbtReactiveAppPlugin)
  .disablePlugins(PlayLayoutPlugin) // use the standard sbt layout... src/main/java, etc.
  .dependsOn(tools, transactionApi, biddingApi, itemApi, searchApi, userApi, searchApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient,
      "org.ocpsoft.prettytime" % "prettytime" % "4.0.2.Final",
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    ),

    PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value,

    // Workaround for https://github.com/lagom/online-auction-java/issues/22
    // Uncomment the commented out line and remove the Scala line when issue #22 is fixed
    EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.ScalaIDE,
    // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
    EclipseKeys.preTasks := Seq(compile in Compile)
  )

val lombok = "org.projectlombok" % "lombok" % "1.18.4"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.6.0"

def elasticsearch: String = {
  val enableElasticsearch = sys.props.getOrElse("enableElasticsearch", default = "false")
  if (enableElasticsearch == "true") {
    "--include-categories=com.example.auction.search.impl.ElasticsearchTests"
  } else {
    "--exclude-categories=com.example.auction.search.impl.ElasticsearchTests"
  }
}

def evictionSettings: Seq[Setting[_]] = Seq(
  // This avoids a lot of dependency resolution warnings to be showed.
  // They are not required in Lagom since we have a more strict whitelist
  // of which dependencies are allowed. So it should be safe to not have
  // the build logs polluted with evictions warnings.
  evictionWarningOptions in update := EvictionWarningOptions.default
    .withWarnTransitiveEvictions(false)
    .withWarnDirectEvictions(false)
)

def commonSettings: Seq[Setting[_]] = eclipseSettings ++ evictionSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror"),
  scalacOptions += "-feature"
)

lagomCassandraCleanOnStart in ThisBuild := false

// ------------------------------------------------------------------------------------------------

// register 'elastic-search' as an unmanaged service on the service locator so that at 'runAll' our code
// will resolve 'elastic-search' and use it. See also com.example.com.ElasticSearch
lagomUnmanagedServices in ThisBuild += ("elastic-search" -> "http://127.0.0.1:9200")
