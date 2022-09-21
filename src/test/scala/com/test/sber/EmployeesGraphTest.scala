package com.test.sber

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class EmployeesGraphTest extends AnyFlatSpec with Matchers {

  behavior of "EmployeesGraph"

  val inputStream = getClass.getClassLoader.getResourceAsStream("test_employee.csv")
  val eGraph = EmployeesGraph(inputStream)

  it should "return max level value" in {
    eGraph.maxLevel shouldBe 4
  }

  it should "return proper response for employee id 4" in {
    val expectedOutput =
      """Employee
        | id '4'
        | full name Yuri Benediktov
        | Supervisors Konstantin Kish
        | Subordinates Illarion Maskov
        |""".stripMargin

    eGraph.prettyEmployee(4).replaceAll("\\s", "") shouldBe expectedOutput.replaceAll("\\s", "")
  }

  it should "return proper response for employee id 2" in {
    val expectedOutput =
      """Employee
        | id '2'
        | full name Konstantin Kish
        | Supervisors Vitaly Glavnov
        | Subordinates Yuri Benediktov
        |""".stripMargin

    eGraph.prettyEmployee(2).replaceAll("\\s", "") shouldBe expectedOutput.replaceAll("\\s", "")
  }

}
