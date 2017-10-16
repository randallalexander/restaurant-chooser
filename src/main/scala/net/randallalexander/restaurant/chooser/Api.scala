package net.randallalexander.restaurant.chooser

//import cats.effect.IO
import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import net.randallalexander.restaurant.chooser.db.PersonDAO
import net.randallalexander.restaurant.chooser.model.Person
//import com.twitter.util.Future
import net.randallalexander.restaurant.chooser.db.RestaurantDAO
//import fs2.Stream
import net.randallalexander.restaurant.chooser.db.PostgreSQL

import io.finch._
import io.finch.circe._
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.filter.RequestLoggingFilter
import net.randallalexander.restaurant.chooser.utils.FutureConversion._
import net.randallalexander.restaurant.chooser.model.Restaurant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Api {

  def echo(): Endpoint[String] =
    get("v1" :: "echo" :: param("what")) { (what: String) =>
      Ok(what)
    }


  def initDatabase(): Endpoint[Int] =
    get("init") {
      /*
      Convert to stream?
       */
      PostgreSQL.initDatabase.map(Ok).unsafeToFuture().asTwitter
    }

  /*
  def chooseRestaurant(): Endpoint[Restaurant] = get("v1" :: "choose" :: "restaurant" :: params("who")) { (who: Seq[String]) =>
  }
*/
  def restaurantPreProcess: Endpoint[Restaurant] = jsonBody[Restaurant].map(_.copy(id = None))

  def createRestaurant(): Endpoint[Restaurant] = post(restaurantPreProcess) { restaurant: Restaurant =>
    RestaurantDAO.createRestaurant(restaurant).map(Ok).unsafeToFuture().asTwitter
  }

  def getRestaurant(): Endpoint[Restaurant] = get(path[Int]) { restaurantId:Int =>
    RestaurantDAO.getRestaurant(restaurantId).map{
        case Some(result) => Ok(result)
        case None => NotFound(new RuntimeException(s"Restaurant $restaurantId is not found"))
    }.unsafeToFuture().asTwitter
  }

  def deleteRestaurant(): Endpoint[Unit] = delete(path[Int]) { restaurantId:Int =>
    RestaurantDAO.deleteRestaurant(restaurantId).map{
      case 0 => NotFound(new RuntimeException(s"Person $restaurantId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  def personPreProcess: Endpoint[Person] = jsonBody[Person].map(_.copy(id = None))

  def createPerson(): Endpoint[Person] = post(personPreProcess) { person: Person =>
    PersonDAO
      .createPerson(person)
      .map{
        _.fold(
          constraintViolation => BadRequest(new RuntimeException(constraintViolation.message)),
          person => Ok(person)
        )
      }
      .unsafeToFuture().asTwitter
  }

  def getPerson(): Endpoint[Person] = get(path[Int]) { personId:Int =>
    PersonDAO.getPerson(personId).map{
      case Some(result) => Ok(result)
      case None => NotFound(new RuntimeException(s"Person $personId is not found"))
    }.unsafeToFuture().asTwitter
  }

  def deletePerson(): Endpoint[Unit] = delete(path[Int]) { personId:Int =>
    PersonDAO.deletePerson(personId).map{
      case 0 => NotFound(new RuntimeException(s"Person $personId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  /*
  Move me
   */
  def chooseRandomElement[T](items: Seq[T]): Option[T] = {
    items.size match {
      case 0 => None
      case 1 => Some(items(0))
      case _ =>
        val randomValue = Random.nextInt(items.size)
        Some(items(randomValue))
    }
  }

  val v1RestaurantRoutes =
    "v1" :: "restaurant" :: (
        createRestaurant() :+: getRestaurant() :+: deleteRestaurant()
      )

  val v1PersonRoutes =
    "v1" :: "person" :: (
        createPerson() :+: getPerson() :+: deletePerson()
      )

  val v1InitRoutes =
    "v1" :: "init" :: (
        initDatabase()
      )

  /*
  TODO: Split up the API according to responsibility
   */

  private def allEndpoints = echo() :+: v1RestaurantRoutes :+: v1PersonRoutes

  /*
  TODO: Look into effective use of MethodRequiredFilter
   */
  def apiService: Service[Request, Response] =
  RequestLoggingFilter andThen
    ExceptionFilter andThen
    allEndpoints.handle(ErrorHandler.apiErrorHandler).toService
}