package net.randallalexander.restaurant.chooser

//import cats.effect.IO
import cats.data.NonEmptyList
import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import net.randallalexander.restaurant.chooser.db.{PersonDAO, PreferenceDAO, TransactionDAO}
import net.randallalexander.restaurant.chooser.model.{Person, Preference, Transaction}
import net.randallalexander.restaurant.chooser.service.RestaurantChooser
import scala.util.Try
//import com.twitter.util.Future
import net.randallalexander.restaurant.chooser.db.RestaurantDAO
//import fs2.Stream
import net.randallalexander.restaurant.chooser.db.PostgreSQL
import io.circe._
import io.finch._
import io.finch.circe._
import io.finch.syntax.{delete,get,post}
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.filter.RequestLoggingFilter
import net.randallalexander.restaurant.chooser.utils.FutureConversion._
import net.randallalexander.restaurant.chooser.model.Restaurant
import scala.concurrent.ExecutionContext.Implicits.global

object Api {

  /*
    Gets the exception message and transforms it to a json body
   */
  implicit val encodeExceptionCirce: Encoder[Exception] = Encoder.instance(e =>
    Json.obj("message" -> Option(e.getMessage).fold(Json.Null)(Json.fromString))
  )

  def echo(): Endpoint[String] =
    get("v1" :: "echo" :: param("what")) { (what: String) =>
      Ok(what)
    }


  def initDatabase(): Endpoint[Int] =
    get("db") {
      PostgreSQL.initDatabase.map(Ok).unsafeToFuture().asTwitter
    }

////restaurantAPI
  def restaurantPreProcess: Endpoint[Restaurant] = jsonBody[Restaurant].map(_.copy(id = None))

  def createRestaurant(): Endpoint[Restaurant] = post(restaurantPreProcess) { restaurant: Restaurant =>
    RestaurantDAO.createRestaurant(restaurant).map(Ok).unsafeToFuture().asTwitter
  }

  def getRestaurant(): Endpoint[Restaurant] = get(path[String]) { restaurantId:String =>
    RestaurantDAO.getRestaurant(restaurantId).map{
        case Some(result) => Ok(result)
        case None => NotFound(new RuntimeException(s"Restaurant $restaurantId is not found"))
    }.unsafeToFuture().asTwitter
  }

  def listRestaurant(): Endpoint[List[Restaurant]] = get(paramOption("offset") :: paramOption("limit")) { (offsetOpt: Option[String], limitOpt: Option[String]) =>
    val offset = offsetOpt.flatMap(in => Try(in.toInt).toOption).fold(0)(identity)
    val limit = limitOpt.flatMap(in => Try(in.toInt).toOption).fold(5)(identity)
    RestaurantDAO.listRestaurants(offset,limit).map(Ok).unsafeToFuture().asTwitter
  }

  //partial match on name...like a contains
  def searchRestaurant(): Endpoint[List[Restaurant]] = get("name" :: path[String]) { name: String =>
    RestaurantDAO.getRestaurantByName(name).map(Ok).unsafeToFuture().asTwitter
  }

  def deleteRestaurant(): Endpoint[Unit] = delete(path[String]) { restaurantId:String =>
    RestaurantDAO.deleteRestaurant(restaurantId).map{
      case 0 => NotFound(new RuntimeException(s"Person $restaurantId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  def findRestaurant(): Endpoint[Restaurant] = get("choose" :: paramsNel("users")) { ids:NonEmptyList[String] =>
    RestaurantChooser.chooseRestaurant(ids).map{
      case Some(restaurant) => Ok(restaurant)
      case None => NotFound(new RuntimeException("No preferred restaurant in common"))
    }.unsafeToFuture().asTwitter
  }

  ///person API
  def personPreProcess: Endpoint[Person] = jsonBody[Person].map(_.copy(id = None))

  def createPerson(): Endpoint[Person] = post(personPreProcess) { person: Person =>
    PersonDAO
      .createPerson(person)
      .map{
        _.fold(
          constraintViolation => BadRequest(new RuntimeException(constraintViolation.message)),
          person => Ok(person)
        )
      }
      .unsafeToFuture().asTwitter
  }

  def getPerson(): Endpoint[Person] = get(path[String]) { personId:String =>
    PersonDAO.getPerson(personId).map{
      case Some(result) => Ok(result)
      case None => NotFound(new RuntimeException(s"Person $personId is not found"))
    }.unsafeToFuture().asTwitter
  }

  def listPeople(): Endpoint[List[Person]] = get(paramOption("offset") :: paramOption("limit")) { (offsetOpt: Option[String], limitOpt: Option[String]) =>
    val offset = offsetOpt.flatMap(in => Try(in.toInt).toOption).fold(0)(identity)
    val limit = limitOpt.flatMap(in => Try(in.toInt).toOption).fold(5)(identity)
    PersonDAO.listPeople(offset,limit).map(Ok).unsafeToFuture().asTwitter
  }

  //partial match on name...like a contains
  def searchPerson(): Endpoint[List[Person]] = get("nickname" :: path[String]) { name: String =>
    PersonDAO.getPersonByName(name).map(Ok).unsafeToFuture().asTwitter
  }

  def deletePerson(): Endpoint[Unit] = delete(path[Int]) { personId:Int =>
    PersonDAO.deletePerson(personId).map{
      case 0 => NotFound(new RuntimeException(s"Person $personId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }
////preference api
  //TODO:If we have a like something we need to make sure to remove any dislike and vica versa
  def createLike(): Endpoint[Preference] = post("like" :: jsonBody[Preference]) { preference: Preference =>
    PreferenceDAO.createLike(preference).map{
      _.fold(
        constraintViolation => BadRequest(new RuntimeException(constraintViolation.message)),
        preference => Ok(preference)
      )
    }.unsafeToFuture().asTwitter
  }

  def deleteLike(): Endpoint[Unit] = delete("like" :: path[String] :: path[String]) { (personId:String, restaurantId:String) =>
    PreferenceDAO.deleteLike(personId, restaurantId).map{
      case 0 => NotFound(new RuntimeException(s"Like $personId / $restaurantId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  def getLikes(): Endpoint[List[Preference]] = get("like" :: "person" :: path[String]) { personId: String =>
    PreferenceDAO.getLikesByPerson(personId).map(Ok).unsafeToFuture().asTwitter
  }

  def getLike(): Endpoint[Preference] = get("like" :: path[String] :: path[String]) { (personId: String, restaurantId: String)=>
    PreferenceDAO.getLike(personId,restaurantId).map{
      case Some(preference) => Ok(preference)
      case None =>
        NotFound(new RuntimeException(s"Like $personId / $restaurantId is not found"))
    }.unsafeToFuture().asTwitter
  }

  def createDislike(): Endpoint[Preference] = post("dislike" :: jsonBody[Preference]) { preference: Preference =>
    PreferenceDAO.createDislike(preference).map{
      _.fold(
        constraintViolation => BadRequest(new RuntimeException(constraintViolation.message)),
        preference => Ok(preference)
      )
    }.unsafeToFuture().asTwitter
  }

  def deleteDislike(): Endpoint[Unit] = delete("dislike" :: path[String] :: path[String]) { (personId:String, restaurantId:String) =>
    PreferenceDAO.deleteDislike(personId, restaurantId).map{
      case 0 => NotFound(new RuntimeException(s"Dislike $personId / $restaurantId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  def getDislikes(): Endpoint[List[Preference]] = get("dislike" :: "person" :: path[String]) { personId: String =>
    PreferenceDAO.getDislikesByPerson(personId).map(Ok).unsafeToFuture().asTwitter
  }

  def getDislike(): Endpoint[Preference] = get("dislike" :: path[String] :: path[String]) { (personId: String, restaurantId: String)=>
    PreferenceDAO.getDislike(personId,restaurantId).map{
      case Some(preference) => Ok(preference)
      case None =>
        NotFound(new RuntimeException(s"Dislike $personId / $restaurantId is not found"))
    }.unsafeToFuture().asTwitter
  }

////Transaction API
  def createTransaction(): Endpoint[Transaction] = post(jsonBody[Transaction]) { transaction: Transaction =>
    TransactionDAO.createTransaction(transaction).map(Ok).unsafeToFuture().asTwitter
  }

  def getTransaction(): Endpoint[Option[Transaction]] = get(path[String]) { transactionId: String =>
    TransactionDAO.getTransaction(transactionId).map(Ok).unsafeToFuture().asTwitter
  }

  def deleteTransaction(): Endpoint[Unit] = delete(path[String]) { transactionId:String =>
    TransactionDAO.deleteTransaction(transactionId).map{
      case 0 => NotFound(new RuntimeException(s"TransactionId $transactionId is not found"))
      case _ => NoContent[Unit]
    }.unsafeToFuture().asTwitter
  }

  val v1TransactionRoutes =
    "v1" :: "transaction" :: (
      createTransaction() :+: getTransaction() :+: deleteTransaction()
      )


  val v1RestaurantRoutes =
    "v1" :: "restaurant" :: (
        createRestaurant() :+: getRestaurant() :+: listRestaurant() :+: searchRestaurant() :+: deleteRestaurant() :+: findRestaurant()
      )

  val v1PersonRoutes =
    "v1" :: "person" :: (
        createPerson() :+: getPerson() :+: listPeople() :+: searchPerson() :+: deletePerson()
      )

  val v1PreferenceRoutes =
    "v1" :: "preference" :: (
      createLike() :+: deleteLike() :+: getLikes() :+: getLike() :+: createDislike() :+: deleteDislike() :+: getDislikes() :+: getDislike()
      )

  val v1InitRoutes =
    "v1" :: "init" :: (
        initDatabase()
      )

  /*
  TODO: Split up the API according to responsibility
   */

  private def allEndpoints = echo :+: v1InitRoutes :+: v1RestaurantRoutes :+: v1PersonRoutes :+: v1PreferenceRoutes :+: v1TransactionRoutes

  /*
  TODO: Look into effective use of MethodRequiredFilter
   */
  def apiService: Service[Request, Response] =
  RequestLoggingFilter andThen
    ExceptionFilter andThen
    allEndpoints.handle(ErrorHandler.apiErrorHandler).toService
}