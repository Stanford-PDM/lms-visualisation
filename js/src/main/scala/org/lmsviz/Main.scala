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
    import io.circe.generic.auto._
    import io.circe.parser._

    val content: String = Macro.fileContentAsString("trace.min.json")

    decode[Trace](content).leftMap { error =>
      println(error.getMessage)
      println(error.toString)
      sys.error("Cannot parse json ast")
    }.merge
  }

  val stmMap = trace.fullGraph.map(stm => stm.id -> stm).toMap

}

// Js interface to retrieve symbols
@JSExport("DB")
object DB {

  import js.JSConverters._

  @JSExport
  def getInfo(id: Int) = {
    val stm = Main.stmMap(id)
    Map("repr" -> stm.repr, "info" -> stm.comments.toJSDictionary).toJSDictionary
  }
}
