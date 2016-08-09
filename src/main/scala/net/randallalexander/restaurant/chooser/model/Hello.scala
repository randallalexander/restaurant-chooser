package net.randallalexander.restaurant.chooser.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Hello(name: String, from: List[String])


object Hello {
  implicit val helloDecoder: Decoder[Hello] = deriveDecoder[Hello]
  implicit val helloEncoder: Encoder[Hello] = deriveEncoder[Hello]
}
