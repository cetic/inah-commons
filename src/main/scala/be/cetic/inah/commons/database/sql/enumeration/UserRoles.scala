package be.cetic.inah.commons.database.sql.enumeration

object UserRoles {
    val SUPERADMIN = "SUPERADMIN"
    val ADMIN = "ADMIN"
    val ADVISOR= "ADVISOR"
    val DATASOURCE = "DATASOURCE"
    val USER = "USER"
    val GUEST = "GUEST"

    object InProject{
        val SUBMITTER = "SUBMITTER"
        val PARTNER = "PARTNER"
        val SUPERVISOR = "SUPERVISOR"
    }
}
