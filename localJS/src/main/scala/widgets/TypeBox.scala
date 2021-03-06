package widgets

import common.widgets.{Box, OutputArea}
import preo.DSL
import preo.ast.{BVal, Connector}
import preo.common.TypeCheckException
import preo.frontend.Show


//todo: this should be local to localJS
class TypeBox(dependency: Box[String], errorBox: OutputArea)
    extends Box[Connector]("Type", List(dependency)){

  var con: Connector = _
  var typeInfo: Block = _


  override def get: Connector = con

  override def init(div: Block, visible: Boolean): Unit = {
    typeInfo = panelBox(div, visible).append("div")
      .attr("id", "typeBox")
  }

  override def update(): Unit = {

    if (typeInfo!=null) typeInfo.text("")
    try DSL.parseWithError(dependency.get) match {
      case Right(result) =>
        try {
          val typ = DSL.unsafeCheckVerbose(result)
          val (_, rest) = DSL.unsafeTypeOf(result)
          if (typeInfo!=null) typeInfo.append("p")
                                      .text(Show(typ))
          if (rest != BVal(true))
            errorBox.warning(s"Warning: did not check if ${Show(rest)}.")
          con = result
        }
        catch Box.checkExceptions(errorBox)
//      case f@preo.lang.Parser.Failure(msg,_) =>
//        errorBox.error(f.toString())
//      //        instanceInfo.append("p").text("-")
//      case preo.lang.Parser.Error(msg,_) =>
      case Left(msg) =>
        errorBox.error("Parser error: " + msg)
      //        instanceInfo.append("p").text("-")
    }
    catch Box.checkExceptions(errorBox)
  }
}
