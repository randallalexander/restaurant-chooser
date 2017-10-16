package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import fs2.Stream
import net.randallalexander.restaurant.chooser.errors.Errors.ConstraintViolation
import net.randallalexander.restaurant.chooser.model._
import shapeless.record._

object PersonDAO {
  def createPerson(person: Person): IO[Either[ConstraintViolation,Person]] = {
    createPersonQuery(person).transact(xa).map {
      _.map { newId =>
        person.copy(id = Some(newId))
      }
    }
  }

  private def createPersonQuery(person: Person): ConnectionIO[Either[ConstraintViolation, Int]] = {
    sql"""
      insert into person (fname, lname, nickname)
        values (
          ${person.fname.trim},
          ${person.lname.trim},
          ${person.nickname.trim})""".update.withUniqueGeneratedKeys[Int]("id").attemptSomeSqlState {
        case UNIQUE_VIOLATION => ConstraintViolation("Duplicate nickname")
      }
  }

  val selectAll = fr"""select id, nickname, fname, lname"""
  val fromPerson = fr"""from person"""
  private def streamToList (stream:Stream[ConnectionIO,Person]):IO[List[Person]] = {
    stream
      .list
      .transact(xa)
  }

  def getPerson(id:Int): IO[Option[Person]] = {
    getPersonQuery(id).transact(xa)
  }

  private def getPersonQuery(restId:Int):ConnectionIO[Option[Person]] = {
    (selectAll ++ fromPerson ++
      fr"""
         where id = $restId
       """).query[Person].option
  }


  def listPeople(offset:Int, limit:Int): IO[List[Person]] = {
    streamToList(listPersonQuery(offset,limit))
  }

  private def listPersonQuery(offset:Int, limit:Int):Stream[ConnectionIO,Person] = {
    (selectAll ++ fromPerson ++
      fr"""
        limit $limit offset $offset
       """).query[Person].process
  }


  def getPersonByName(name:String): IO[List[Person]] = {
    streamToList(getPersonByNameQuery(name))
  }

  private def getPersonByNameQuery(name:String):Stream[ConnectionIO,Person] = {
    val predicateValue = s"%$name%"
    (selectAll ++ fromPerson ++
      fr"""
         where nickname ILIKE $predicateValue
       """).query[Person].process
  }


  def deletePerson(id:Int): IO[Int] = {
    deletePersonQuery(id).transact(xa)
  }

  private def deletePersonQuery(personId:Int): ConnectionIO[Int] = {
    sql"""
      DELETE FROM person
      WHERE id = $personId
       """.update.run
  }
}

object PersonDDL {
  def initDatabaseQuery = (
      dropPerson.run *>
      createPerson.run
    )

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
}