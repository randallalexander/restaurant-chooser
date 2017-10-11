package net.randallalexander.restaurant.chooser

import cats.effect.IO
//import doobie._
//import doobie.implicits._
import doobie.hikari.HikariTransactor


//https://tpolecat.github.io/doobie-0.2.1/13-Extensions-PostgreSQL.html
//import doobie.contrib.postgresql.pgtypes._

package object db {
  val xa: HikariTransactor[IO] = HikariTransactor[IO](
    "org.postgresql.Driver", "jdbc:postgresql:resturaunt", "postgres", "postgress%FOO"
  ).unsafeRunSync()

}