package widgets

import common.widgets.{Box, OutputArea}

class RemoteGraphicBox(dependency: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Trajectories", List(dependency)) {
  var box : Block = _
  override def get: Unit = {}

//  private val widthCircRatio = 7
//  private val heightCircRatio = 3
//  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("glyphicon glyphicon-refresh")-> (()=>update(),"Load the program (shift-enter)")
//        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))
    box.append("div")
       .attr("id", "graphic")

//    traj = Trajectory.hprogToTraj(Map(),dependency.get)._1

    //    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
//      .onclick = {e: MouseEvent => if(!isVisible) drawGraph() else deleteDrawing()}
  }

//  override def update(): Unit = if(isVisible) {
//    deleteDrawing()
//    drawGraph()
//  }

  def draw(js: String): Unit = {
//    println("before eval")
    if (js.startsWith("Error")) errorBox.error(js)
    else scala.scalajs.js.eval(js)
//    println("after eval")
  }

  override def update(): Unit = {
    RemoteBox.remoteCall("linceWS",dependency.get,draw)
  }

}

