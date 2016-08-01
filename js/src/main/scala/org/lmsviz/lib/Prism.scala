package org.lmsviz
package lib

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@js.native
object Prism extends js.Object {

  @js.native
  type Language

  @js.native
  object languages extends js.Object {
    val scala: Language = js.native
  }

  def highlight(code: String, language: Language): String = js.native
}
