package ru.fitquest

import routes._

import cats.effect.Async
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

import doobie.util.transactor
import cats.effect.kernel.Resource
import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor

object Server:
  def run[F[_]: Async]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      transactor <- postgres[F]
      userTable = core.UserTable.impl(transactor)
      authMiddleware = auth.userMiddleware[F](auth.UserAuth[F])

      helloWorldAlg = silly.HelloWorld.impl[F]
      jokeAlg = silly.Jokes.impl[F](client)
      catAlg = silly.Cat.impl[F]
      registerAlg = identity.Register.impl[F](userTable)

      publicRoutes =
        SillyRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          SillyRoutes.jokeRoutes[F](jokeAlg) <+>
          IdentityRoutes.registerRouteLogger[F](registerAlg)

      protectedRoutes =
        SillyRoutes.catRoutes[F](catAlg)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = Router(
        "/api" -> (publicRoutes <+> authMiddleware(protectedRoutes))
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever

  private def postgres[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ce <- ExecutionContexts.fixedThreadPool[F](32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:docker",
      "docker", //login
      "docker", // The password
      ce
    )
  } yield xa
