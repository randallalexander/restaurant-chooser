package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import fs2.Stream
import java.util.UUID
import net.randallalexander.restaurant.chooser.model._
import shapeless.record._

object RestaurantDAO {

  def createRestaurant(restaurant: Restaurant): IO[Restaurant] = {
    val id = UUID.randomUUID().toString
    val restaurantWithId = restaurant.copy(id=Some(id))
    createRestaurantQuery(restaurantWithId).run.transact(xa).map(_ => restaurantWithId)
  }

  private def createRestaurantQuery(restaurant: Restaurant): Update0 = {
    val address = restaurant.address
    val geo = address.geo
    sql"""
          insert into restaurant (
          id, name, addressLine1, city, state, zip, ethnic_type, food_type, price_per_person,cord_lat,cord_long)
           values (
            ${restaurant.id},
            ${restaurant.name.trim},
            ${address.addressLine1.trim},
            ${address.city.trim},
            ${address.state.trim},
            ${address.zip},
            ${restaurant.ethnicity.map(_.name.trim)},
            ${restaurant.kindOfFood.map(_.name.trim)},
            ${restaurant.pricePerPerson},
            ${geo.map(_.lat)},
            ${geo.map(_.lat)})""".update
  }

  private def mapRecordToResponse(record:restaurantRec):Restaurant = {
    val geo = (record('lat), record('long)) match {
      case (Some(lat), Some(long)) => Some(Geo(lat, long))
      case _ => None
    }
    val addr = Address(
      addressLine1 = record('addressLine1),
      city = record('city),
      state = record('state),
      zip = record('zip),
      geo = geo

    )
    Restaurant(
      id = record('id),
      name = record('name),
      address = addr,
      ethnicity = record('ethnicType).flatMap(EthnicityOps.toEnum),
      kindOfFood = record('foodType).flatMap(KindOfFoodOps.toEnum),
      pricePerPerson = record('pricePerPerson)
    )
  }

  //should be able to use LabelledGeneric[Restaurant] instead but can't get the type info
  type restaurantRec = Record.`'id -> Option[String], 'name -> String, 'addressLine1 -> String, 'city -> String, 'state -> String, 'zip -> Int, 'lat -> Option[Double], 'long -> Option[Double], 'ethnicType -> Option[String], 'foodType -> Option[String], 'pricePerPerson -> Option[Double]`.T
  val selectAll = fr"""select id, name, addressLine1, city, state, zip, cord_lat, cord_long, ethnic_type, food_type, price_per_person"""
  val fromRestaurant = fr"""from restaurant"""
  private def streamToList (stream:Stream[ConnectionIO,restaurantRec]):IO[List[Restaurant]] = {
    stream
      .map(mapRecordToResponse)
      .list
      .transact(xa)
  }

  def getRestaurant(id:String): IO[Option[Restaurant]] = {
    getRestaurantQuery(id).transact(xa).map { _.map(mapRecordToResponse)}
  }

  private def getRestaurantQuery(restId:String):ConnectionIO[Option[restaurantRec]] = {
    (selectAll ++ fromRestaurant ++
      fr"""
         where id = $restId
       """).query[restaurantRec].option
  }


  def listRestaurants(offset:Int, limit:Int): IO[List[Restaurant]] = {
    streamToList(listRestaurantQuery(offset,limit))
  }

  private def listRestaurantQuery(offset:Int, limit:Int):Stream[ConnectionIO,restaurantRec] = {
    (selectAll ++ fromRestaurant ++
      fr"""
        limit $limit offset $offset
       """).query[restaurantRec].process
  }


  def getRestaurantByName(name:String): IO[List[Restaurant]] = {
    streamToList(getRestaurantByNameQuery(name))
  }

  private def getRestaurantByNameQuery(name:String):Stream[ConnectionIO,restaurantRec] = {
    val predicateValue = s"%$name%"
    (selectAll ++ fromRestaurant ++
      fr"""
         where name ILIKE $predicateValue
       """).query[restaurantRec].process
  }


  def deleteRestaurant(id:Int): IO[Int] = {
    deleteRestaurantQuery(id).transact(xa)
  }

  private def deleteRestaurantQuery(restaurantId:Int): ConnectionIO[Int] = {
    sql"""
      DELETE FROM restaurant
      WHERE id = $restaurantId
       """.update.run
  }
}

object RestaurantDDL {

  def initDatabaseQuery = (
      dropRestaurant.run *>

      createRestaurant.run
    )

  private val dropRestaurant:Update0 =
    sql"""
    DROP TABLE IF EXISTS restaurant CASCADE
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
      id VARCHAR PRIMARY KEY,
      name VARCHAR NOT NULL,
      addressLine1 VARCHAR NOT NULL,
      city VARCHAR NOT NULL,
      state VARCHAR(2) NOT NULL,
      zip NUMERIC(5, 0) NOT NULL,
      ethnic_type VARCHAR,
      food_type VARCHAR,
      price_per_person NUMERIC (5,2),
      cord_lat NUMERIC(11, 8),
      cord_long NUMERIC(11, 8)
    )
  """.update
}