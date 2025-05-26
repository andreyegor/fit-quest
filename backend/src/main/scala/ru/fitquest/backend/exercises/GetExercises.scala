package ru.fitquest.backend.exercises

import cats.data.EitherT

import cats.effect.kernel.Sync
import cats.syntax.all._

import ru.fitquest.backend.core.database.ExercisesTable
import ru.fitquest.backend.core.structures.user.User
import ru.fitquest.backend.core.structures.exercise.Exercise
import ru.fitquest.backend.core.structures.exercise.NewExerciseRequest
import ru.fitquest.backend.core.structures.exercise.Exercise
import ru.fitquest.backend.core.database.ExercisesTable
import ru.fitquest.backend.core.structures.exercise.ExerciseType

trait GetExercises[F[_]](exercisesTable: ExercisesTable[F]) {
  def apply(
      user: User,
      exerciseTypes: Option[List[ExerciseType]] = None,
      from: Option[Int] = None,
      to: Option[Int] = None
  ): F[List[Exercise]]
}

object GetExercises {
  def impl[F[_]: Sync](exercisesTable: ExercisesTable[F]): GetExercises[F] =
    new GetExercises[F](exercisesTable) {
      def apply(
          user: User,
          exerciseTypes: Option[List[ExerciseType]] = None,
          from: Option[Int] = None,
          to: Option[Int] = None
      ): F[List[Exercise]] =
        exercisesTable.get(user.userId, exerciseTypes, from, to)
    }
}
