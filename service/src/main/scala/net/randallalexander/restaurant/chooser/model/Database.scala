package net.randallalexander.restaurant.chooser.model

trait Enum extends Product {
  def name:String = productPrefix
}

trait EnumOps[T<:Enum] {
  def values:Seq[T]
  def toEnum(name: String):Option[T] = values.find(_.name == name)
  def toEnumYOLO(name: String):T = toEnum(name).get
}

trait DatabaseEnum extends Enum

trait DatabaseEnumOps[T <: DatabaseEnum] extends EnumOps[T]

trait ApiEnum extends Enum

trait ApiEnumOps[T <: ApiEnum] extends EnumOps[T]