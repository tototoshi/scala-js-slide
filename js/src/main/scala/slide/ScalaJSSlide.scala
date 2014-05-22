package slide

import scala.scalajs.js

import org.scalajs.jquery._
import org.scalajs.dom._

import js.annotation.JSExport

import scala.scalajs.js.Dynamic.{ global => g }


object AsInt {
  def unapply(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }
}

object KeyCode {
  val LEFT = 37
  val UP = 38
  val RIGHT = 39
  val DOWN = 40
  val ESC = 27
}

@JSExport
object ScalaJSSlide {

  var currentPageNum = 1

  def render(pageNum: Int): Unit = {
    jQuery("#slide div.section").hide()
    jQuery(s"#slide div.section:nth-child($pageNum)").show()
  }

  def moveToTopPage(): Unit = render(1)

  def moveToNextPage(): Unit = {
    val nextPage = Seq(currentPageNum + 1, jQuery("#slide div.section").length.toInt).min
    render(nextPage)
    currentPageNum = nextPage
    g.console.log(currentPageNum)
  }

  def moveToPreviousPage(): Unit = {
    val prevPage = Seq(currentPageNum - 1, 1).max
    render(prevPage)
    currentPageNum = prevPage
    g.console.log(currentPageNum)
  }

  def move(e: JQueryEventObject): Unit = {

    sealed abstract class Direction
    case object Next extends Direction
    case object Prev extends Direction

    val directions: Map[Int, Direction] = Map(
      KeyCode.LEFT -> Prev,
      KeyCode.UP -> Prev,
      KeyCode.RIGHT -> Next,
      KeyCode.DOWN -> Next
    )

    val keyCodeAsInt = js.Number.toDouble(e.which).toInt
    keyCodeAsInt match {
      case KeyCode.ESC =>
        moveToTopPage()
      case k => directions.get(k).foreach {
        case Next => moveToNextPage()
        case Prev => moveToPreviousPage()
      }
    }
  }

  @JSExport
  def main(): Unit = {

    jQuery(document).ready {
      () =>
        jQuery("#slide-modal").on("shown.bs.modal", (ev: JQueryEventObject) => {
          currentPageNum = 1
          jQuery("#slide div.section").hide()
          render(currentPageNum)
          jQuery(document).bind("keydown", move _)
          jQuery("#main").hide()
        })

        jQuery("#slide-modal").on("hidden.bs.modal", (ev: JQueryEventObject) => {
          jQuery("#main").show()
        })
    }

  }
}
