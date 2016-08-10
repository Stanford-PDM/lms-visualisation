package org.lmsviz

import scalacss.Defaults._

object Styles extends StyleSheet.Inline {
  import dsl._

  val stm = styleF.int(0 to 100)(i => styleS(borderLeft((i * 10).px, solid, gainsboro)))

  val ast = style(addClassName("collapsible"), boxShadow := none)

  val focusedStm = style(backgroundColor.beige)

  val relatedStm = style(backgroundColor(c"#eee"))

  val code = style(margin(0.px))

  val card = style(padding(10.px))
}
