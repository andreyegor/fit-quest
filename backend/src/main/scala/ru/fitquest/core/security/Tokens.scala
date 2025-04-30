package ru.fitquest.core.security

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import java.time.Instant
import java.security.SecureRandom
import java.util.Base64

import ru.fitquest.core.structures.user.User
import ru.fitquest.core.types.*

val secretKey = "самый секретный ключ"

def generateAccessToken(user: User): AcessToken = {
  val userId = user.userId
  val claim = JwtClaim(
    content = s"""{"userId":"$userId"}""",
    issuedAt = Some(Instant.now.getEpochSecond),
    expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond) // 1 час
  )
  AcessToken(Jwt.encode(claim, secretKey, JwtAlgorithm.HS256))
}

def generateRefreshToken: RefreshToken = {
  val random = new SecureRandom()
  val bytes = new Array[Byte](64)
  random.nextBytes(bytes)
  RefreshToken(Base64.getUrlEncoder.withoutPadding().encodeToString(bytes))
}
