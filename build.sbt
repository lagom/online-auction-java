organization in ThisBuild := "com.example"

scalaVersion in ThisBuild := "2.11.8"

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
  .settings(commonSettings: _*)

lazy val security = (project in file("security"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslServer % Optional
    )
  )


lazy val testkit = (project in file("testkit"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslPersistenceCassandra
    )
  )
  .dependsOn(tools)

lazy val itemApi = (project in file("item-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val itemImpl = (project in file("item-impl"))
  .settings(commonSettings: _*)
  .settings(kafkaSettings: _*)
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker,
      "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"
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
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security)

lazy val biddingImpl = (project in file("bidding-impl"))
  .settings(commonSettings: _*)
  .settings(kafkaSettings: _*)
  .enablePlugins(LagomJava)
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
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, tools)

lazy val searchImpl = (project in file("search-impl"))
  .settings(commonSettings: _*)
  .settings(kafkaSettings: _*)
  .enablePlugins(LagomJava)
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
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )


lazy val transactionApi = (project in file("transaction-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security, itemApi)

lazy val transactionImpl = (project in file("transaction-impl"))
  .settings(commonSettings: _*)
  .settings(kafkaSettings: _*)
  .enablePlugins(LagomJava)
  .dependsOn(transactionApi, itemApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker
    )
  )

lazy val userApi = (project in file("user-api"))
  .settings(commonSettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )
  .dependsOn(security)

lazy val userImpl = (project in file("user-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomJava)
  .dependsOn(userApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      "de.svenkubiak" % "jBCrypt" % "0.4"
    )
  )

lazy val webGateway = (project in file("web-gateway"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayJava && LagomPlay)
  .dependsOn(tools, transactionApi, biddingApi, itemApi, searchApi, userApi, searchApi)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient,
      "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    ),
    // Workaround for https://github.com/lagom/online-auction-java/issues/22
    // Uncomment the commented out line and remove the Scala line when issue #22 is fixed
    EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Scala,
    // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
    EclipseKeys.preTasks := Seq(compile in Compile)
  )

val lombok = "org.projectlombok" % "lombok" % "1.16.10"

def elasticsearch: String = {
  val enableElasticsearch = sys.props.getOrElse("enableElasticsearch", default = "false")
  if (enableElasticsearch == "true") {
    "--include-categories=com.example.auction.search.impl.ElasticsearchTests"
  } else {
    "--exclude-categories=com.example.auction.search.impl.ElasticsearchTests"
  }
}

def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)

// Include this into impl projects that use the message broker API
// It overrides the production configuration to use a hardcoded Kafka broker
// host and port rather than looking it up from the service locator.
// See docs/running-in-conductr.md for details.
def kafkaSettings: Seq[Setting[_]] = Seq(
  BundleKeys.startCommand ++= Seq(
    "-Dlagom.broker.kafka.service-name=''",
    // You may have to edit this list if your Kafka
    // server is not listening on 127.0.0.1:9092
    "-Dlagom.broker.kafka.brokers='127.0.0.1:9092'"
  )
)

lagomCassandraCleanOnStart in ThisBuild := false

// ------------------------------------------------------------------------------------------------

// register 'elastic-search' as an unmanaged service on the service locator so that at 'runAll' our code
// will resolve 'elastic-search' and use it. See also com.example.com.ElasticSearch
lagomUnmanagedServices in ThisBuild += ("elastic-search" -> "http://127.0.0.1:9200")

