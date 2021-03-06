package widgets

import common.widgets.{Box, OutputArea}
import preo.DSL
import preo.ast.{Connector, CoreConnector}
import preo.frontend.{Eval, Show, Simplify}

//todo: this should also be shared
class InstanceBox(dependency: Box[Connector], errorBox: OutputArea)
    extends Box[CoreConnector]("Concrete instance", List(dependency)){
  private var ccon: CoreConnector = _
  private var box: Block = _

  override def get: CoreConnector = ccon

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible).append("div")
      .attr("id", "instanceBox")
  }

  override def update(): Unit = try {
    if(box!=null) box.text("")
    Eval.unsafeInstantiate(dependency.get) match {
      case Some(reduc) =>
        // GOT A TYPE
        if (box!=null)
          box.append("p")
            .text(Show(reduc) + ":\n  " +
            Show(DSL.unsafeTypeOf(reduc)._1))
        //println(Graph.toString(Graph(Eval.unsafeReduce(reduc))))
        ccon = Eval.unsafeReduce(reduc)
      case _ =>
        // Failed to simplify
        errorBox.warning("Failed to reduce connector: " + Show(Simplify.unsafe(dependency.get)))
    }
  }
  catch Box.checkExceptions(errorBox,"Instance")
}

