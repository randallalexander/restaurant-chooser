package net.randallalexander.restaurant.chooser.service

import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.IO
import net.randallalexander.restaurant.chooser.db.{PreferenceDAO, RestaurantDAO}
import net.randallalexander.restaurant.chooser.model.Restaurant
import scala.util.Random

object RestaurantChooser {

  /*
  Poor mans weighting system for prefered items
   */
  def chooseRestaurant(userIds:NonEmptyList[String]): IO[Option[Restaurant]] = {
    (PreferenceDAO.getAllAcceptableRestaurants(userIds) |+|
    PreferenceDAO.getAllPreferredRestaurants(userIds)
      ).map(chooseRandomElement).flatMap {
      case None => IO.pure(None)
      case Some(restId) => RestaurantDAO.getRestaurant(restId)
    }
  }

  def chooseRandomElement[T](items: Seq[T]): Option[T] = {
    items.size match {
      case 0 => None
      case 1 => Some(items(0))
      case _ =>
        val randomValue = Random.nextInt(items.size)
        Some(items(randomValue))
    }
  }
}
