package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import net.randallalexander.restaurant.chooser.model._
import shapeless.record._

object RestaurantDAO {

  def createRestaurant(restaurant: Restaurant): IO[Restaurant] = {
    createRestaurantQuery(restaurant).transact(xa).map { newId =>
      restaurant.copy(id = Some(newId))
    }
  }

  private def createRestaurantQuery(restaurant: Restaurant): ConnectionIO[Int] = {
    val address = restaurant.address
    val geo = address.geo
    sql"""
          insert into restaurant (
          name, addressLine1, city, state, zip, ethnic_type, food_type, price_per_person,cord_lat,cord_long)
           values (
            ${restaurant.name},
            ${address.addressLine1},
            ${address.city},
            ${address.state},
            ${address.zip},
            ${restaurant.ethnicity.map(_.name)},
            ${restaurant.kindOfFood.map(_.name)},
            ${restaurant.pricePerPerson},
            ${geo.map(_.lat)},
            ${geo.map(_.lat)})""".update.withUniqueGeneratedKeys[Int]("id")
  }

  def getRestaurant(id:Int): IO[Option[Restaurant]] = {
    getRestaurantQuery(id).transact(xa).map { _.map {
      record =>
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
    }
  }

  //should be able to use LabelledGeneric[Restaurant] instead but can't get the type info
  type restaurantRec = Record.`'id -> Option[Int], 'name -> String, 'addressLine1 -> String, 'city -> String, 'state -> String, 'zip -> Int, 'lat -> Option[Double], 'long -> Option[Double], 'ethnicType -> Option[String], 'foodType -> Option[String], 'pricePerPerson -> Option[Double]`.T
  private def getRestaurantQuery(restId:Int):ConnectionIO[Option[restaurantRec]] = {
    sql"""
         select id, name, addressLine1, city, state, zip, cord_lat, cord_long, ethnic_type, food_type, price_per_person from restaurant where id = $restId
       """.query[restaurantRec].option
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
      id SERIAL PRIMARY KEY,
      name varchar NOT NULL,
      addressLine1 varchar NOT NULL,
      city varchar NOT NULL,
      state varchar(2) NOT NULL,
      zip NUMERIC(5, 0) NOT NULL,
      ethnic_type VARCHAR,
      food_type VARCHAR,
      price_per_person NUMERIC (5,2),
      cord_lat NUMERIC(11, 8),
      cord_long NUMERIC(11, 8)
    )
  """.update
}