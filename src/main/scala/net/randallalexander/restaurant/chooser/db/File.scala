package net.randallalexander.restaurant.chooser.db

import java.util

import com.typesafe.config.{Config, ConfigFactory}
import monix.eval.Task
import net.randallalexander.restaurant.chooser.model.{Restaurant, User}

import scala.collection.JavaConverters._

/*
Yes, Yes using config as a mini-database is bad, but will work well enough to get me started
 */
object File {

  private val config: Config = ConfigFactory.load()

  private val dataConfig: Config = config.getConfig("data")

  private val usersConfig: util.List[_ <: Config] = dataConfig.getConfigList("users")

  private val restaurantConfig: util.List[_ <: Config] = dataConfig.getConfigList("restaurants")

  val getUsers: List[User] = usersConfig.asScala.map {
    config =>
      User(
        name = config.getString("name"),
        likes = config.getLongList("likes").asScala.map(_.toLong),
        dislikes = config.getLongList("dislikes").asScala.map(_.toLong)
      )
  }.toList

  val getRestaurants: Map[Long,Restaurant] = restaurantConfig.asScala.map {
    config =>
      Restaurant(
        id = config.getLong("id"),
        name = config.getString("name"),
        tags = config.getStringList("tags").asScala.toList
      )
  }.map { rest => rest.id -> rest }.toMap

  //TODO:make this a service...if I go to a real db then I won't have to move this to a service(possibly)
  def getLikedRestaurant(names: Seq[String], tagFilter: Seq[String]): Task[Seq[Restaurant]] = {
    Task {
      val matchingUsers = getUsers.filter(
        user =>
          names.contains(user.name)
      )
      val allRestaurants = getRestaurants.keys.toList //get all restaurants
      /*
       If a user likes a restaurant, then that id gets added to the eligible list and NOT dedupped.  Simple ranking
       system so the more people like a restaurant the more it shows up say neutral (not in liked or disliked) feelings.
     */
      val allLikes = matchingUsers.flatMap(_.likes)
      val allDislikes = matchingUsers.flatMap(_.dislikes)
      val filteredByLikes = (allRestaurants ++ allLikes).filterNot(allDislikes.contains)

      tagFilter match {
        case Nil => filteredByLikes.flatMap(getRestaurants.get)
        case _ => filteredByLikes.filter(containsTag(_: Long, tagFilter)).flatMap(getRestaurants.get)
      }
    }
  }

  private def containsTag(id:Long, tags:Seq[String]):Boolean = {
    getRestaurants.get(id).map {
        restaurant =>
          restaurant.tags.intersect(tags).size > 0
      }.getOrElse(false)
  }

}
