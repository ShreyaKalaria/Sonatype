package samples

import org.scalatest.junit.JUnitSuite
import scala.collection.mutable.ListBuffer
import _root_.org.junit.Test
import _root_.org.junit.Before

class ScalaTestJUnitSuite extends JUnitSuite {

  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _

  @Before def initialize() {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]
  }

  @Test def verifyEasy() {
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
  }

  @Test def verifyFun() {
    sb.append("fun!")
    assert(sb.toString === "ScalaTest is fun!")
    assert(lb.isEmpty)
  }
}