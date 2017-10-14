package net.randallalexander.restaurant.chooser.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Person(id:Int, fname:String, lname:String, nickname:String, likes:Seq[Long], dislikes:Seq[Long])

object Person {
  implicit val userDecoder: Decoder[Person] = deriveDecoder[Person]
  implicit val userEncoder: Encoder[Person] = deriveEncoder[Person]
}

case class Preference (person:Int, restaurant:Int)
object Preference {
  implicit val userDecoder: Decoder[Preference] = deriveDecoder[Preference]
  implicit val userEncoder: Encoder[Preference] = deriveEncoder[Preference]
}