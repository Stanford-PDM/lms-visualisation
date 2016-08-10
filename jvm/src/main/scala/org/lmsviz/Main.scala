package org.lmsviz
import rapture.html._
import htmlSyntax._
import rapture.uri._
import rapture.net._
import rapture.css._
import rapture.dom._
import better.files._
import materialize._
import model.lms._

object Main {

  val cwd = RelativePath.Self
  val target = cwd / "js" / "target"
  val lib = target / "web" / "web-modules" / "main" / "webjars" / "lib"

  def MainPage(title: String): HtmlDoc = {
    HtmlDoc(Html(Head(Title(title),
                Link(typ = "text/css", rel = stylesheet,
                    href = lib / "material-design-icons" / "iconfont" / "material-icons.css"),
                Link(typ = "text/css", rel = stylesheet,
                    href = lib / "materialize" / "bin" / "materialize.css"),
                Link(typ = "text/css", rel = stylesheet,
                    href = lib / "prism" / "themes" / "prism.css"),
                Style(Styles.render(scalacss.Defaults.stringRenderer,
                        scalacss.Defaults.env)), // Compiled css
                Style(css""" 
            .token.operator {
              background: none
            }
            """.content), // css added for already existing components in highlighting lib for example
                Meta(name = 'viewport, content = "width=device-width, initial-scale=1.0")),
            Body(Div(id = Symbol(Constants.ReactContainerID)),
                Script(typ = "text/javascript",
                    src = target / "scala-2.11" / "lms-visualisation-jsdeps.js"),
                Script(typ = "text/javascript",
                    src = target / "scala-2.11" / "lms-visualisation-fastopt.js"),
                Script(typ = "text/javascript",
                    src = target / "scala-2.11" / "lms-visualisation-launcher.js"))))
  }

  def main(args: Array[String]): Unit = {
    val index = file"index.html"
    val html = MainPage("Lms Visualisation")
    index.createIfNotExists().overwrite(html.format)
  }

}
