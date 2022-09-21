package com.test.sber

import java.io.InputStream
import scala.collection.mutable
import scala.io.{Source, StdIn}
import scala.util.{Failure, Success, Try}

case class HNode(
                  level: Int = -1,
                  employeeFullName: String,
                  supervisorIds: Set[Int] = Set.empty,
                  subordinatesIds: Set[Int] = Set.empty
                )

class EmployeesGraph(inputStream: InputStream) {
  private val graph = mutable.HashMap[Int, HNode]()
  private var vMaxLevel = -1
  private val rowPattern = """(\d+),(\d+),(.*)""".r.unanchored

  def maxLevel = vMaxLevel

  println("Reading and preparing data from resource...")
  EmployeesGraph.using(inputStream) { data =>
    Source.fromInputStream(data)
      .getLines()
      .drop(1)
      .foreach {
        case inStr@rowPattern(employee_id, supervisor_id, full_name)   =>
          println(s"Processing $inStr -> $employee_id, $supervisor_id, $full_name" )
          val empId = employee_id.toInt
          val superId = supervisor_id.toInt

          val node = graph.getOrElseUpdate(empId, HNode(employeeFullName = full_name))
          graph.update(empId, node.copy(supervisorIds = node.supervisorIds + superId, employeeFullName = full_name))

          val sNode = graph.getOrElseUpdate(superId, HNode(employeeFullName = "Virtual node"))
          graph.update(superId, sNode.copy(subordinatesIds = sNode.subordinatesIds + empId))
        case inArg => throw new IllegalArgumentException(s"String $inArg could not be splitted")
      }
  }
  println("Updating graph with level values and finding a maximum")

  private def resolveLevel(id: Int): Int = {
    val node = graph.getOrElse(id, throw new IllegalArgumentException(s"Unable to find node with id '$id'"))

    if (node.level != -1) {
      node.level
    } else if (id == 0) {
      graph.update(id, node.copy(level = 0))
      0
    } else {
      val level = node.supervisorIds.map(resolveLevel).max + 1
      graph.update(id, node.copy(level = level))
      level
    }
  }

  graph.keysIterator.foreach { id =>
    vMaxLevel = Math.max(vMaxLevel, resolveLevel(id))
  }

  def node(id: Int): Option[HNode] = graph.get(id)

  def prettyEmployee(id: Int): String = {
    graph.get(id).map { node =>
      def fullName(id: Int) = graph.get(id).map(_.employeeFullName).getOrElse("No Name")
      s"""Employee
         | id '$id'
         | full name ${node.employeeFullName}
         | Supervisors ${node.supervisorIds.map(fullName).mkString(", ")}
         | Subordinates ${node.subordinatesIds.map(fullName).mkString(", ")}
         | """.stripMargin
    }.getOrElse(s"Unable to find node with id '$id'")
  }
}


object EmployeesGraph extends App {

  def apply(inputStream: InputStream): EmployeesGraph = new EmployeesGraph(inputStream)

  def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B = {
    try {
      f(resource)
    } finally {
      resource.close()
    }
  }

  private val inputStream = getClass.getClassLoader.getResourceAsStream("employee_multi_roots.csv")

  val eGraph = EmployeesGraph(inputStream)
  println(s"Max level is ${eGraph.maxLevel}")

  while (true) {
    print("Enter employee id or type 'exit': ")
    StdIn.readLine().trim.toLowerCase match {
      case "exit" => System.exit(0)
      case str => Try(str.toInt) match {
        case Success(value) => println(eGraph.prettyEmployee(value))
        case Failure(exception) => println(s"Unable to parse input '$str' \n ${exception.getMessage}")
      }
    }
  }
}
