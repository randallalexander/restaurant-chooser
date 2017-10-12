package net.randallalexander.restaurant.chooser.db

//import cats._
//import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object PostgreSQL {

  def initDatabase: IO[Int] =
    initDatabaseQuery.transact(xa)

  private def initDatabaseQuery = (
    dropLikes.run *>
    dropDislikes.run *>

    dropPerson.run *>
    dropRestaurant.run *>

    dropEthnicType.run *>
    dropFoodType.run *>

    createEthnicType.run *>
    createFoodType.run *>

    createPerson.run *>
    createRestaurant.run *>

    createLikes.run *>
    createDislikes.run
    )

////types

  private val dropEthnicType:Update0 =
    sql"""
    DROP TYPE IF EXISTS ethnicType
  """.update

  private val createEthnicType:Update0 =
    sql"""
    CREATE TYPE ethnicType AS ENUM ('mexican', 'american', 'italian', 'chinese');
  """.update

  private val dropFoodType:Update0 =
    sql"""
    DROP TYPE IF EXISTS foodType
  """.update

  private val createFoodType:Update0 =
    sql"""
    CREATE TYPE foodType AS ENUM ('sandwich', 'burrito');
  """.update

///regular tables

  private val dropPerson:Update0 =
    sql"""
    DROP TABLE IF EXISTS person
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


  private val dropRestaurant:Update0 =
    sql"""
    DROP TABLE IF EXISTS restaurant
  """.update

  /*
    lat/long at millimeter precision
    could down grade to 0.1m by using only
    NUMERIC(9, 6) which is probably more
    appropriate but hey, lets live on the edge
   */
  private val createRestaurant:Update0 =
    sql"""
    CREATE TABLE restaurant (
      id SERIAL PRIMARY KEY,
      name varchar NOT NULL,
      addressLine1 varchar NOT NULL,
      city varchar NOT NULL,
      state varchar(2) NOT NULL,
      zip NUMERIC(5, 0) NOT NULL,
      ethnic_type ethnicType,
      food_type foodType,
      price_per_person NUMERIC (5,2),
      cord_lat NUMERIC(11, 8),
      cord_long NUMERIC(11, 8)
    )
  """.update

////join tables

  private val dropLikes:Update0 =
    sql"""
    DROP TABLE IF EXISTS likes
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
    DROP TABLE IF EXISTS dislikes
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
