package ru.fitquest.core.security

import ru.fitquest.core.types.*

import de.mkammerer.argon2.Argon2Factory

object PasswordHasher:
  private val argon2 = Argon2Factory.create()

  def hash(password: Password): Passhash =
    Passhash(argon2.hash(10, 65536, 1, password.toCharArray()))

  def verify(hash: Passhash, password: Password): Boolean =
    argon2.verify(hash, password)