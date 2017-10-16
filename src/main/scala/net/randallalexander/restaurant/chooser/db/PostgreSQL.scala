package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object PostgreSQL {

  def initDatabase: IO[Int] = (RestaurantDDL.initDatabaseQuery *> PersonDDL.initDatabaseQuery *> initDatabaseQuery).transact(xa)

  private def initDatabaseQuery = (
    dropLikes.run *>
    dropDislikes.run *>

    createLikes.run *>
    createDislikes.run
    )

////join tables

  private val dropLikes:Update0 =
    sql"""
    DROP TABLE IF EXISTS likes CASCADE
  """.update

  private val createLikes:Update0 =
    sql"""
    CREATE TABLE likes (
      person_id VARCHAR NOT NULL REFERENCES person(id),
      restaurant_id VARCHAR NOT NULL REFERENCES restaurant(id),
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
      person_id VARCHAR NOT NULL REFERENCES person(id),
      restaurant_id VARCHAR NOT NULL REFERENCES restaurant(id),
      PRIMARY KEY(person_id, restaurant_id)
    )
  """.update

}
