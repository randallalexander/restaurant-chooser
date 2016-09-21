package net.randallalexander.restaurant.chooser

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.finch._
import io.finch.circe._
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import net.randallalexander.restaurant.chooser.db.File
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.filter.RequestLoggingFilter
import net.randallalexander.restaurant.chooser.utils.FutureConversion._
import net.randallalexander.restaurant.chooser.model.{Hello, Restaurant}
import scala.util.Random

object Api {
  /*
  TODO: Create a service and API section for each service and move the applicable stuff out!!
   */
  def helloApi() = hello :+: getEcho

  def hello: Endpoint[Hello] =
    get("v1" :: "hello" :: string("from") :: params("to")) { (from: String, to: Seq[String]) =>
      //Task is overkill but just trying it out
      Task {
        Ok(Hello(from, to.toList))
      }.runAsync.asTwitter
    }

  def getEcho: Endpoint[String] =
    get("v1" :: "echo" :: string("what")) { (what: String) =>
      //Future is overkill, just experimenting
      Future {
        Ok(what)
      }
    }

  def chooseApi() = chooseRestaurant

  def chooseRestaurant: Endpoint[Restaurant] = get("v1" :: "choose" :: "restaurant" :: params("who") :: params("tags")) { (who: Seq[String], tags: Seq[String]) =>
    chooseLikedRestaurant(who, tags).map(_.map(Ok).getOrElse(NotFound(new RuntimeException("Users have no restaurants in common.")))).runAsync.asTwitter
  }

  def chooseLikedRestaurant(who: Seq[String], tags: Seq[String]): Task[Option[Restaurant]] = {
    File.getLikedRestaurantTask(who, tags).map {
      likedRestaurants =>
        likedRestaurants.toList match {
          case Nil => None
          case liked =>
            val randomValue = Random.nextInt(liked.size)
            Some(liked(randomValue))
        }
    }
  }

  private def api = helloApi() :+: chooseApi()

  /*
  TODO: Look into effective use of MethodRequiredFilter
   */
  def apiService: Service[Request, Response] =
  RequestLoggingFilter andThen
    ExceptionFilter andThen
    api.handle(ErrorHandler.apiErrorHandler).toService
}