package be.cetic.inah.commons.database.sql

import slick.jdbc.JdbcProfile

trait DriverComponent {
  implicit val driver: JdbcProfile
}
