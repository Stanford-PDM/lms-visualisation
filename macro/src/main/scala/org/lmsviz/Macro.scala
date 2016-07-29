package org.lmsviz

import better.files._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Macro {

  def fileContentAsString(file: String): String = macro fileContentImpl

  def fileContentImpl(c: Context)(file: c.Expr[String]): c.Expr[String] = {
    import c.universe._
    val Literal(Constant(filename: String)) = file.tree
    val betterFile = File(filename)
    val content = if (!betterFile.isRegularFile) {
      c.error(file.tree.pos, s"Could not find file $filename")
      ""
    } else {
      betterFile.contentAsString
    }

    // Split the string into pieces of small enough length to encode in scala-js
    val pieces = content.grouped((1 << 8) - 1).toSeq
    c.Expr(q"Seq(..$pieces).reduce(_ + _)")
  }
}