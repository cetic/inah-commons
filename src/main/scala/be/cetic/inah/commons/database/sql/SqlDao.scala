package be.cetic.inah.commons.database.sql

object SqlDao {

  var daoFactory: Option[DaoFactory] = None

  def dao = daoFactory.get
}
