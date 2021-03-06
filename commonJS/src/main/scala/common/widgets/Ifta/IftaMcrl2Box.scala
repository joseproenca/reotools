package common.widgets.Ifta

import java.util.Base64

import common.widgets.{Box, OutputArea}
import ifta.analyse.mcrl2.IftaModel
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.{Model, ReoModel}

/**
  * Created by guillerminacledou on 2019-05-14
  */


class IftaMcrl2Box(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends Box[IftaModel]("mCRL2 of the IFTA instance", List(dependency))  {
  private var box: Block = _
  private var model: IftaModel = _

  override def get: IftaModel = model

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List(Right("download")-> (()=>download(), "Download mCRL2 specification")))
      .append("div")
      .attr("id", "mcrl2IftaBox")
      .style("white-space","pre-wrap")


    dom.document.getElementById("mCRL2 of the IFTA instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { _: MouseEvent => if (!isVisible) produceMcrl2() else deleteMcrl2()}
  }

  private def download(): Unit = {
    //    <a href="data:application/octet-stream;charset=utf-16le;base64,//5mAG8AbwAgAGIAYQByAAoA">text file</a>
    val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
    val filename = "model.mcrl2"
    val url= "data:application/octet-stream;charset=utf-16le;base64,"+enc
    //
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = e => {
      if(x.status == 200){
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
            a.download="$filename";
            a.text = "hidden link";
            //programatically click the link to trigger the download
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL("$url");
          """
        )
      }
      else if(x.status == 404){
        errorBox.error(x.responseText)
      }
    }
    x.send()
  }
  override def update(): Unit = if(isVisible) produceMcrl2()

  private def produceMcrl2(): Unit = try {
    deleteMcrl2()
    model = Model[IftaModel](dependency.get)
    box.html(model.toString)
  }
  catch Box.checkExceptions(errorBox,"mCRL2-code")

  private def deleteMcrl2(): Unit = {
    box.html("")
  }

}

