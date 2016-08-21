package net.randallalexander.restaurant.chooser

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.{ExceptionFilter}
import com.twitter.finagle.http.{Request, Response}
import io.finch._
import io.finch.circe._
import net.randallalexander.restaurant.chooser.db.File
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.filter.RequestLoggingFilter
import net.randallalexander.restaurant.chooser.model.{Hello, Restaurant}

import scala.util.Random

object Api {
  /*
  TODO: Create a service and API section for each service and move the applicable stuff out!!
   */
  def helloApi() = hello :+: getEcho

  def hello: Endpoint[Hello] =
    get("v1" :: "hello" :: string("from") :: params("to")) { (from: String, to: Seq[String]) =>
      Ok(Hello(from, to.toList))
    }

  def getEcho: Endpoint[String] =
    get("v1" :: "echo" :: string("what")) { (what: String) =>
      Ok(what)
    }

  def chooseApi() = chooseRestaurant

  def chooseRestaurant: Endpoint[Restaurant] = get("v1" :: "choose" :: "restaurant" :: params("who") :: params("tags")) { (who: Seq[String], tags: Seq[String]) =>
    chooseLikedRestaurant(who, tags).map(Ok).getOrElse(NotFound(new RuntimeException("Users have no restaurants in common.")))
  }

  def chooseLikedRestaurant(who: Seq[String], tags: Seq[String]): Option[Restaurant] = {
    val likedRest = File.getLikedRestaurant(who, tags).toList
    likedRest match {
      case Nil => None
      case liked =>
        val randomValue = Random.nextInt(liked.size)
        Some(liked(randomValue))
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