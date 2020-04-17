package be.cetic.inah.commons.database.sql

import be.cetic.inah.commons.database.sql.access_control.SqlAccessControlJsonProtocol
import be.cetic.inah.commons.database.sql.management.SqlManagementJsonProtocol
import be.cetic.inah.commons.database.sql.population.SqlPopulationJsonProtocol

trait SqlJsonProtocol extends SqlManagementJsonProtocol with SqlAccessControlJsonProtocol with SqlPopulationJsonProtocol
