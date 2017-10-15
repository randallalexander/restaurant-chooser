package net.randallalexander.restaurant.chooser

//import cats.effect.IO
import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
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


  def initRestaurant(): Endpoint[Int] =
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
    RestaurantDAO.createRestaurant(restaurant).map(Ok).unsafeToFuture().asTwitter.onFailure {
      case th => th.printStackTrace()
    }
  }

  def getRestaurant(): Endpoint[Restaurant] = get(path[Int]) { restaurantId:Int =>
    /*
    val geo:Geo =  Geo(1.0d,1.0d)
    val address:Address = Address("addrLn1","cty","st",1,Some(geo))
    val restaurant:Restaurant = Restaurant(None,"nam",address,Some(mexican),Some(burrito),Some(1.1d))
    */
    RestaurantDAO.getRestaurant(restaurantId).map{
        case Some(result) => Ok(result)
        case None => NotFound(new RuntimeException(s"Restaurant $restaurantId is not found"))
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
      initRestaurant() :+: createRestaurant() :+: getRestaurant()

      )

  private def api = echo() :+: v1RestaurantRoutes

  /*
  TODO: Look into effective use of MethodRequiredFilter
   */
  def apiService: Service[Request, Response] =
  RequestLoggingFilter andThen
    ExceptionFilter andThen
    api.handle(ErrorHandler.apiErrorHandler).toService
}