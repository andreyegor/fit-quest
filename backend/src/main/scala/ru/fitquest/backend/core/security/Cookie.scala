package ru.fitquest.backend.core.security;

import org.http4s.*
import ru.fitquest.backend.core.security.Cookie.Name
import ru.fitquest.backend.core.structures.session
import cats.effect.kernel.Ref

object Cookie:
  enum Name(val value: String): // Todo add types
    case AccessToken extends Name("accessToken")
    case RefreshToken extends Name("refreshToken")

  def make(
      name: Name,
      value: String,
      maxAgeSeconds: Long,
      httpOnly: Boolean = true,
      secure: Boolean = true,
      extraPath: String = ""
  ): ResponseCookie =
    ResponseCookie(
      name = name.value,
      content = value,
      httpOnly = httpOnly,
      secure = secure,
      path = Some("/api/" + extraPath),
      maxAge = Some(maxAgeSeconds),
      sameSite = Some(SameSite.Strict)
    )

  def fromRequest(request: Request[?], name: Name): Option[String] =
    request.cookies.find(_.name == name.value).map(_.content)

  def fromAcessToken(token: session.AccessToken): ResponseCookie =
    make(Name.AccessToken, token.value, 3600)

  def fromRefreshToken(token: session.RefreshToken): ResponseCookie =
    make(
      Name.RefreshToken,
      token.value,
      7 * 24 * 60 * 60,
      extraPath = "auth/"
    )

  def emptyAcessToken: ResponseCookie =
    make(Name.AccessToken, "", 0)

  def emptyRefreshToken: ResponseCookie =
    make(Name.RefreshToken, "", 0, extraPath = "auth/")
