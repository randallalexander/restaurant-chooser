package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object PostgreSQL {

  def initDatabase: IO[Int] = (RestaurantDDL.initDatabaseQuery *> initDatabaseQuery).transact(xa)

  private def initDatabaseQuery = (
    dropLikes.run *>
    dropDislikes.run *>
    dropPerson.run *>

    createPerson.run *>
    createLikes.run *>
    createDislikes.run
    )

///regular tables

  private val dropPerson:Update0 =
    sql"""
    DROP TABLE IF EXISTS person CASCADE
  """.update

  private val createPerson:Update0 =
    sql"""
    CREATE TABLE person (
      id SERIAL PRIMARY KEY,
      nickname varchar NOT NULL unique,
      fname varchar NOT NULL,
      lname varchar NOT NULL
    )
  """.update

////join tables

  private val dropLikes:Update0 =
    sql"""
    DROP TABLE IF EXISTS likes CASCADE
  """.update

  private val createLikes:Update0 =
    sql"""
    CREATE TABLE likes (
      person_id INTEGER NOT NULL REFERENCES person(id),
      restaurant_id INTEGER NOT NULL REFERENCES restaurant(id),
      PRIMARY KEY(person_id, restaurant_id)
    )
  """.update

  private val dropDislikes:Update0 =
    sql"""
    DROP TABLE IF EXISTS dislikes CASCADE
  """.update

  private val createDislikes:Update0 =
    sql"""
    CREATE TABLE dislikes (
      person_id INTEGER NOT NULL REFERENCES person(id),
      restaurant_id INTEGER NOT NULL REFERENCES restaurant(id),
      PRIMARY KEY(person_id, restaurant_id)
    )
  """.update

}
