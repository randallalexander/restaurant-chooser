package net.randallalexander.restaurant.chooser.db

//import cats._
//import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object PostgreSQL {

  def initPersonTable: IO[Int] =
    initPersonTableQuery.transact(xa)

  private def initPersonTableQuery = (dropPerson.run *> dropRestaurant.run *> createPerson.run *> createRestaurant.run)

  private val dropPerson:Update0 =
    sql"""
    DROP TABLE IF EXISTS person
  """.update

  private val createPerson:Update0 =
    sql"""
    CREATE TABLE person (
      id SERIAL,
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
      id SERIAL,
      name varchar NOT NULL,
      addressLine1 varchar NOT NULL,
      city varchar NOT NULL,
      state varchar(2) NOT NULL,
      zip NUMERIC(5, 0) NOT NULL,
      cord_lat NUMERIC(11, 8) NOT NULL,
      cord_long NUMERIC(11, 8) NOT NULL
    )
  """.update
}
