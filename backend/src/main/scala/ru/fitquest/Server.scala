package ru.fitquest

import cats.data.Kleisli
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import ru.fitquest.routes.{AuthRoutes, SillyRoutes}

object Server:
  def run[F[_]: Async]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build

      transactor <- postgres[F]
      userTable = core.database.UserTable.impl(transactor)
      sessionTable = core.database.SessionTable.impl(transactor)
      authMiddleware = auth.userMiddleware[F](auth.UserAuth[F])

      helloWorldAlg = silly.HelloWorld.impl[F]
      catAlg = silly.Cat.impl[F]
      registerAlg = auth.Register.impl[F](userTable)
      loginAlg = auth.Login.impl[F](userTable, sessionTable)

      publicRoutes =
        SillyRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          AuthRoutes.registerRoute[F](registerAlg) <+>
          AuthRoutes.loginRoute[F](loginAlg)

      protectedRoutes =
        SillyRoutes.catRoutes[F](catAlg)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = Router(
        "/api" -> (publicRoutes <+> authMiddleware(protectedRoutes))
      ).orNotFound

      safeHttpApp = withErrorLogging(httpApp)

      loggerHttpApp = Logger.httpApp(true, true)(safeHttpApp)

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(loggerHttpApp)
          .build
    } yield ()
  }.useForever

  private def postgres[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ce <- ExecutionContexts.fixedThreadPool[F](32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:docker",
      "docker", // login
      "docker", // The password
      ce
    )
  } yield xa

  private def withErrorLogging[F[_]: Async](httpApp: HttpApp[F]): HttpApp[F] = {
    val logger =
      Slf4jLogger.getLogger[F] // <- теперь сразу создаём логгер, а не в `F`
    Kleisli { req =>
      httpApp(req).handleErrorWith { ex =>
        logger.error(ex)(
          s"Unhandled error while processing request: ${req.method} ${req.uri}"
        ) *>
          Response[F](Status.InternalServerError).pure[F]
      }
    }
  }
