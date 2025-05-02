package ru.fitquest.core.security;

import org.http4s.*
import ru.fitquest.core.types.AcessToken
import ru.fitquest.core.security.Cookie.Name
import ru.fitquest.core.structures.session.Tokens

object Cookie:
  enum Name:
    case AccessToken
    case RefreshToken
  object Name:
    extension (n: Name)
      def value: String = n match {
        case Name.AccessToken  => "accessToken"
        case Name.RefreshToken => "refreshToken"
      }

  def make(
      name: Name,
      value: String,
      maxAgeSeconds: Long,
      extraPath: String = "/"
  ): ResponseCookie =
    ResponseCookie(
      name = name.value,
      content = value,
      httpOnly = true,
      secure = true,
      path = Some("/api" + extraPath),
      maxAge = Some(maxAgeSeconds),
      sameSite = Some(SameSite.Strict)
    )

  def fromRequest(request: Request[?], name: Name): Option[RequestCookie] =
    request.cookies.find(_.name == name.value)

  def fromTokens(tokens: Tokens): List[ResponseCookie] = List(
    make(Name.AccessToken, tokens.acessToken.value, 3600),
    make(Name.RefreshToken, tokens.refreshToken.value, 604800)
  )
