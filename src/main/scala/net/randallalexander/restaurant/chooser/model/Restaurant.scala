package net.randallalexander.restaurant.chooser.model

import io.circe._
import io.circe.generic.semiauto._
import net.randallalexander.restaurant.chooser.model.Ethnicity._
import net.randallalexander.restaurant.chooser.model.KindOfFood._

/*
  maybe calculate pricePerPerson when request is made instead?
  ....
  how about keeping track of price and person tranx info per restaurant and calc pricePerPerson on the fly
  probably need a search api to make that useable

 */

case class Restaurant (id:Option[String], name:String, address: Address, ethnicity: Option[Ethnicity], kindOfFood:Option[KindOfFood], pricePerPerson:Option[Double])

object Restaurant {
  implicit val restaurantDecoder: Decoder[Restaurant] = deriveDecoder[Restaurant]
  implicit val restaurantEncoder: Encoder[Restaurant] = deriveEncoder[Restaurant]
}

case class Address(addressLine1:String,city:String,state:String,zip:Int, geo:Option[Geo])

object Address {
  implicit val addressDecoder: Decoder[Address] = deriveDecoder[Address]
  implicit val addressEncoder: Encoder[Address] = deriveEncoder[Address]
}

case class Geo(lat:Double, long:Double)

object Geo {
  implicit val geoDecoder: Decoder[Geo] = deriveDecoder[Geo]
  implicit val geoEncoder: Encoder[Geo] = deriveEncoder[Geo]
}

sealed trait Ethnicity extends DatabaseEnum
object Ethnicity {
  case object mexican extends Ethnicity
  case object american extends Ethnicity
  case object italian extends Ethnicity
  case object chinese extends Ethnicity
  case object greek extends Ethnicity

  implicit val ethnicityEncoder: Encoder[Ethnicity] = new Encoder[Ethnicity] {
    final def apply(ethnicity: Ethnicity): Json = {
      Json.fromString(ethnicity.name)
    }
  }

  implicit val ethnicityDecoder: Decoder[Ethnicity] = new Decoder[Ethnicity] {
    final def apply(cursor: HCursor): Decoder.Result[Ethnicity] = {
      val value = cursor.value.asString
      val kindOfFood = value.flatMap(EthnicityOps.toEnum)
      kindOfFood.toRight(DecodingFailure("Invalid ethnicity", cursor.history))
    }
  }
}

object EthnicityOps extends DatabaseEnumOps [Ethnicity] {
  override def values = Seq(mexican,american,italian,chinese,greek)
}

sealed trait KindOfFood extends DatabaseEnum
object KindOfFood {
  case object sandwich extends KindOfFood
  case object burrito extends KindOfFood

  implicit val kindOfFoodEncoder: Encoder[KindOfFood] = new Encoder[KindOfFood] {
    final def apply(kindOfFood: KindOfFood): Json = {
      Json.fromString(kindOfFood.name)
    }
  }

  implicit val kindOfFoodDecoder: Decoder[KindOfFood] = new Decoder[KindOfFood] {
    final def apply(cursor: HCursor): Decoder.Result[KindOfFood] = {
      val value = cursor.value.asString
      val kindOfFood = value.flatMap(KindOfFoodOps.toEnum)
      kindOfFood.toRight(DecodingFailure("Invalid kind of food", cursor.history))
    }
  }
}

object KindOfFoodOps extends DatabaseEnumOps [KindOfFood] {
  override def values = Seq(burrito,sandwich)
}