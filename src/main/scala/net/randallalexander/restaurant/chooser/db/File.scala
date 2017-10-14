package net.randallalexander.restaurant.chooser.db
/*
import cats.effect.IO
import java.util
import com.typesafe.config.{Config, ConfigFactory}
import net.randallalexander.restaurant.chooser.model.{Restaurant, Person}
import scala.collection.JavaConverters._

/*
Yes, Yes using config as a mini-database is bad, but will work well enough to get me started
 */
object File {

  private val config: IO[Config] = IO.pure(ConfigFactory.load())

  private val dataConfig: IO[Config] = config.map(_.getConfig("data"))

  private val usersConfig: IO[util.List[_ <: Config]] = dataConfig.map(_.getConfigList("users"))

  private val restaurantConfig: IO[util.List[_ <: Config]] = dataConfig.map(_.getConfigList("restaurants"))

  val getUsers: IO[List[Person]] = usersConfig
    .map{
      _.asScala.map{
        conf =>
          Person(
            name = conf.getString("name"),
            likes = conf.getLongList("likes").asScala.map(_.toLong),
            dislikes = conf.getLongList("dislikes").asScala.map(_.toLong)
          )
      }
    }.map(_.toList)

  val getRestaurants: IO[Map[Long,Restaurant]] = restaurantConfig
      .map{
        _.asScala.map {
          config =>
            Restaurant(
              id = config.getLong("id"),
              name = config.getString("name"),
              tags = config.getStringList("tags").asScala.toList
            )
        }.map {
          rest =>
            rest.id -> rest
        }.toMap
      }

  //TODO:make this a service...if I go to a real db then I won't have to move this to a service(possibly)
  def getLikedRestaurant(names: Seq[String], tagFilter: Seq[String]): IO[Seq[Restaurant]] = {
      val matchingUsers = getUsers.map{
        _.filter(
          user =>
            names.contains(user.name)
        )
      }
      val allRestaurants = getRestaurants.map(_.keys.toList) //get all restaurants
      /*
       If a user likes a restaurant, then that id gets added to the eligible list and NOT dedupped.  Simple ranking
       system so the more people like a restaurant the more it shows up say neutral (not in liked or disliked) feelings.
     */
      val allLikes = matchingUsers.map(_.flatMap(_.likes))
      val allDislikes = matchingUsers.map(_.flatMap(_.dislikes))
      /*
      Todo: make an effects applicative for IO if the IO.type is an applicative
       */
    val filterByLikes = for {
        rest <- allRestaurants
        likes <- allLikes
        all  = rest ++ likes
        dislikes <- allDislikes
      } yield {
        all.filterNot(dislikes.contains)
      }

      tagFilter match {
        case Nil =>
          for {
            likes <- filterByLikes
            restaurants <- getRestaurants
          } yield {
            likes.flatMap(restaurants.get)
          }
        case _ =>
          for {
            likes <- filterByLikes
            restaurants <- getRestaurants
          } yield {
            likes
              .flatMap(restaurants.get)
              .filter {
                restaurant =>
                  restaurant.tags.intersect(tagFilter).size > 0
              }
          }
      }
  }
}
*/