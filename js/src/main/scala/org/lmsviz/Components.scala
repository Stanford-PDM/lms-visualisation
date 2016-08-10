package org.lmsviz

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import model.lms._
import lib.Prism
import org.scalajs.dom.html

object Components {

  object Materialize {

    // TODO: type this a bit better to enforce some functions only on certain nodes
    implicit class PimpedDiv[T <: TopNode](val d: ReactTagOf[T]) extends AnyVal {
      def withClass(cls: String): ReactTagOf[T] = d(classSet1(cls))

      def s(numCols: Int) = withClass(s"s$numCols")
      def m(numCols: Int) = withClass(s"m$numCols")
      def l(numCols: Int) = withClass(s"l$numCols")

      def offsetS(numCols: Int) = withClass(s"offset-s$numCols")
      def offsetM(numCols: Int) = withClass(s"offset-m$numCols")
      def offsetL(numCols: Int) = withClass(s"offset-l$numCols")

      def centered = withClass("center")
      def left = withClass("left")
      def right = withClass("right")

      def tiny = withClass("tiny")
      def small = withClass("small")
      def medium = withClass("medium")
      def large = withClass("large")

      def prefix = withClass("prefix")

      def disabled(bool: Boolean = true) = if (bool) withClass("disabled") else d

      def valignWrapper = withClass("valign-wrapper")
      def valigned = withClass("valign")
    }

    object btn {
      val flat = "btn-flat"
      val floating = "btn-floating"
      val large = "btn-large"
    }

    def btn(classes: String*) = classes.foldLeft(a)(_.withClass(_))

    val row = div.withClass("row")
    val col = div.withClass("col")
    val section = div.withClass("section")
    val card = div.withClass("card-panel")

    def micon(name: String) = i.withClass("material-icons")(name)
  }

  /** Returns true is a is a suffix of b */
  def isSuffixOf(a: Seq[SourceLocation], b: Seq[SourceLocation]): Boolean = {
    a == b || (b.nonEmpty && isSuffixOf(a, b.tail))
  }

  def areRelated(a: StmInfo, b: StmInfo): Boolean = {
    //TODO: don't quite understand why there are many source location histories, so for now just use the longest one
    val hist1 = a.pos.maxBy(_.length)
    val hist2 = b.pos.maxBy(_.length)
    isSuffixOf(hist1, hist2) || isSuffixOf(hist2, hist1)
  }

  import Materialize._

  // TODO: is it possible to use Rapture syntax instead of scalatags for this ?

  // Helper : TODO: this is annoying, it would be nice to get rid of
  import scala.language.dynamics
  object data extends Dynamic {
    def selectDynamic(field: String) = s"data-$field".reactAttr
  }

  ////////////
  // Statement
  object Stm {
    case class Props(stm: StmInfo, handleSelect: StmInfo => Callback, isFocused: Boolean,
        isRelated: Boolean, level: Int)
    type State = Unit
    class Backend($ : BackendScope[Props, State]) {

      def handleClick(e: ReactEventI): Callback = {
        e.preventDefaultCB >> $.props >>= { props =>
          props.handleSelect(props.stm)
        }
      }

      def render(props: Props, state: State) = {
        val stm = props.stm

        li(key := stm.id)(div(classSet1("collapsible-header truncate",
                    Styles.stm(props.level).htmlClass -> true,
                    Styles.focusedStm.htmlClass -> props.isFocused,
                    Styles.relatedStm.htmlClass -> (!props.isFocused && props.isRelated)),
                onClick ==> handleClick,
                span(dangerouslySetInnerHtml(Prism.highlight(stm.repr, Prism.languages.scala)))))
      }
    }
    val element = ReactComponentB[Props]("Stm").renderBackend[Backend].build

    def apply(props: Props, children: ReactNode*) = element.apply(props, children: _*)
    def apply(stm: StmInfo, handleSelect: StmInfo => Callback, isFocused: Boolean,
        isRelated: Boolean, level: Int) =
      element.apply(Props(stm, handleSelect, isFocused, isRelated, level))

  }

  ////////////
  // Ast
  object Ast {
    case class Props(statements: Seq[IRNode], fullGraph: Seq[StmInfo],
        symbolsMap: Map[Int, StmInfo], handleSelect: StmInfo => Callback,
        focusedStm: Option[StmInfo], open: Set[StmInfo])
    type State = Unit

    class Backend($ : BackendScope[Props, State]) {

      def render(props: Props, state: State) = {
        val statements = props.statements
        val symbolsMap = props.symbolsMap

        def renderStatement(node: IRNode, level: Int): Seq[ReactNode] = {
          val stm = symbolsMap(node.stmId)
          val isFocused = props.focusedStm.contains(stm)
          val isRelated = props.focusedStm.exists(areRelated(_, stm))

          val tag = Stm(stm, props.handleSelect, isFocused, isRelated, level)

          if (props.open.contains(stm)) {
            // render children
            tag +: node.children.flatMap(renderStatement(_, level + 1))
          } else {
            Seq(tag)
          }
        }

        ul(Seq(classSet1(Styles.ast.htmlClass), data.collapsible := "expandable"))(
            // force implicit conversion, TODO: find what is happening
            statements.flatMap(renderStatement(_, 0)).map(x => { val t: TagMod = x; t }): _*)
      }
    }

    // we need this type because of the recursive definition
    val element = ReactComponentB[Props]("Ast").stateless.renderBackend[Backend].build

    def apply(props: Props, children: ReactNode*) = element.apply(props, children: _*)
    def apply(statements: Seq[IRNode], fullGraph: Seq[StmInfo], symbolsMap: Map[Int, StmInfo],
        handleSelect: StmInfo => Callback, focusedStm: Option[StmInfo], open: Set[StmInfo]) =
      element.apply(Props(statements, fullGraph, symbolsMap, handleSelect, focusedStm, open))

  }

  ////////////
  // Transform
  object Transform {
    case class Props(transform: TransformInfo, fullGraph: Seq[StmInfo],
        symbolsMap: Map[Int, StmInfo]) {

      private def buildNodeIndex(schedule: Seq[IRNode]): Map[Int, IRNode] = {
        if (schedule.isEmpty) {
          Map.empty
        } else {
          schedule.map(node => node.stmId -> node).toMap ++ buildNodeIndex(
              schedule.flatMap(_.children))
        }
      }

      // index from stmid to IRNode
      private val nodeIndex = buildNodeIndex(transform.before) ++ buildNodeIndex(transform.after)

      // All the stms id in the current view
      private val idsInView = nodeIndex.keys.toSet
      private val transformScope = fullGraph.filter(stm => idsInView.contains(stm.id))

      def getStatement(id: Int) = symbolsMap(id)

      // get all statements related to 
      def getAllRelated(selected: StmInfo): Seq[StmInfo] = {
        transformScope.filter(stm => stm != selected && areRelated(selected, stm))
      }

      def allParents(stm: StmInfo): Seq[StmInfo] = {
        val node = nodeIndex(stm.id)
        def allParentNodes(node: IRNode): Seq[IRNode] =
          node +: node.parent.toSeq.flatMap(allParentNodes)
        allParentNodes(node).map(n => symbolsMap(n.stmId))
      }

    }
    case class State(focusedStm: Option[StmInfo], open: Set[StmInfo])

    class Backend($ : BackendScope[Props, State]) {

      def updateState(s: State, selected: StmInfo, related: Seq[StmInfo]): State = {
        val open = if (!s.open.contains(selected)) {
          // click on closed statement -> open
          s.open + selected
        } else if (s.focusedStm.contains(selected) && s.open.contains(selected)) {
          // close the statement only if we click twice on it
          s.open - selected
        } else {
          s.open
        }

        // open all related statements
        s.copy(open = open ++ related, focusedStm = Some(selected))
      }

      def handleSelect(stm: StmInfo) =
        /*Callback {
          println(s"Selected ${stm.id}");
          stm.pos.foreach { loc =>
            def print(level: Int, loc: Seq[SourceLocation]): Unit = {
              if (loc.nonEmpty) {
                val SourceLocation(file, line, offset) = loc.head
                println("." * level + s" $file:$line:$offset")
                print(level + 1, loc.tail)
              }
            }
            print(0, loc)
          }
        } >>*/ $.props >>= { p =>
          val related = p.getAllRelated(stm)
          val parents = related.flatMap(p.allParents)
          $.modState(updateState(_, stm, parents))
        }

      def render(props: Props, state: State) = {
        val transform = props.transform
        val focusedStm = state.focusedStm
        row(
            col
              .s(8)
              .offsetS(2)(card(classSet1(Styles.card.htmlClass))(
                      pre(classSet1(Styles.code.htmlClass))(focusedStm.fold {
                span("Current node info")
              } { stm =>
                val comment = stm.comments.mkString("/*\n * ", "\n * ", "\n */\n")
                val highlited = Prism.highlight(comment + stm.repr, Prism.languages.scala)
                span(dangerouslySetInnerHtml(highlited))
              }))),
            col.s(6)(Ast(transform.before, props.fullGraph, props.symbolsMap, handleSelect _,
                    focusedStm, state.open)),
            col.s(6)(Ast(transform.after, props.fullGraph, props.symbolsMap, handleSelect _,
                    focusedStm, state.open)))
      }
    }

    val element = ReactComponentB[Props]("Transform")
      .initialState(State(focusedStm = None, open = Set.empty))
      .renderBackend[Backend]
      .build

    def apply(props: Props, children: ReactNode*) = element.apply(props, children: _*)
    def apply(transform: TransformInfo, fullGraph: Seq[StmInfo], symbolsMap: Map[Int, StmInfo]) =
      element.apply(Props(transform, fullGraph, symbolsMap))
  }

  ////////////
  // Pipeline
  object Pipeline {
    implicit class Props(val trace: Trace) {
      val symbolsMap: Map[Int, StmInfo] = trace.fullGraph.map(stm => stm.id -> stm).toMap
    }

    case class State(transformIdx: Int, focusedStm: Option[StmInfo])
    class Backend($ : BackendScope[Props, State]) {

      def handleNext = $.modState(s => s.copy(transformIdx = s.transformIdx + 1))
      def handlePrev = $.modState(s => s.copy(transformIdx = s.transformIdx - 1))

      def hanleSelect(stm: StmInfo) = $.modState(s => s.copy(focusedStm = Some(stm)))

      def render(props: Props, state: State) = {
        val transforms = props.trace.transforms
        val idx = state.transformIdx
        val currentTransform = transforms(idx)
        val previous = btn(btn.large, btn.floating)
          .disabled(idx == 0)
          .valignWrapper(onClick --> handlePrev)(micon("arrow_back").valigned)
          .left
        val next = btn(btn.large, btn.floating)
          .disabled(idx == (transforms.length - 1))
          .valignWrapper(onClick --> handleNext)(micon("arrow_forward").valigned)
          .right
        section(row(col.s(8).offsetS(2).centered(h3(previous, s"#${idx + 1}: ${currentTransform.name}", next)),
                col.s(12)(Transform(currentTransform, props.trace.fullGraph, props.symbolsMap))))
      }
    }

    val element = ReactComponentB[Props]("Pipeline")
      .initialState(State(transformIdx = 0, focusedStm = None))
      .renderBackend[Backend]
      .build

    def apply(props: Props, children: ReactNode*) = element.apply(props, children: _*)
  }
}
