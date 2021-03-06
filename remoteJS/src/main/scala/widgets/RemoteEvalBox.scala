package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.SymbolicExpr.SyExpr
import hprog.ast.Syntax
import hprog.frontend.Deviator
import hprog.frontend.solver.StaticSageSolver
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

class RemoteEvalBox(program: Box[String], errorBox: OutputArea, bounds: Box[String], default:String = "")
  extends Box[Unit]("Symbolic Evaluation", List(program)) {

  private var box : Block = _
  private var input: String = default
  private var inputAreaDom: html.TextArea = _
  private var outEval: Block = _

  private val boxID = title+"_id"


  override def get: Unit = {}

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("refresh") -> (() => update(), "Evaluate expression at a given time (Shift-enter)")
//        Left("resample") -> (() => redraw(true), "Resample: draw again the image, using the current zooming window"),
//        Left("all jumps") -> (() => redraw(false), "Resample and include all boundary nodes")
        //        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))

    toggleVisibility(visible = ()=>{callSage()})

    val evaluator = box.append("div")
      .attr("id", "evaluator box")
    outEval = box.append("div")
      .attr("id","evalOutput")

    evaluator.append("div")
      .attr("style","color: blue; display: inline; vertical-align: top; line-height: 20pt;")
      .text("Time: ")
    val inputArea = evaluator.append("textarea")
      .attr("id", boxID)
      .attr("name", boxID)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", "1")
      .attr("style", "width: 75%; ")
      //      .attr("placeholder", input)
      .text(input)

    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); update()}
      else ()
    }

  }

  override def update(): Unit = try {
    if (isVisible) callSage()
  }
  catch Box.checkExceptions(errorBox,"Evaluator1")

  private def callSage() = {
    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]
    if(inputAreaDom.value != "") {
      input = inputAreaDom.value

      outEval.text("<waiting>")
      errorBox.clear()
      errorBox.message("Waiting for SageMath...")
      RemoteBox.remoteCall("linceWS", s"E§${bounds.get}§$input§${program.get}", eval)
    }
  }

  def eval(sageReply: String): Unit = {
    //errorBox.message(s"got reply: ${sageReply}")
    errorBox.clear()
    outEval.text("")
    if (sageReply startsWith "Error")
      errorBox.error(sageReply)
    else try {
      //println(s"got reply from sage: ${sageReply}. About to parse ${dependency.get}.")
      sageReply.split("§§§",2) match {
        case Array(sol1,sol2) =>
          var res:List[String] = Nil
          var warnStr: String = ""
          //
          val warns = sol2.split("§§")
          for (kv <- warns)
            kv.split("§",2) match {
              case Array(pos,msg) => warnStr += drawWarning(pos,msg)
              case Array("") =>
              case arr => errorBox.error(s"2unexpected reply ${arr.map(x=>s"'$x'")
                .mkString(", ")} : ${arr.getClass}")
            }
          if (warnStr.nonEmpty) res = List("",warnStr)
          ///
          val sol = sol1.split("§§").map(_.split("§"))
          for (kv <- sol)
            kv match {
              case Array(k,v) => res ::= s"$k: $v"
              case _ => errorBox.error(s"1unexpected reply ${kv.map(x=>s"'$x'")
                .mkString(", ")} : ${kv.getClass}")
            }
          ///
          //      errorBox.message(res.mkString("</br>"))
          outEval.html(res.mkString("</br>"))
        case arr => errorBox.error(s"3unexpected reply ${arr.map(x=>s"'$x'")
          .mkString(", ")} : ${arr.getClass}")
      }
    }
    catch Box.checkExceptions(errorBox, "Evaluator2")
  }

  private def drawWarning(pos: String, msg: String): String = {
    s"""<div class="alert alert-warning" style="display: flex; margin-top: 10px;">
       | <div style="margin-top:auto; margin-bottom:auto;">[Warn] @$pos:</div>
       | <div style="margin-left: 10px;">$msg</div>
       | </div>""".stripMargin
  }


}
