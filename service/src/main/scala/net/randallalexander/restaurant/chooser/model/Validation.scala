package net.randallalexander.restaurant.chooser.model

import io.circe.HCursor

object Validation {
  def state(hCursor: HCursor): Boolean = {
    hCursor.downField("state").focus.flatMap(_.asString) match {
      case Some(value) => (value.size == 2) //Good enough for now
      case _ => false
    }
  }

  def zip(hCursor: HCursor): Boolean = {
    hCursor.downField("zip").focus.flatMap(_.asString) match {
      case Some(value) =>
        value.length == 5 //Good enough for now
      case _ => false
    }
  }

  def positiveMoney(fieldName:String)(hCursor: HCursor): Boolean = {
    hCursor.downField(fieldName).focus.flatMap(_.asNumber.map(_.toDouble)) match {
      case Some(value) =>
        value.toString.matches("\\d{1,}(.\\d?\\d?)?")
      case _ => false
    }
  }
  def intGTE(fieldName:String, minValue:Int)(hCursor: HCursor): Boolean = {
    hCursor.downField(fieldName).focus.flatMap(_.asNumber.flatMap(_.toInt)) match {
      case Some(value) =>
        value >= minValue
      case _ => false
    }
  }

}
