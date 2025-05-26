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

trait AddExercises[F[_]](exercisesTable: ExercisesTable[F]) {
  def apply(
      user: User,
      exercises: List[NewExerciseRequest]
  ): F[Unit]
}

object AddExercises {
  def impl[F[_]: Sync](exercisesTable: ExercisesTable[F]): AddExercises[F] =
    new AddExercises[F](exercisesTable) {
      def apply(
          user: User,
          exercises: List[NewExerciseRequest]
      ): F[Unit] = {
        exercises.traverse_ { exerciseIn =>
          val exercise = Exercise.create(exerciseIn, user)
          exercisesTable.add(exercise)
        }
      }
    }
}
