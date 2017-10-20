package net.randallalexander.restaurant.chooser.model

import io.circe._
import io.circe.generic.semiauto._

//need date and authors
case class Transaction(id:Option[String], restaurantId:String, total:Double, numberOfPeople:Int)
object Transaction {
  implicit val transactionDecoder: Decoder[Transaction] = deriveDecoder[Transaction]
    .validate(Validation.positiveMoney("total"),"Invalid total")
    .validate(Validation.intGTE("numberOfPeople",1), "Invalid number of people")
  implicit val transactionEncoder: Encoder[Transaction] = deriveEncoder[Transaction]
}
