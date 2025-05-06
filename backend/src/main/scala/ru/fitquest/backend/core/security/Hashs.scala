package ru.fitquest.backend.core.security

import de.mkammerer.argon2.Argon2Factory
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

import ru.fitquest.backend.core.structures.session.{RefreshToken, RefreshTokenHash}
import ru.fitquest.backend.core.structures.user.{Password, PasswordHash}

trait Hasher[U,H]:
  def hash(unhashed: U): H
  def verify(hashed: H, unhashed: U): Boolean

object RefreshTokenHasher extends Hasher[RefreshToken, RefreshTokenHash]:
  val hasher:Hasher[String, String] = Sha256Hasher

  override def hash(u: RefreshToken): RefreshTokenHash = RefreshTokenHash(hasher.hash(u.value))
  override def verify(h:RefreshTokenHash, u:RefreshToken) = hasher.verify(h.value, u.value)

object PasswordHasher extends Hasher[Password, PasswordHash]:
  val hasher:Hasher[String, String] = Sha256Hasher

  override def hash(u: Password): PasswordHash = PasswordHash(hasher.hash(u.value))
  override def verify(h:PasswordHash, u:Password) = hasher.verify(h.value, u.value)

private object Argon2Hasher extends Hasher[String, String]:
  private val argon2 = Argon2Factory.create()

  override def hash(s: String): String =
    argon2.hash(10, 65536, 1, s.toCharArray())

  override def verify(hash: String, password: String): Boolean =
    argon2.verify(hash, password)

object Sha256Hasher extends Hasher[String, String] {

  override def hash(s: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val digestBytes = md.digest(s.getBytes(StandardCharsets.UTF_8))
    digestBytes.map("%02x".format(_)).mkString
  }

  override def verify(hash: String, password: String): Boolean = {
    hash == this.hash(password)
  }
}