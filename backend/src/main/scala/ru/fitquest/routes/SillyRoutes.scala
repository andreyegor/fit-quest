package ru.fitquest.routes

import cats.effect.Sync
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.AuthedRoutes
import ru.fitquest.silly
import ru.fitquest.auth//TODO

object SillyRoutes:
  def jokeRoutes[F[_] : Sync](J: silly.Jokes[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }

  def helloWorldRoutes[F[_] : Sync](H: silly.HelloWorld[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(silly.HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }

  def catRoutes[F[_] : Sync](C: silly.Cat[F]): AuthedRoutes[auth.User, F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    AuthedRoutes.of {
      case GET -> Root / "cat" as auth =>
        for {
          cat <- C.cat
          resp <- Ok(cat)
        } yield resp
    }