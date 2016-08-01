package org.lmsviz

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.document

@JSExport("Main")
object Main extends js.JSApp {
  def main(): Unit = {
    val reactNode = document.getElementById(Constants.ReactContainerID)
    Components.Pipeline(trace) render reactNode
  }

  import model.lms._
  val trace = {

    // Want to do this : CompilationTrace.fromFile("compilation.json")

    // Have to do this instead
    import io.circe.generic.auto._
    import io.circe.parser._

    val content: String = Macro.fileContentAsString("compilation.json")

    decode[Seq[TransformInfo]](content).leftMap { error =>
      println(error.getMessage)
      println(error.toString)
      sys.error("Cannot parse json ast")
    }.merge
  }
}
