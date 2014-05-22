package models

import scalikejdbc._

case class Note(id: Long, title: String, raw: String)

object Note {

  def *(rs: WrappedResultSet): Note = Note(rs.long("id"), rs.string("title"), rs.string("raw"))

  def findById(id: Long)(implicit session: DBSession = AutoSession): Option[Note] = {
    sql"select * from note where id = $id".map(*).single.apply()
  }

  def list()(implicit session: DBSession = AutoSession): List[Note] = {
    sql"select * from note".map(*).list.apply()
  }

  def create(title: String, raw: String)(implicit session: DBSession = AutoSession): Note = {
    val id = sql"insert into note (title, raw) values ($title, $raw)".updateAndReturnGeneratedKey.apply()
    Note(id, title, raw)
  }

  def update(note: Note)(implicit session: DBSession = AutoSession): Unit = {
    val id = note.id
    val title = note.title
    val raw = note.raw
    sql"update note set title = $title, raw = $raw where id = $id".update.apply()

  }

  def delete(id: Long)(implicit session: DBSession = AutoSession): Unit  = {
    sql"delete from note where id = $id".update.apply()
  }

}
