package ru.fitquest.core.security

import ru.fitquest.core.types.*

import de.mkammerer.argon2.Argon2Factory

object Argon2Hasher:
  private val argon2 = Argon2Factory.create()

  def hash(s: String): String =
    argon2.hash(10, 65536, 1, s.toCharArray())

  def verify(hash: String, password: String): Boolean =
    argon2.verify(hash, password)