package ru.fitquest.core.security

import de.mkammerer.argon2.Argon2Factory
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

trait Hasher:
  def hash(s: String): String
  def verify(hash: String, password: String): Boolean

object Argon2Hasher extends Hasher:
  private val argon2 = Argon2Factory.create()

  override def hash(s: String): String =
    argon2.hash(10, 65536, 1, s.toCharArray())

  override def verify(hash: String, password: String): Boolean =
    argon2.verify(hash, password)

object Sha256Hasher extends Hasher {

  override def hash(s: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val digestBytes = md.digest(s.getBytes(StandardCharsets.UTF_8))
    digestBytes.map("%02x".format(_)).mkString
  }

  override def verify(hash: String, password: String): Boolean = {
    hash == this.hash(password)
  }
}