package ru.fitquest.backend

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = Server.run[IO]

//TODO безопасное храненние логинов и ключей
//TODO логирование в докере
//TODO деплой всего вместе
