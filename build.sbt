import sbtassembly.AssemblyPlugin.autoImport._
import com.earldouglas.xwp.JettyPlugin

val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")
val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
val liftModule = project in file("liftModule")


val buildOrganization = "eu.getintheloop"
val buildVersion      = "0.0.9-SNAPSHOT"
val buildScalaVersion = "2.11.12"

val buildSettings = Seq (
  liftVersion := (liftVersion ?? "3.0").value,
  liftEdition := (liftVersion apply { _.substring(0,3) }).value,
  moduleName  := name.value + "_" + liftEdition.value,
  organization := buildOrganization,
  version      := buildVersion,
  scalaVersion := buildScalaVersion,
  scalacOptions := scalaVersion.map{ v: String =>
    val opts = "-deprecation" :: "-unchecked" :: Nil
    if (v.startsWith("2.9.")) opts else opts ++ ( "-language:implicitConversions" :: "-language:postfixOps" :: Nil)
  }.value,
  crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.4", "2.11.1", "2.11.12"),
  resolvers ++= Seq(
    "CB Central Mirror" at "https://repo.cloudbees.com/content/groups/public",
    "Shiro Releases" at "https://repository.apache.org/content/repositories/releases/",
    "Shiro Snapshots" at "https://repository.apache.org/content/repositories/snapshots/",
    "sonatype.repo" at "https://oss.sonatype.org/content/repositories/public/"
  ),
  assembly / assemblyJarName := (moduleName.value + "_" + scalaVersion.value.substring(0,4) + "-" + version.value + ".jar"),
  assembly / assemblyOption  ~= { _.withIncludeScala(false) },
  publishTo := version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  }.value,
  credentials ++= Seq(
    Credentials(Path.userHome / ".ivy2" / ".credentials"),
    Credentials( file("sonatype.credentials") ),
    Credentials( file("/private/liftmodules/sonatype.credentials") )
  ),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { repo => false },
  pomExtra := (
    <url>https://github.com/timperrett/lift-shiro</url>
      <licenses>
        <license>
          <name>Apache 2.0 License</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:timperrett/lift-shiro.git</url>
        <connection>scm:git:git@github.com:timperrett/lift-shiro.git</connection>
      </scm>
      <developers>
        <developer>
          <id>timperrett</id>
          <name>Timothy Perrett</name>
          <url>http://timperrett.com</url>
        </developer>
        <developer>
          <id>fmpwizard</id>
          <name>Diego Medina</name>
          <url>http://www.fmpwizard.com</url>
        </developer>
      </developers>)
)


lazy val `lift-shiro-root` = (project in file("."))
  .aggregate(`lift-shiro`, `lift-shiro-example`)
  .settings(
    buildSettings,
    // the root is just an aggregator so don't publish a JAR
    Compile / packageBin / publishArtifact := false,
    Test    / packageBin / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false
  )


lazy val `lift-shiro` = (project in file("library"))
  .settings(
    buildSettings,
    libraryDependencies ++= Seq(
      "org.apache.shiro" % "shiro-core" % "1.3.2",
      "org.apache.shiro" % "shiro-web"  % "1.3.2",
      "commons-beanutils" % "commons-beanutils" % "1.11.0",
      liftVersion("net.liftweb" %% "lift-webkit" % _ % "provided").value
    )
  )


lazy val `lift-shiro-example` = (project in file("example"))
  .dependsOn(`lift-shiro`)
  .enablePlugins(JettyPlugin)
  .settings(
    buildSettings,
    libraryDependencies ++= Seq(
      "net.liftmodules"   %% "fobo-jquery_3.0"  % "1.7"                             % "compile",
      "org.eclipse.jetty" % "jetty-webapp"      % JettyPlugin.jettyRunner.revision  % "container",
      "org.eclipse.jetty" % "jetty-plus"        % JettyPlugin.jettyRunner.revision  % "container",
      "javax.servlet"     % "javax.servlet-api" % "3.1.0"                           % "provided",
      "ch.qos.logback"    % "logback-classic"   % "1.2.13",
      liftVersion("net.liftweb" %% "lift-webkit" % _ % "compile").value
    )
  )