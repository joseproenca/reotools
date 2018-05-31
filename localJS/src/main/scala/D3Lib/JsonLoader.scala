package D3Lib

import preo.ast._
import preo.frontend.Show

import scala.util.parsing.json._

object JsonLoader {
  def apply(rawjs: String):  Either[(String, String, CoreConnector, Map[String, String], Map[String, String], String), String] = {
    val json = JSON.parseRaw(rawjs).get.asInstanceOf[JSONObject]
    val parsed = JSON.parseFull(rawjs).get.asInstanceOf[Map[String, Any]]
//    println(json.getClass)
//    json.
//    print(json.toString)
//
//    println(rawjs)
//
//    println(parsed)

    if (parsed.contains("error")) Right(parsed.get("error").asInstanceOf[String])
    else {
      println(0)
      val typ = parsed("type").asInstanceOf[String]
      println(typ)
      val reducTyp = parsed("reducType").asInstanceOf[String]
      println(reducTyp)
      val con = convertCon(parsed("connector").asInstanceOf[Map[String, Any]])
      println(Show(con))
      val graph = convertGraph(parsed("graph").asInstanceOf[Map[String, Any]])
      println(4)
      val automata = convertAut(parsed("automata").asInstanceOf[Map[String, Any]])
      println(5)
      val mcrl2 = parsed("model").asInstanceOf[String]
      println(6)
      Left((typ, reducTyp, con, graph, automata, mcrl2))
    }
  }

  private def convertCon(raw: Map[String, Any]): CoreConnector = {

    raw("type").asInstanceOf[String] match {
      case x if x == "seq" => CSeq(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case x if x == "par" => CPar(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case x if x == "id" => CId(convertInterface(raw("i").asInstanceOf[String]))
      case x if x == "symmetry" => CSymmetry(convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]))
      case x if x == "trace" => CTrace(convertInterface(raw("i").asInstanceOf[String]), convertCon(raw("c").asInstanceOf[Map[String, Any]]))
      case x if x == "prim" => CPrim(raw("name").asInstanceOf[String], convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]), None)
      case x if x == "sub" => CSubConnector(raw("name").asInstanceOf[String], convertCon(raw("c").asInstanceOf[Map[String, Any]]), Nil)
      case _ => null
    }
  }

  private def convertInterface(i: String): CoreInterface = CoreInterface(i.toInt)

  private def convertGraph(raw: Map[String, Any]): Map[String, String] = {
    println(raw)
    Map(
      "nodes" -> convertList(raw("nodes").asInstanceOf[List[Map[String, Any]]]),
      "edges" -> convertList(raw("edges").asInstanceOf[List[Map[String, Any]]])
    )
  }


  private def convertAut(raw: Map[String, Any]): Map[String, String] = Map(
    "nodesautomata" -> convertList(raw("nodesautomata").asInstanceOf[List[Map[String, Any]]]),
    "linksautomata" -> convertList(raw("linksautomata").asInstanceOf[List[Map[String, Any]]])
  )

  private def convertList(objects: List[Map[String, Any]]): String = objects.map(convertMap).mkString("[",",","]")

  private def convertMap(obj: Map[String, Any]): String = obj.toList.map{ case (a, b) => "\"" + a + "\": \"" + b.toString + "\""}.mkString("{", ",", "}")

}
