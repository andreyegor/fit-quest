package ru.fitquest.backend.core.security

import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import java.time.{Instant, Clock}
import java.security.SecureRandom
import java.util.{Base64, UUID}
import scala.util.{Try, Success, Failure}

import ru.fitquest.backend.core.structures.session.*
import ru.fitquest.backend.core.structures.user.*

object GenerateTokens:
  val secretKey = "самый секретный ключ"
  val algo = JwtAlgorithm.HS256

  def generateAccessToken(user: User): AccessToken =
    generateAccessToken(user.userId)

  def generateAccessToken(userId: UserId): AccessToken =
    val claim = JwtClaim(
      content = s"""{"userId":"$userId"}""",
      issuedAt = Some(Instant.now.getEpochSecond),
      expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond) // 1 час
    )
    AccessToken(Jwt.encode(claim, secretKey, algo))

  def decodeAccessToken(accessToken: AccessToken): Option[UserId] =
    for {
      claim <- Jwt.decode(accessToken.value, secretKey, Seq(algo)).toOption
      if claim.isValid(Clock.systemUTC())
      decoded <- decode[Map[String, UUID]](claim.content).toOption
      userId <- decoded.get("userId")
    } yield UserId(userId)

  def generateRefreshToken: RefreshToken =
    val random = new SecureRandom()
    val bytes = new Array[Byte](64)
    random.nextBytes(bytes)
    RefreshToken(Base64.getUrlEncoder.withoutPadding().encodeToString(bytes))
