package ru.fitquest.core.types

import io.circe.Decoder

opaque type Email = String
object Email:
  def apply(raw: String): Either[String, Email] =
    if raw.matches("^[^@]+@[^@]+\\.[^@]+$") then Right(raw)
    else Left(s"Invalid email: $raw")
  extension (e: Email) def value: String = e
  given Conversion[Email, String] = _.value
  given Decoder[Email] = Decoder[String].emap(Email(_))

opaque type Name = String
object Name:
  def apply(raw: String): Either[String, Name] =
    if raw.length() < 2 then Left("Name is too short")
    else if raw.length() > 20 then Left("Name is too log")
    else Right(raw)
  extension (n: Name) def value: String = n
  given Conversion[Name, String] = _.value
  given Decoder[Name] = Decoder[String].emap(Name(_))

opaque type Password = String
object Password:
  def apply(raw: String): Either[String, Password] =
    if raw.length >= 8 then Right(raw) else Left("Password too short")
  extension (p: Password) def value: String = p
  given Conversion[Password, String] = _.value
  given Decoder[Password] = Decoder[String].emap(Password(_))

opaque type Passhash = String
object Passhash:
  def apply(hash: String): Passhash = hash
  extension (p: Passhash) def value: String = p
  given Conversion[Passhash, String] = _.value

opaque type GoogleId = String
object GoogleId:
  def apply(id: String): Either[String, GoogleId] = Right(id) // TODO проверка
  extension (g: GoogleId) def value: String = g
  given Conversion[GoogleId, String] = _.value
  given Decoder[GoogleId] = Decoder[String].emap(GoogleId(_))
