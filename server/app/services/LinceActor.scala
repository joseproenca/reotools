package services

import akka.actor._
import hprog.backend.TrajToJS
import hprog.common.ParserException
import hprog.frontend.Semantics.Valuation
import hprog.frontend.{Distance, SageSolver, Solver, Traj}

import sys.process._



object LinceActor {
  def props(out: ActorRef) = Props(new LinceActor(out))
}

class LinceActor(out: ActorRef) extends Actor{
  /**
    * Reacts to messages containing a JSON with a connector (string) and a modal formula (string),
    * produces a mcrl2 model and its LPS from the connector,
    * calls mcrl2 to verify the formula,
    * wraps each into a new JSON (via process),
    * and forwards the result to the "out" actor to generate an info (or error) box.
    */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Get a request to produce a graphic of a hprog.
    * @param msg
    * @return
    */
  private def process(msg: String): String = {
    val cleanMsg = msg.replace("\\\\", "\\")
      .replace("\\n", "\n")

    callSage(cleanMsg,"/home/jose/Applications/SageMath")

//    if (cleanMsg.startsWith("§redraw ")) {
//      cleanMsg.drop(8).split(",", 3) match {
//        case Array(v1, v2, rest) =>
//          draw(rest, Some(v1.toDouble, v2.toDouble))
//        case _ => s"Error: Unexpected message: ${msg.drop(8)}."
//      }
//    }
//    else {
//      draw(cleanMsg, None)
//    }
  }

  private def callSage(progAndEps: String, sagePath:String): String = try {
    // first part is the epsilon
    val splitted = progAndEps.split(" ",2)
    val eps = splitted(0).toDouble
    val prog = splitted(1)

    val syntax = hprog.DSL.parse(prog)
    val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
    val replySage = hprog.frontend.SageSolver.callSageSolver(eqs,sagePath,timeout=20)

    // re-evaluating trajectory to get warnings
    val solver = new hprog.frontend.SageSolverStatic(eqs,replySage)
    val traj = hprog.frontend.Semantics.syntaxToValuation(syntax,solver,new Distance(eps)).traj(Map())
    val warnings = traj.warnings(hprog.ast.BVal(true))

    replySage.mkString("§") ++
    "§§§" ++
    buildWarinings(warnings)
  }
  catch {
    case p:ParserException =>
      //println("failed parsing: "+p.toString)
      s"Error: When parsing $progAndEps - ${p.toString}"
    case e:Throwable => "Error "+e.toString
  }

  private def buildWarinings(warns: Map[Double, Set[String]]): String = {
    // "double" space "strings splitted by §"
    // all splitted by §§
    val s = warns.map(kv => s"${kv._1} ${kv._2.mkString("§")}").mkString("§§")
    //println(s"warnings: $s")
    s
  }

//  private def draw(msg: String, range: Option[(Double,Double)]): String =
//    try {
//        //println(s"building trajectory with range $range from $msg")
//        val syntax = hprog.DSL.parse(msg)
//        //      println("a")
//        //      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
//        val prog = hprog.frontend.Semantics.syntaxToValuation(syntax)
//
//        /////
//        // tests: to feed to Sage
//        //      var sages = List[String]()
//        //      val systems = Solver.getDiffEqs(syntax)
//        //      val solver = new SageSolver("/home/jose/Applications/SageMath")
//        //      solver.batch(systems)
//        //      for ((eqs,repl) <- solver.cached) {
//        //        sages ::= s"## Solved(${eqs.map(Show(_)).mkString(", ")})"
//        ////        sages ::= repl.mkString(",")
//        //      }
//
//        val traj = prog.traj(Map())
//
//        TrajToJS(traj,range)
//    }
//    catch {
//      case p:ParserException =>
//        //println("failed parsing: "+p.toString)
//        "Error: "+p.toString
//      case e:Throwable => "Error "+e.toString
//    }


}
