package ru.fitquest.identity

import ru.fitquest.core.{User, UserTable}

import cats.effect.{Sync, Concurrent}
import cats.implicits._

trait Register[F[_]](userTable: UserTable[F]) {
  def apply(user: User): F[Either[String, Unit]]
}

object Register:
  def impl[F[_]: Sync](userTable: UserTable[F]): Register[F] =
    new Register[F](userTable):
      override def apply(user: User): F[Either[String, Unit]] =
        userTable.findDuplicates(user).flatMap {
          case Some(duplicateMessage) =>
            Sync[F]
              .pure(Left(duplicateMessage))
          case None =>
            userTable.add(user).map(Right(_))
        }

// // HTTP-эндпоинт
// class UserRoutes[F[_]: Sync](userService: UserService[F]) extends Http4sDsl[F] {
//   implicit val userDecoder: EntityDecoder[F, User] = jsonOf[F, User]

//   val routes: HttpRoutes[F] = HttpRoutes.of[F] {
//     case req @ POST -> Root / "register" =>
//       req.decode[User] { user =>
//         userService.register(user).flatMap {
//           case Right(_)  => Created("User registered successfully")
//           case Left(msg) => Conflict(msg)
//         }
//       }
//   }
