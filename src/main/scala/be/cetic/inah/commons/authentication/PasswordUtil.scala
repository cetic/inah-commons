package be.cetic.inah.commons.authentication

import de.mkammerer.argon2.Argon2Factory

trait PasswordUtil {

  def hashPassword(password: String): String = {
    val argon2 = Argon2Factory.create()
    val h = argon2.hash(10, 1000, 2, password.toCharArray)
    argon2.wipeArray(password.toCharArray)
    h
  }

  def verifyPasssword(password: String, hashedPassword: String): Boolean = {
    val argon2 = Argon2Factory.create()
    val check = argon2.verify(hashedPassword, password.toCharArray)
    argon2.wipeArray(password.toCharArray)
    check
  }
}