package org.lmsviz
package model

object lms {
  case class SourceLocation(file: String, line: Int, offset: Int, parent: Option[SourceLocation]) {
    
    def isAncestorOf(other: SourceLocation): Boolean = {
      var elder = Option(other)
      while (!elder.contains(this) && elder.nonEmpty) {
        elder = elder.flatMap(_.parent)
      }
      elder.contains(this)
    }
  }

  case class StmInfo(id: Int, repr: String, pos: Seq[SourceLocation], comments: Seq[String], parentId: Option[Int], childrens: Seq[StmInfo] = Seq.empty) {
  	def isRelated(other: StmInfo) : Boolean = {
  		val p1 = pos.head
  		val p2 = other.pos.head
  		if(pos.length != 1) println(s"Warning, $id position doenst have only one history ${pos.length}")
  		if(other.pos.length != 1) println(s"Warning, $id position doenst have only one history ${other.pos.length}")
  		p1.isAncestorOf(p2) || p2.isAncestorOf(p1) 	
  	}
  }
  case class TransformInfo(name: String, before: Seq[StmInfo], after: Seq[StmInfo])
}
