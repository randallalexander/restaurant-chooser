package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import net.randallalexander.restaurant.chooser.errors.Errors.ConstraintViolation
import net.randallalexander.restaurant.chooser.model._

object PreferenceDAO {

  private val likesNameFr = fr"""likes"""
  def createLike(preference: Preference):IO[Either[ConstraintViolation,Preference]] = {
    createPreferenceQuery(preference, likesNameFr).run.attemptSomeSqlState {
      case UNIQUE_VIOLATION => ConstraintViolation("Duplicate like")
    }.transact(xa).map(_.map(_ => preference))
  }

  def deleteLike(personId:String, restaurantId:String):IO[Int] = {
    deletePreferenceQuery(personId, restaurantId, likesNameFr).run.transact(xa)
  }

  def getLikesByPerson(id:String):IO[List[Preference]] = {
    getLikesForPersonQuery(id, likesNameFr).transact(xa)
  }

  def getLike(personId:String, restaurantId:String):IO[Option[Preference]] = {
    getPreferenceQuery(personId, restaurantId, likesNameFr).transact(xa)
  }

  private val dislikesNameFr = fr"""dislikes"""
  def createDislike(preference: Preference):IO[Either[ConstraintViolation,Preference]] = {
    createPreferenceQuery(preference, dislikesNameFr).run.attemptSomeSqlState {
      case UNIQUE_VIOLATION => ConstraintViolation("Duplicate dislike")
    }.transact(xa).map(_.map(_ => preference))
  }

  def deleteDislike(personId:String, restaurantId:String):IO[Int] = {
    deletePreferenceQuery(personId, restaurantId, dislikesNameFr).run.transact(xa)
  }

  def getDislikesByPerson(id:String):IO[List[Preference]] = {
    getLikesForPersonQuery(id, dislikesNameFr).transact(xa)
  }

  def getDislike(personId:String, restaurantId:String):IO[Option[Preference]] = {
    getPreferenceQuery(personId, restaurantId, dislikesNameFr).transact(xa)
  }


  private def createPreferenceQuery(preference: Preference, dbName:Fragment):Update0 = {
    (fr"""insert into""" ++ dbName ++ fr"""(person_id, restaurant_id) values (${preference.person}, ${preference.restaurant})""").update
  }

  private def deletePreferenceQuery(personId:String, restaurantId:String, dbName:Fragment):Update0 = {
    (fr"""delete from""" ++ dbName ++ fr"""where person_id = $personId AND restaurant_id = $restaurantId""").update
  }

  private val selectAll = fr"""select person_id, restaurant_id"""
  private def getLikesForPersonQuery(id:String, dbName:Fragment):ConnectionIO[List[Preference]] = {
    (selectAll ++ fr"""from""" ++ dbName ++
      fr"""
         where person_id = $id
       """).query[Preference].list
  }

  private def getPreferenceQuery(personId:String, restaurantId:String, dbName:Fragment):ConnectionIO[Option[Preference]] = {
    (selectAll ++ fr"""from""" ++ dbName ++
      fr"""
         where person_id = $personId AND restaurant_id  = $restaurantId
       """).query[Preference].option
  }
}

object PreferenceDDL {
  def initDatabaseQuery = (
      dropLikes.run *>
      dropDislikes.run *>

      createLikes.run *>
      createDislikes.run
    )

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
