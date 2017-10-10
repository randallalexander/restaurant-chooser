package net.randallalexander.restaurant.chooser

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import net.randallalexander.restaurant.chooser.db.PostgreSQL
//import com.twitter.util.Future
import io.finch._
import io.finch.circe._
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import net.randallalexander.restaurant.chooser.db.File
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.filter.RequestLoggingFilter
import net.randallalexander.restaurant.chooser.utils.FutureConversion._
import net.randallalexander.restaurant.chooser.model.Restaurant

import scala.util.Random

object Api {

  def echo(): Endpoint[String] =
    get("v1" :: "echo" :: string("what")) { (what: String) =>
      Ok(what)
    }

  def initRestaurant(): Endpoint[Int] =
    get("v1" :: "resturaunt" :: "init") {
      PostgreSQL.initPersonTable.map(Ok)
    }

  def chooseRestaurant(): Endpoint[Restaurant] = get("v1" :: "choose" :: "restaurant" :: params("who") :: params("tags")) { (who: Seq[String], tags: Seq[String]) =>
    chooseLikedRestaurant(who, tags).map(_.map(Ok).getOrElse(NotFound(new RuntimeException("Users have no restaurants in common.")))).runAsync.asTwitter
  }

  /*
  Move me
   */
  def chooseLikedRestaurant(who: Seq[String], tags: Seq[String]): Task[Option[Restaurant]] = {
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