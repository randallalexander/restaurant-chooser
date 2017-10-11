package net.randallalexander.restaurant.chooser

import cats.effect.IO
import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import fs2.Stream
//import net.randallalexander.restaurant.chooser.db.PostgreSQL
//import com.twitter.util.Future
import io.finch._
import io.finch.circe._
import net.randallalexander.restaurant.chooser.db.File
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
    get("v1" :: "restaurant" :: "init") {
      Ok(-1)
      //PostgreSQL.initPersonTable.map(Ok)
    }

  def chooseRestaurant(): Endpoint[Restaurant] = get("v1" :: "choose" :: "restaurant" :: params("who") :: params("tags")) { (who: Seq[String], tags: Seq[String]) =>
    /*
    Probably could get away with just running the IO but
    part of the idea in to get familiar with Stream and
    hopefully something will pop so we can leverage it more
     */
    Stream
      .eval(chooseLikedRestaurant(who, tags))
      .runLast
      .map(_.flatten)
      .map{
        case Some(restaurant) => Ok(restaurant)
        case None => NotFound(new RuntimeException("Users have no restaurants in common."))
      }.unsafeToFuture().asTwitter
  }

  /*
  Move me
   */
  def chooseLikedRestaurant(who: Seq[String], tags: Seq[String]): IO[Option[Restaurant]] = {
    File.getLikedRestaurant(who, tags).map {
      likedRestaurants =>
        likedRestaurants.toList match {
          case Nil => None
          case liked =>
            val randomValue = Random.nextInt(liked.size)
            Some(liked(randomValue))
        }
    }
  }

  private def api = echo() :+: chooseRestaurant() :+: initRestaurant()

  /*
  TODO: Look into effective use of MethodRequiredFilter
   */
  def apiService: Service[Request, Response] =
  RequestLoggingFilter andThen
    ExceptionFilter andThen
    api.handle(ErrorHandler.apiErrorHandler).toService
}