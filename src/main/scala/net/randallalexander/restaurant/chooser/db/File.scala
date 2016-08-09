package net.randallalexander.restaurant.chooser.db

import com.typesafe.config.ConfigFactory
import net.randallalexander.restaurant.chooser.model.{Restaurant, User}

import scala.collection.JavaConversions._

/*
Yes, Yes using config as a mini-database is bad, but will work well enough to get me started
 */
object File {

  val config = ConfigFactory.load()

  private val usersConfig = config.getConfigList("data.users")

  private val restaurantConfig = config.getConfigList("data.restaurants")

  val getUsers:List[User] = usersConfig.toList.map{
    config =>
      User(
        name = config.getString("name"),
        likes = config.getLongList("likes").toList.map(_.toLong),
        dislikes = config.getLongList("dislikes").toList.map(_.toLong)
      )
  }
  
  val getRestaurants:List[Restaurant] = restaurantConfig.toList.map {
    config =>
      Restaurant(
        id = config.getLong("id"),
        name = config.getString("name")
      )
  }
  
  def getLikedRestaurant(names:Seq[String]): Set[Restaurant] = {
    val matchingUsers = getUsers.filter(
      user =>
        names.contains(user.name)
    )
    val allLikes = matchingUsers.flatMap(_.likes).toSet
    val allDislikes = matchingUsers.flatMap(_.dislikes).toSet

    val restaurantMap = getRestaurants.map{rest => rest.id -> rest}.toMap

    allLikes.diff(allDislikes).flatMap(id => restaurantMap.get(id))
  }

}
