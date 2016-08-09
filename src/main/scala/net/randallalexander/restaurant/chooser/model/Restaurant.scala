package net.randallalexander.restaurant.chooser.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Restaurant (id:Long, name:String)

object Restaurant {
  implicit val restaurantDecoder: Decoder[Restaurant] = deriveDecoder[Restaurant]
  implicit val restaurantEncoder: Encoder[Restaurant] = deriveEncoder[Restaurant]
}
