package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import pamflet.PamfletDiscounter._
import models._
import com.github.tototoshi.play2.json4s.jackson._
import scalikejdbc._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object MainController extends Controller with Json4s {

  implicit val formats = DefaultFormats

  def index = Action {
    Ok(views.html.index())
  }

  def renderMarkdown = Action { implicit request =>
    def parseMarkdown(content: String) = toXHTML(knockoff(content))

    Form("raw" -> text).bindFromRequest.fold({
      formWithErrors => BadRequest
    }, {
      raw => {
        val slides = <div>{ (for {
          slide <- raw.split("!SLIDE")
          if !slide.trim.isEmpty
        } yield {
          <div class="section">{ parseMarkdown(slide) }</div>
        }) }</div>

        Ok(slides).as("plain/text")
      }
    })
  }

  def apiListNotes = Action {
    DB.readOnly { implicit session =>
      Ok(Extraction.decompose(Note.list))
    }
  }

  def apiFindNote(id: Long) = Action {
    DB.readOnly { implicit session =>
      Ok(Extraction.decompose(Note.findById(id)))
    }
  }

  case class NewNote(title: String, raw: String)

  def apiCreateNote = Action(json) { implicit request =>
    val newNote = request.body.extract[NewNote]
    DB.localTx { implicit session =>
      val note = Note.create(newNote.title, newNote.raw)
      Ok(Extraction.decompose(note))
    }
  }

  def apiUpdateNote(id: Long) = Action (json) { implicit request =>
    val note = request.body.extract[Note]
    DB.localTx { implicit session =>
      Note.update(note)
      Ok(Extraction.decompose(note))
    }
  }

  def apiDeleteNote(id: Long) = Action {
    DB.localTx { implicit session =>
      Note.delete(id)
      Ok(Extraction.decompose(Map("id" -> id)))
    }
  }

}
