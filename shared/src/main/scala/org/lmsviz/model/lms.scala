package org.lmsviz
package model

object lms {

  case class SourceLocation(file: String, line: Int, offset: Int)
  case class StmInfo(id: Int, repr: String,
      pos: Seq[Seq[SourceLocation]], comments: Map[String, String])
  case class IRNode(stmId: Int, parent: Option[IRNode], children: Seq[IRNode])
  case class TransformInfo(name: String, before: Seq[IRNode], after: Seq[IRNode])
  case class Trace(transforms: Seq[TransformInfo], fullGraph: Seq[StmInfo])
}
