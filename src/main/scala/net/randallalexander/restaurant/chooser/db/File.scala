package net.randallalexander.restaurant.chooser.db

import com.typesafe.config.ConfigFactory
import monix.eval.Task
import net.randallalexander.restaurant.chooser.model.{Restaurant, User}

import scala.collection.JavaConversions._

/*
Yes, Yes using config as a mini-database is bad, but will work well enough to get me started
 */
object File {

  val config = ConfigFactory.load()

  private val dataConfig = config.getConfig("data")

  private val usersConfig = dataConfig.getConfigList("users")

  private val restaurantConfig = dataConfig.getConfigList("restaurants")

  val getUsers: List[User] = usersConfig.toList.map {
    config =>
      User(
        name = config.getString("name"),
        likes = config.getLongList("likes").toList.map(_.toLong),
        dislikes = config.getLongList("dislikes").toList.map(_.toLong)
      )
  }

  val getRestaurants: Map[Long,Restaurant] = restaurantConfig.toList.map {
    config =>
      Restaurant(
        id = config.getLong("id"),
        name = config.getString("name"),
        tags = config.getStringList("tags")
      )
  }.map { rest => rest.id -> rest }.toMap

  def getLikedRestaurantTask(names: Seq[String], tagFilter: Seq[String]): Task[Seq[Restaurant]] = {
    Task {
      getLikedRestaurant(names,tagFilter)
    }
  }

  //TODO:make this a service...if I go to a real db then I won't have to move this to a service(possibly)
  def getLikedRestaurant(names: Seq[String], tagFilter: Seq[String]): Seq[Restaurant] = {
    val matchingUsers = getUsers.filter(
      user =>
        names.contains(user.name)
    )
    val allRestaurants = getRestaurants.keys.toList//get all restaurants
    /*
       If a user likes a restaurant, then that id gets added to the eligible list and NOT dedupped.  Simple ranking
       system so the more people like a restaurant the more it shows up say neutral (not in liked or disliked) feelings.
     */
    val allLikes = matchingUsers.flatMap(_.likes)
    val allDislikes = matchingUsers.flatMap(_.dislikes)
    val filteredByLikes = (allRestaurants ++ allLikes).filterNot(allDislikes.contains)

    tagFilter match {
      case Nil => filteredByLikes.flatMap(getRestaurants.get)
      case _ => filteredByLikes.filter(containsTag(_:Long,tagFilter)).flatMap(getRestaurants.get)
    }
  }

  private def containsTag(id:Long, tags:Seq[String]):Boolean = {
    getRestaurants.get(id).map {
        restaurant =>
          restaurant.tags.intersect(tags).size > 0
      }.getOrElse(false)
  }

}
