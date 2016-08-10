package org.lmsviz

import rapture.html._
import htmlSyntax._

object materialize {
  val Container = Div(classes = Seq("container"))
  val MSection = Div(classes = Seq("section"))
  val Row = Div(classes = Seq("row"))

  object Column {
    private def column(prefix: String, size: Int, offset: Int, cls: Seq[String]) = offset match {
      case 0 => Div(classes = Seq("col", s"$prefix$size") ++ cls)
      case n =>
        Div(classes = Seq("col", s"$prefix$size", s"offset-$prefix$n") ++ cls)
    }
    def small(size: Int, offset: Int = 0, classes: Seq[String] = Seq.empty) =
      column("s", size, offset, classes)
    def medium(size: Int, offset: Int = 0, classes: Seq[String] = Seq.empty) =
      column("m", size, offset, classes)
    def large(size: Int, offset: Int = 0, classes: Seq[String] = Seq.empty) =
      column("l", size, offset, classes)
  }

  val MIcon = I(classes = Seq("material-icons"))
  val MediumMIcon = I(classes = Seq("material-icons", "medium"))
  val MIconPrefix = I(classes = Seq("material-icons", "prefix"))
  val ValignWrapper = Div(classes = Seq("valign-wrapper"))
}
