package reolive

import common.widgets._
import org.scalajs.dom
import dom.html
import org.singlespaced.d3js.d3
import preo.backend._
import preo.frontend.mcrl2.Model
import preo.ast.CoreConnector
import reolive.RemoteReo.outputBox
import widgets.{OutputArea, RemoteInstanceBox, RemoteLogicBox, RemoteModelBox}

import scalajs.js.annotation.JSExportTopLevel



/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo extends{


  private var inputBox: Box[String] = _
  private var typeInstanceInfo: RemoteInstanceBox = _
  private var errors: ErrorArea = _

  private var modalBox: Box[String] = _
  private var outputBox: OutputArea = _

  private var svg: Box[Graph] = _
  private var svgAut: Box[Automata] = _
  private var mcrl2Box: RemoteModelBox = _

  @JSExportTopLevel("reolive.RemoteReo.main")
  def main(content: html.Div): Unit = {

    //    // add header
    //    d3.select(content).append("div")
    //      .attr("id", "header")
    //      .append("h1").text("Reo Live - Connector Families")

    val contentDiv = d3.select(content).append("div")
      .attr("id", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
      .attr("id", "mytable")

    val leftside = rowDiv.append("div")
//      .attr("class", "col-sm-4")
      .attr("id", "leftbar_wr")
      .attr("class", "leftside")
    leftside.append("div")
      .attr("id","dragbar_wr")
      .attr("class", "middlebar")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar_wr")
      .attr("class", "rightside")

    // Create boxes (order matters)
    inputBox =
      new InputCodeBox(first_reload(), default="dupl  ;  fifo * lossy", id="wr",rows=4)
    errors =
      new ErrorArea
    typeInstanceInfo =
      new RemoteInstanceBox(second_reload(),inputBox, errors)
    val buttonsDiv =
      new ButtonsBox(first_reload(), inputBox.asInstanceOf[InputCodeBox])
    svg =
      new GraphBox(typeInstanceInfo, errors)
    svgAut =
      new AutomataBox(typeInstanceInfo, errors)
    mcrl2Box =
      new RemoteModelBox(typeInstanceInfo, errors)
    outputBox = new OutputArea()
    // must be after inputbox and mcrl2box
    modalBox = new RemoteLogicBox(inputBox, typeInstanceInfo, outputBox)

    inputBox.init(leftside,true)
    errors.init(leftside)
    typeInstanceInfo.init(leftside,true)
    buttonsDiv.init(leftside,false)
    modalBox.init(leftside,true)
    outputBox.init(leftside)
    svg.init(rightside,true)
    svgAut.init(rightside,false)
    mcrl2Box.init(rightside,false)

    first_reload()
  }

  /**
    * Called by InputBox.
    * Parse the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {
    errors.clear
    inputBox.update
    typeInstanceInfo.update
  }

  /**
    * Called by TypeInstance upon producing a new value.
    * Retrieve the instance from the TypeInstance widget and
    * triggers the circuit, svg, and mCRL2 generation.
    */
  private def second_reload(): Unit = {
    mcrl2Box.id = typeInstanceInfo.id
    svg.update
    svgAut.update
    mcrl2Box.update
  }

}

