package net.randallalexander.restaurant.chooser.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class User(name:String, likes:Seq[Long], dislikes:Seq[Long])

object User {
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
}
