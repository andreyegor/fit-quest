package ru.fitquest.backend

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.data.Kleisli

import com.comcast.ip4s.*

import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import org.http4s.{HttpApp, HttpRoutes, Request, Response, Status}
import org.http4s.implicits.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, middleware as Http4sMiddleware}

import org.typelevel.log4cats.slf4j.Slf4jLogger

import ru.fitquest.backend.routes.*
import ru.fitquest.backend.users
import ru.fitquest.backend.mobile.routes.MobileAuthRoutes
import org.http4s.CacheDirective.public
import ru.fitquest.backend.mobile.routes.MobileExercisesRoutes
import ru.fitquest.backend.exercises.AddExercises
import ru.fitquest.backend.core.database.ExercisesTable

object Server:
  def run[F[_]: Async]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build

      transactor <- postgres[F]
      userTable = core.database.UserTable.impl(transactor)
      sessionsTable = core.database.SessionsTable.impl(transactor)
      exercisesTable = core.database.ExercisesTable.impl(transactor)

      authenticate = auth.Authenticate.impl(userTable)
      authMiddleware = auth.Middleware(authenticate)

      registerAlg = users.Register.impl[F](userTable)

      loginAlg = auth.Login.impl[F](authenticate, sessionsTable)
      refreshAlg = auth.Refresh.impl[F](sessionsTable)

      helloWorldAlg = silly.HelloWorld.impl[F]
      catAlg = silly.Cat.impl[F]

      addExercisesAlg = exercises.AddExercises.impl[F](exercisesTable)
      getExercisesAlg = exercises.GetExercises.impl[F](exercisesTable)

      webPublicRoutes =
        SillyRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          UsersRoutes[F](registerAlg) <+>
          AuthRoutes[F](loginAlg, refreshAlg)
      mobilePublicRoutes = MobileAuthRoutes[F](loginAlg, refreshAlg)

      webProtectedRoutes =
        SillyRoutes.catRoutes[F](catAlg) <+>
          ExercisesRoutes.GetExercises[F](getExercisesAlg)
      mobileProtectedRoutes = MobileExercisesRoutes.AddExercises[F](
        addExercisesAlg
      )

      publicRoutes = webPublicRoutes <+> mobilePublicRoutes
      protectedRoutes = webProtectedRoutes <+> mobileProtectedRoutes

      routes = publicRoutes <+> authMiddleware(protectedRoutes)

      httpApp = Router("/api" -> routes).orNotFound

      appWithErrorHandling = withErrorLogging(httpApp)

      finalApp = Http4sMiddleware.Logger.httpApp(
        logHeaders = true,
        logBody = true
      )(
        appWithErrorHandling
      )

      _ <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalApp)
        .build
    } yield ()
  }.useForever

  private def withErrorLogging[F[_]: Async](httpApp: HttpApp[F]): HttpApp[F] = {
    val logger = Slf4jLogger.getLogger[F]
    Kleisli { req =>
      httpApp(req).handleErrorWith { ex =>
        logger.error(ex)(
          s"Unhandled error while processing request: ${req.method} ${req.uri}"
        ) *>
          Response[F](Status.InternalServerError).pure[F]
      }
    }
  }

  private def postgres[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    val dbUrl = sys.env.getOrElse("DB_URL", "jdbc:postgresql:docker")
    val dbUser = sys.env.getOrElse("DB_USER", "docker")
    val dbPassword = sys.env.getOrElse("DB_PASSWORD", "docker")
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        dbUrl,
        dbUser,
        dbPassword,
        ce
      )
    } yield xa
