package slide

import scala.scalajs.js

import org.scalajs.jquery._
import org.scalajs.dom._

import js.annotation.JSExport

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

trait URLUtil {

  def hashValue(hash: String): Option[String] = hash match {
    case h if h.startsWith("#") => Some(h.substring(1))
    case _ => None
  }

}

@JSExport
object ScalaJSSlide extends URLUtil {

  var currentPageNum = 1

  def render(pageNum: Int): Unit = {
    jQuery("div.section").hide()
    jQuery(s"div.section:nth-child($pageNum)").show()
    location.hash = s"#$pageNum"
    currentPageNum = pageNum
  }

  def moveToTopPage(): Unit = render(1)

  def moveToNextPage(): Unit =
    render(Seq(currentPageNum + 1, jQuery("div.section").length.toInt).min)

  def moveToPreviousPage(): Unit =
    render(Seq(currentPageNum - 1, 1).max)

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

    e.which match {
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
        currentPageNum = (for {
          AsInt(i) <- hashValue(location.hash)
        } yield i).getOrElse(1)
        jQuery("div.section").hide()
        render(currentPageNum)
        jQuery(document).bind("keydown", move _)

        jQuery("div.section").each { (index: js.Any, elem: Element) =>
          val footer = jQuery("<div>").addClass("footer")
          val prev = jQuery("<img>").addClass("slide-prev").attr("src", "./img/left.png")
          val next = jQuery("<img>").addClass("slide-next").attr("src", "./img/right.png")
          footer.append(prev).append(next).appendTo(elem)
        }

        jQuery("img.slide-prev").click { (_: JQueryEventObject) =>
          moveToPreviousPage()
        }
        jQuery("img.slide-next").click { (_: JQueryEventObject) =>
          moveToNextPage()
        }
    }

  }
}
