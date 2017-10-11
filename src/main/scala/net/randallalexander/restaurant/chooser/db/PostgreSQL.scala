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

  private def initPersonTableQuery = (dropPerson.run *> createPerson.run)

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
}
