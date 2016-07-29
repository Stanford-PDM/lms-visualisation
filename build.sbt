name := "lms-visualisation root project"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

lazy val root = project.in(file(".")).
  aggregate(lmsvizJS, lmsvizJVM, macro).
  dependsOn(macro).
  settings(
    publish := {},
    publishLocal := {}
  )

val crossType = CrossType.Full
val circeVersion = "0.4.1"
val materializeVersion = "0.97.6"
val prismVersion = "1.5.1"
val reactVersion = "15.2.1"
val betterFilesVersion = "2.16.0"

lazy val macro = project.in(file("macro")).settings(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.github.pathikrit" %% "better-files" % betterFilesVersion
  )
)

lazy val lmsviz = crossProject.crossType(crossType).in(file(".")).
  enablePlugins(SbtWeb).
  settings(
    name := "lms-visualisation",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      // Css :
      "org.webjars" % "material-design-icons" % "2.2.0",
      "org.webjars.bower" % "materialize" % materializeVersion,
      "org.webjars.bower" % "prism" % prismVersion,

      // Shared libraries
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion ,
      "com.github.japgolly.scalacss" %%% "core" % "0.4.1"
    )
  ) jvmSettings ( 
    // JVM-specific settings
    libraryDependencies ++= Seq(
      "com.propensive" %% "rapture" % "2.0.0-M6",
      "com.github.pathikrit" %% "better-files" % betterFilesVersion
    )
  ) jsSettings ( 
    // JS-specific settings
    persistLauncher in Compile := true,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1",
       "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
    ),
    jsDependencies ++= Seq(
      // JQuery (simplified dom operations)
      "org.webjars.bower" % "jquery" % "3.1.0"
      /             "dist/jquery.js"
      minified      "dist/jquery.min.js"
      commonJSName  "JQuery",

      // React (reactive dom modifications)
      "org.webjars.bower" % "react" % reactVersion
      /             "react-with-addons.js"
      minified      "react-with-addons.min.js"
      commonJSName  "React",

      "org.webjars.bower" % "react" % reactVersion
      /             "react-dom.js"
      minified      "react-dom.min.js"
      dependsOn     "react-with-addons.js"
      commonJSName  "ReactDOM",

      // Prism (syntax coloring)
      "org.webjars.bower" % "prism" % prismVersion
      /             "prism.js"
      commonJSName  "Prism",

      "org.webjars.bower" % "prism" % prismVersion
      /             "components/prism-java.js"
      minified      "components/prism-java.min.js"
      dependsOn     "prism.js",

      "org.webjars.bower" % "prism" % prismVersion
      /             "components/prism-scala.js"
      minified      "components/prism-scala.min.js"
      dependsOn     "components/prism-java.js",

      // Materialize (css framework)
      "org.webjars.bower" % "materialize" % materializeVersion
      /             "bin/materialize.js"
      minified      "dist/js/materialize.min.js"
      dependsOn     "dist/jquery.js"
      commonJSName  "Materialize"
    )
  )


lazy val lmsvizJVM = lmsviz.jvm
lazy val lmsvizJS = lmsviz.js.dependsOn(macro).aggregate(macro)
